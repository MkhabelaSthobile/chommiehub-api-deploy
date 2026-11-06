using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services.Interfaces;
using ChommieHub.Api.Services;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers;

/// <summary>
/// Manager residence notices and administrator platform announcements.
/// Targeted notices are never returned to unrelated residents or students.
/// </summary>
[ApiController]
[Route("api/announcements")]
public sealed class AnnouncementsController : ControllerBase
{
    private readonly FirestoreDb _db;
    private readonly CommunityAccess _access;
    private readonly IAccommodationRepository _accommodations;
    private readonly IResidentRepository _residents;
    private readonly IStudentRepository _students;
    private readonly IUserNotifier _notifier;
    private static readonly HashSet<string> Types = new(StringComparer.OrdinalIgnoreCase)
        { "Maintenance", "Safety", "Meeting", "Interruption", "Weather", "General" };

    public AnnouncementsController(FirestoreDb db, CommunityAccess access,
        IAccommodationRepository accommodations, IResidentRepository residents,
        IStudentRepository students, IUserNotifier notifier)
    { _db = db; _access = access; _accommodations = accommodations;
      _residents = residents; _students = students; _notifier = notifier; }

    private Task<CommunityIdentity?> Identity(CancellationToken ct) =>
        _access.ResolveAsync(Request.Headers.Authorization.ToString(), ct);

    [HttpPost]
    public async Task<IActionResult> Create(CreateAnnouncementRequest request, CancellationToken ct)
    {
        var identity = await Identity(ct);
        if (identity is null) return Unauthorized();
        string accommodationId = request.AccommodationId?.Trim() ?? string.Empty;
        if (!identity.IsAdmin && (identity.ManagerAccommodationId is null ||
            !identity.IsManagerFor(accommodationId))) return StatusCode(403);
        if (!Types.Contains(request.Type?.Trim() ?? string.Empty))
            return BadRequest(new { message = "Invalid notice type." });
        if (accommodationId.Length > 0 && await _accommodations.GetByIdAsync(accommodationId, ct) is null)
            return NotFound(new { message = "Accommodation not found." });
        var reference = _db.Collection("announcements").Document();
        var record = new Announcement {
            Id = reference.Id, AccommodationId = accommodationId, PostedBy = identity.Uid,
            Type = request.Type.Trim(), Title = request.Title.Trim(),
            Description = request.Description.Trim(), CreatedAtUtc = DateTime.UtcNow
        };
        await reference.SetAsync(record, cancellationToken: ct);
        // FCM delivery is best effort and never reverses a successfully published notice.
        // Notify only residents of the accommodation or users who enabled platform alerts.
        try
        {
        IReadOnlyList<Student> subscribers;
        if (accommodationId.Length > 0)
        {
            var residentRecords = await _residents.GetByAccommodationIdAsync(accommodationId, ct);
            var residentUids = residentRecords.Where(r => r.IsActive)
                .Select(r => r.StudentId).ToHashSet(StringComparer.Ordinal);
            subscribers = (await _students.GetAllAsync(ct)).Where(s =>
                residentUids.Contains(s.FirebaseUid) && s.NotificationsEnabled && !s.IsDisabled).ToArray();
        }
        else
        {
            subscribers = (await _students.GetAllAsync(ct)).Where(s =>
                s.NotificationsEnabled && !s.IsDisabled).ToArray();
        }
        foreach (Student recipient in subscribers)
        {
            await _notifier.NotifyAsync(recipient.FirebaseUid, record.Title, record.Description[..Math.Min(120, record.Description.Length)]);
        }
        }
        catch (Exception)
        {
            // The notice is already saved. A notification failure must not undo publication.
        }
        return StatusCode(StatusCodes.Status201Created, record);
    }

    [HttpGet]
    public async Task<IActionResult> Visible(CancellationToken ct)
    {
        var identity = await Identity(ct);
        if (identity is null) return Unauthorized();
        // Firestore OR queries need extra indexes; keep audience filtering on the server.
        var snapshot = await _db.Collection("announcements").GetSnapshotAsync(ct);
        var notices = snapshot.Documents.Select(d => d.ConvertTo<Announcement>())
            .Where(n => identity.IsAdmin || string.IsNullOrWhiteSpace(n.AccommodationId) ||
                identity.IsManagerFor(n.AccommodationId) ||
                (identity.Resident is not null && identity.Resident.AccommodationId == n.AccommodationId))
            .OrderByDescending(n => n.CreatedAtUtc).Take(100).ToArray();
        return Ok(notices);
    }
}
