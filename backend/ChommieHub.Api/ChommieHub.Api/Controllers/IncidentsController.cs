using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Services;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers;

/// <summary>
/// Resident incident submission, scoped manager review and administrator oversight.
/// All reads and writes require a verified Firebase bearer token.
/// </summary>
[ApiController]
[Route("api/incidents")]
public sealed class IncidentsController : ControllerBase
{
    private readonly FirestoreDb _db;
    private readonly CommunityAccess _access;
    private static readonly HashSet<string> Categories = new(StringComparer.OrdinalIgnoreCase)
        { "Safety", "Security", "Damage", "Noise", "Other" };
    private static readonly HashSet<string> Statuses = new(StringComparer.OrdinalIgnoreCase)
        { "Submitted", "Under Review", "Completed" };

    public IncidentsController(FirestoreDb db, CommunityAccess access) { _db = db; _access = access; }
    private Task<CommunityIdentity?> Identity(CancellationToken ct) =>
        _access.ResolveAsync(Request.Headers.Authorization.ToString(), ct);

    [HttpPost]
    public async Task<IActionResult> Create(CreateIncidentRequest request, CancellationToken ct)
    {
        var identity = await Identity(ct);
        if (identity is null) return Unauthorized();
        if (identity.Resident is null || identity.IsAdmin || identity.ManagerAccommodationId is not null)
            return StatusCode(403, new { message = "Only active residents may report incidents." });
        if (!Categories.Contains(request.Category?.Trim() ?? string.Empty))
            return BadRequest(new { message = "Use Safety, Security, Damage, Noise or Other." });
        var reference = _db.Collection("incidentReports").Document();
        var incident = new IncidentReport
        {
            Id = reference.Id, StudentId = identity.Uid,
            ResidentId = identity.Resident.Id,
            AccommodationId = identity.Resident.AccommodationId,
            Category = request.Category.Trim(), Description = request.Description.Trim(),
            Location = request.Location.Trim(), Status = "Submitted", CreatedAtUtc = DateTime.UtcNow
        };
        await reference.SetAsync(incident, cancellationToken: ct);
        return StatusCode(StatusCodes.Status201Created, incident);
    }

    [HttpGet("mine")]
    public async Task<IActionResult> Mine(CancellationToken ct)
    {
        var identity = await Identity(ct);
        if (identity is null) return Unauthorized();
        if (identity.Resident is null) return StatusCode(403);
        var snapshot = await _db.Collection("incidentReports").WhereEqualTo("StudentId", identity.Uid)
            .GetSnapshotAsync(ct);
        return Ok(snapshot.Documents.Select(d => d.ConvertTo<IncidentReport>())
            .OrderByDescending(i => i.CreatedAtUtc).ToArray());
    }

    [HttpGet("managed")]
    public async Task<IActionResult> Managed(CancellationToken ct)
    {
        var identity = await Identity(ct);
        if (identity is null) return Unauthorized();
        if (!identity.IsAdmin && identity.ManagerAccommodationId is null) return StatusCode(403);
        Query query = _db.Collection("incidentReports");
        if (!identity.IsAdmin) query = query.WhereEqualTo("AccommodationId", identity.ManagerAccommodationId);
        var snapshot = await query.GetSnapshotAsync(ct);
        return Ok(snapshot.Documents.Select(d => d.ConvertTo<IncidentReport>())
            .OrderByDescending(i => i.CreatedAtUtc).ToArray());
    }

    [HttpPatch("{id}/review")]
    public async Task<IActionResult> Review(string id, ReviewIncidentRequest request, CancellationToken ct)
    {
        var identity = await Identity(ct);
        if (identity is null) return Unauthorized();
        if (!identity.IsAdmin && identity.ManagerAccommodationId is null) return StatusCode(403);
        if (!Statuses.Contains(request.Status?.Trim() ?? string.Empty))
            return BadRequest(new { message = "Invalid incident status." });
        if (request.Status.Equals("Completed", StringComparison.OrdinalIgnoreCase)
            && string.IsNullOrWhiteSpace(request.ResolutionNote))
            return BadRequest(new { message = "A resolution note is required to complete an incident." });
        var reference = _db.Collection("incidentReports").Document(id);
        var snapshot = await reference.GetSnapshotAsync(ct);
        if (!snapshot.Exists) return NotFound();
        var existing = snapshot.ConvertTo<IncidentReport>();
        if (!identity.IsAdmin && !identity.IsManagerFor(existing.AccommodationId)) return StatusCode(403);
        // The manager cannot change who created the incident or its accommodation.
        existing.Status = request.Status.Trim();
        existing.ResolutionNote = request.ResolutionNote?.Trim() ?? string.Empty;
        existing.UpdatedAtUtc = DateTime.UtcNow;
        existing.ResolvedAtUtc = existing.Status.Equals("Completed", StringComparison.OrdinalIgnoreCase)
            ? DateTime.UtcNow : null;
        // Update only review fields so the separately stored private PhotoBlobKey
        // remains intact after a manager changes incident status.
        await reference.UpdateAsync(new Dictionary<string, object>
        {
            ["Status"] = existing.Status,
            ["ResolutionNote"] = existing.ResolutionNote,
            ["UpdatedAtUtc"] = existing.UpdatedAtUtc!.Value,
            ["ResolvedAtUtc"] = existing.ResolvedAtUtc.HasValue
                ? (object)existing.ResolvedAtUtc.Value : FieldValue.Delete
        }, cancellationToken: ct);
        return Ok(existing);
    }
}
