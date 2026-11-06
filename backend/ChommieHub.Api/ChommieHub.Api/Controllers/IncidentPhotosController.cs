using ChommieHub.Api.Models;
using ChommieHub.Api.Services;
using ChommieHub.Api.Storage;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers;

/// <summary>
/// Private Azure incident photo upload/download. Only the resident who submitted
/// the incident, their assigned manager or an administrator can read the image.
/// </summary>
[ApiController]
[Route("api/incidents/{incidentId}/photo")]
public sealed class IncidentPhotosController : ControllerBase
{
    private const long MaxBytes = 5L * 1024 * 1024;
    private readonly FirestoreDb _db;
    private readonly CommunityAccess _access;
    private readonly AzureAccommodationImageStore _storage;
    private readonly ILogger<IncidentPhotosController> _logger;

    public IncidentPhotosController(FirestoreDb db, CommunityAccess access,
        AzureAccommodationImageStore storage, ILogger<IncidentPhotosController> logger)
    { _db = db; _access = access; _storage = storage; _logger = logger; }

    [HttpPost]
    [RequestSizeLimit(MaxBytes + 64 * 1024)]
    public async Task<IActionResult> Upload(string incidentId, IFormFile image, CancellationToken ct)
    {
        var identity = await _access.ResolveAsync(Request.Headers.Authorization.ToString(), ct);
        if (identity is null) return Unauthorized();
        var reference = _db.Collection("incidentReports").Document(incidentId);
        var snapshot = await reference.GetSnapshotAsync(ct);
        if (!snapshot.Exists) return NotFound();
        var incident = snapshot.ConvertTo<IncidentReport>();
        if (identity.Uid != incident.StudentId) return StatusCode(403);
        if (!string.IsNullOrWhiteSpace(incident.PhotoUrl))
            return Conflict(new { message = "An image has already been uploaded for this incident." });
        if (!_storage.IsConfigured) return StatusCode(503, new { message = "Azure image storage is not configured." });
        // All supported image signatures require at least 12 bytes to inspect safely.
        if (image is null || image.Length < 12 || image.Length > MaxBytes)
            return BadRequest(new { message = "A valid image under 5 MB is required." });
        byte[] signature = new byte[12];
        try
        {
            await using var signatureStream = image.OpenReadStream();
            // ReadExactlyAsync handles short reads instead of silently using incomplete data.
            await signatureStream.ReadExactlyAsync(signature.AsMemory(), ct);
        }
        catch (EndOfStreamException)
        {
            return BadRequest(new { message = "The image is incomplete." });
        }
        string? extension = GetExtension(signature);
        if (extension is null) return BadRequest(new { message = "Only JPEG, PNG or WebP is supported." });
        string key = $"incidents/{incidentId}/{Guid.NewGuid():N}.{extension}";
        string contentType = extension == "jpg" ? "image/jpeg" : extension == "png" ? "image/png" : "image/webp";
        try
        {
            await using var stream = image.OpenReadStream();
            await _storage.UploadAsync(key, stream, contentType, ct);
            incident.PhotoUrl = $"/api/incidents/{Uri.EscapeDataString(incidentId)}/photo";
            incident.UpdatedAtUtc = DateTime.UtcNow;
            // Store the private blob key separately; never expose a public SAS URL.
            await reference.UpdateAsync(new Dictionary<string, object>
            {
                ["PhotoUrl"] = incident.PhotoUrl,
                ["PhotoBlobKey"] = key,
                ["UpdatedAtUtc"] = incident.UpdatedAtUtc.Value
            }, cancellationToken: ct);
            return Ok(new { path = incident.PhotoUrl });
        }
        catch (Azure.RequestFailedException exception)
        {
            _logger.LogError(exception, "Incident photo upload failed.");
            return StatusCode(503, new { message = "Image storage is unavailable." });
        }
    }

    [HttpGet]
    public async Task<IActionResult> Download(string incidentId, CancellationToken ct)
    {
        var identity = await _access.ResolveAsync(Request.Headers.Authorization.ToString(), ct);
        if (identity is null) return Unauthorized();
        var snapshot = await _db.Collection("incidentReports").Document(incidentId).GetSnapshotAsync(ct);
        if (!snapshot.Exists) return NotFound();
        var incident = snapshot.ConvertTo<IncidentReport>();
        if (!identity.IsAdmin && identity.Uid != incident.StudentId && !identity.IsManagerFor(incident.AccommodationId))
            return StatusCode(403);
        if (!snapshot.ToDictionary().TryGetValue("PhotoBlobKey", out object? keyValue) ||
            keyValue is not string key || string.IsNullOrWhiteSpace(key))
            return NotFound();
        if (!_storage.IsConfigured) return StatusCode(503);
        try
        {
            var photo = await _storage.OpenReadAsync(key, ct);
            return photo is null ? NotFound() : File(photo.Value.Content, photo.Value.ContentType);
        }
        catch (Azure.RequestFailedException exception)
        {
            _logger.LogError(exception, "Incident image retrieval failed.");
            return StatusCode(503);
        }
    }

    private static string? GetExtension(byte[] h)
    {
        if (h[0] == 0xFF && h[1] == 0xD8 && h[2] == 0xFF) return "jpg";
        if (h[0] == 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47 &&
            h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) return "png";
        if (h[0] == 0x52 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x46 &&
            h[8] == 0x57 && h[9] == 0x45 && h[10] == 0x42 && h[11] == 0x50) return "webp";
        return null;
    }
}
