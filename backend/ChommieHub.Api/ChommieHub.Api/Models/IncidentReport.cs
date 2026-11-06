using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models;

/// <summary>
/// A residence incident raised by an active resident. The linked student UID is
/// established by the server rather than accepted from a client-supplied identity.
/// </summary>
[FirestoreData]
public sealed class IncidentReport
{
    [FirestoreDocumentId] public string Id { get; set; } = string.Empty;
    [FirestoreProperty] public string StudentId { get; set; } = string.Empty;
    [FirestoreProperty] public string ResidentId { get; set; } = string.Empty;
    [FirestoreProperty] public string AccommodationId { get; set; } = string.Empty;
    [FirestoreProperty] public string Category { get; set; } = string.Empty;
    [FirestoreProperty] public string Description { get; set; } = string.Empty;
    [FirestoreProperty] public string Location { get; set; } = string.Empty;
    [FirestoreProperty] public string PhotoUrl { get; set; } = string.Empty;
    [FirestoreProperty] public string Status { get; set; } = "Submitted";
    [FirestoreProperty] public string ResolutionNote { get; set; } = string.Empty;
    [FirestoreProperty] public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;
    [FirestoreProperty] public DateTime? ResolvedAtUtc { get; set; }
    [FirestoreProperty] public DateTime? UpdatedAtUtc { get; set; }
}
