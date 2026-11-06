using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System.Text;

namespace ChommieHub.Api.Controllers;

/// <summary>
/// Live, role-scoped operational summaries for managers and administrators.
/// Exposes aggregate counts, not student personal records or sensitive incidents.
/// </summary>
[ApiController]
[Route("api/reports")]
public sealed class ReportsController : ControllerBase
{
    private readonly CommunityAccess _access;
    private readonly IAccommodationRepository _accommodations;
    private readonly IApplicationRepository _applications;
    private readonly IResidentRepository _residents;
    private readonly FirestoreDb _db;

    public ReportsController(CommunityAccess access, IAccommodationRepository accommodations,
        IApplicationRepository applications, IResidentRepository residents, FirestoreDb db)
    { _access = access; _accommodations = accommodations; _applications = applications;
      _residents = residents; _db = db; }

    private async Task<(OperationalReport? Report, IActionResult? Error)> ResolveReport(CancellationToken ct)
    {
        var identity = await _access.ResolveAsync(Request.Headers.Authorization.ToString(), ct);
        if (identity is null) return (null, Unauthorized());
        if (!identity.IsAdmin && identity.ManagerAccommodationId is null)
            return (null, StatusCode(403, new { message = "Manager or administrator access required." }));
        var listings = (await _accommodations.GetAllAsync(ct)).AsEnumerable();
        var applications = (await _applications.GetAllAsync(ct)).AsEnumerable();
        if (!identity.IsAdmin)
        {
            listings = listings.Where(l => l.Id == identity.ManagerAccommodationId);
            applications = applications.Where(a => a.AccommodationId == identity.ManagerAccommodationId);
        }
        var listingList = listings.ToArray();
        var applicationList = applications.ToArray();
        // Managers can only count residents and incidents belonging to their own listing.
        int residentCount = 0;
        if (identity.IsAdmin)
        {
            var residentSnapshot = await _db.Collection("residents").GetSnapshotAsync(ct);
            residentCount = residentSnapshot.Documents.Count(d => d.ConvertTo<Resident>().IsActive);
        }
        else
        {
            residentCount = (await _residents.GetByAccommodationIdAsync(identity.ManagerAccommodationId!, ct))
                .Count(r => r.IsActive);
        }
        var incidentSnapshot = await _db.Collection("incidentReports").GetSnapshotAsync(ct);
        var incidents = incidentSnapshot.Documents.Select(d => d.ConvertTo<IncidentReport>())
            .Where(i => identity.IsAdmin || i.AccommodationId == identity.ManagerAccommodationId).ToArray();
        var announcementSnapshot = await _db.Collection("announcements").GetSnapshotAsync(ct);
        var notices = announcementSnapshot.Documents.Select(d => d.ConvertTo<Announcement>())
            .Where(n => identity.IsAdmin || n.AccommodationId == identity.ManagerAccommodationId).ToArray();
        return (new OperationalReport(
            identity.IsAdmin ? "Platform" : "Accommodation", DateTime.UtcNow,
            listingList.Length,
            listingList.Count(l => l.ReviewStatus == ListingStatuses.Pending),
            listingList.Count(l => l.IsVisibleToStudents),
            residentCount, applicationList.Length,
            applicationList.Count(a => a.Status == "Submitted" || a.Status == "Under Review"),
            incidents.Length, incidents.Count(i => i.Status != "Completed"), notices.Length), null);
    }

    [HttpGet("summary")]
    public async Task<IActionResult> Summary(CancellationToken ct)
    {
        var (report, error) = await ResolveReport(ct);
        return error ?? Ok(report);
    }

    [HttpGet("summary.csv")]
    public async Task<IActionResult> Csv(CancellationToken ct)
    {
        var (report, error) = await ResolveReport(ct);
        if (error is not null) return error;
        var csv = new StringBuilder("Metric,Value\r\n");
        void Row(string key, int value) => csv.Append(key).Append(',').Append(value).Append("\r\n");
        Row("Listings", report!.Listings);
        Row("Pending listings", report.PendingListings);
        Row("Approved listings", report.ApprovedListings);
        Row("Active residents", report.ActiveResidents);
        Row("Applications", report.Applications);
        Row("Open applications", report.OpenApplications);
        Row("Incidents", report.Incidents);
        Row("Open incidents", report.OpenIncidents);
        Row("Announcements", report.Announcements);
        return File(Encoding.UTF8.GetBytes(csv.ToString()), "text/csv", "chommiehub-summary.csv");
    }
}

/// <summary>Counts calculated for the requester's authorized accommodation scope.</summary>
public sealed record OperationalReport(string Scope, DateTime GeneratedAtUtc, int Listings,
    int PendingListings, int ApprovedListings, int ActiveResidents, int Applications,
    int OpenApplications, int Incidents, int OpenIncidents, int Announcements);
