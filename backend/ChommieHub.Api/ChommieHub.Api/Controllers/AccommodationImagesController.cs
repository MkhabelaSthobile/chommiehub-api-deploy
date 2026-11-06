using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using ChommieHub.Api.Storage;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers;

/// <summary>
/// Image uploads are manager-owner/admin only. Public downloads are allowed only
/// for approved, non-archived listings. Azure container access stays private.
/// </summary>
[ApiController]
[Route("api/images/accommodations")]
public sealed class AccommodationImagesController : ControllerBase
{
    private const long MaxBytes = 5 * 1024 * 1024;
    private const int MaxPhotosPerListing = 12;
    private readonly AzureAccommodationImageStore _storage;
    private readonly IAccommodationRepository _accommodations;
    private readonly IManagerRepository _managers;
    private readonly FirebaseTokenVerificationService _tokens;
    private readonly ILogger<AccommodationImagesController> _logger;

    public AccommodationImagesController(
        AzureAccommodationImageStore storage,
        IAccommodationRepository accommodations,
        IManagerRepository managers,
        FirebaseTokenVerificationService tokens,
        ILogger<AccommodationImagesController> logger)
    {
        _storage = storage;
        _accommodations = accommodations;
        _managers = managers;
        _tokens = tokens;
        _logger = logger;
    }

    [HttpPost("{accommodationId}")]
    [RequestSizeLimit(MaxBytes + 64 * 1024)]
    public async Task<IActionResult> Upload(string accommodationId, IFormFile image, CancellationToken ct)
    {
        var (uid, token) = await AuthenticateAsync();
        if (uid is null) return Unauthorized(new { message = "Sign in before uploading." });
        var listing = await _accommodations.GetByIdAsync(accommodationId, ct);
        if (listing is null) return NotFound(new { message = "Accommodation was not found." });
        if (!await HasUploadPermissionAsync(uid, token!, accommodationId, ct))
            return StatusCode(403, new { message = "You do not manage this accommodation." });
        if (!_storage.IsConfigured)
            return StatusCode(503, new { message = "Azure Blob Storage has not been configured." });
        if (image is null || image.Length == 0 || image.Length > MaxBytes)
            return BadRequest(new { message = "Choose an image smaller than 5 MB." });
        if (listing.ImageUrls.Count >= MaxPhotosPerListing)
            return Conflict(new { message = "Only 12 photos are allowed per listing." });

        // Validate the actual image signature, not just its MIME type or filename.
        byte[] header = new byte[12];
        await using (var stream = image.OpenReadStream())
        {
            int read = await stream.ReadAsync(header, ct);
            if (read < 4) return BadRequest(new { message = "Invalid image." });
        }
        string? extension = GetImageExtension(header);
        if (extension is null) return BadRequest(new { message = "Only JPEG, PNG and WebP images are supported." });
        string mime = extension switch
        {
            "jpg" => "image/jpeg", "png" => "image/png", _ => "image/webp"
        };
        string name = $"{Guid.NewGuid():N}.{extension}";
        string key = $"{accommodationId}/{name}";
        try
        {
            await using var stream = image.OpenReadStream();
            await _storage.UploadAsync(key, stream, mime, ct);
            string path = $"/api/images/accommodations/{Uri.EscapeDataString(accommodationId)}/{name}";
            listing.ImageUrls.Add(path);
            listing.UpdatedAtUtc = DateTime.UtcNow;
            await _accommodations.UpdateAsync(listing, ct);
            return Ok(new { path, count = listing.ImageUrls.Count });
        }
        catch (Exception ex) when (ex is Azure.RequestFailedException or InvalidOperationException)
        {
            _logger.LogError(ex, "Blob image upload failed for accommodation {Id}", accommodationId);
            return StatusCode(503, new { message = "Image storage is temporarily unavailable." });
        }
    }

    [HttpGet("{accommodationId}/{fileName}")]
    public async Task<IActionResult> Download(string accommodationId, string fileName, CancellationToken ct)
    {
        if (fileName.Contains('/') || fileName.Contains('\\') || !System.Text.RegularExpressions.Regex.IsMatch(fileName, "^[a-f0-9]{32}\\.(jpg|png|webp)$"))
            return NotFound();
        Accommodation? listing = await _accommodations.GetByIdAsync(accommodationId, ct);
        if (listing is null) return NotFound();
        if (!listing.ImageUrls.Contains($"/api/images/accommodations/{Uri.EscapeDataString(accommodationId)}/{fileName}"))
            return NotFound();
        if (!listing.IsVisibleToStudents)
        {
            var (uid, token) = await AuthenticateAsync();
            if (uid is null) return NotFound();
            if (!await HasUploadPermissionAsync(uid, token!, accommodationId, ct)) return NotFound();
        }
        if (!_storage.IsConfigured) return StatusCode(503, new { message = "Image storage is unavailable." });
        try
        {
            var downloaded = await _storage.OpenReadAsync($"{accommodationId}/{fileName}", ct);
            return downloaded is null ? NotFound() : File(downloaded.Value.Content, downloaded.Value.ContentType);
        }
        catch (Azure.RequestFailedException ex)
        {
            _logger.LogError(ex, "Blob image download failed for {Id}", accommodationId);
            return StatusCode(503, new { message = "Image storage is temporarily unavailable." });
        }
    }

    private async Task<bool> HasUploadPermissionAsync(string uid, string token, string accommodationId, CancellationToken ct)
    {
        if (await _tokens.VerifyAdminAsync(token)) return true;
        Manager? manager = await _managers.GetByIdAsync(uid, ct);
        return manager is not null && !manager.IsDisabled &&
            string.Equals(manager.AccommodationId, accommodationId, StringComparison.Ordinal);
    }

    private async Task<(string? Uid, string? Token)> AuthenticateAsync()
    {
        string header = Request.Headers.Authorization.ToString();
        if (!header.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase)) return (null, null);
        string token = header[7..].Trim();
        return (await _tokens.VerifyUserIdAsync(token), token);
    }

    private static string? GetImageExtension(byte[] h)
    {
        if (h[0] == 0xFF && h[1] == 0xD8 && h[2] == 0xFF) return "jpg";
        if (h[0] == 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47 &&
            h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A) return "png";
        if (h[0] == 0x52 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x46 &&
            h[8] == 0x57 && h[9] == 0x45 && h[10] == 0x42 && h[11] == 0x50) return "webp";
        return null;
    }
}
