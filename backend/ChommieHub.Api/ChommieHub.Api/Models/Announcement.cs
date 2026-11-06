using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models;

/// <summary>
/// A manager notice for one accommodation, or an administrator's platform-wide notice.
/// An empty AccommodationId identifies a platform-wide announcement.
/// </summary>
[FirestoreData]
public sealed class Announcement
{
    [FirestoreDocumentId] public string Id { get; set; } = string.Empty;
    [FirestoreProperty] public string AccommodationId { get; set; } = string.Empty;
    [FirestoreProperty] public string PostedBy { get; set; } = string.Empty;
    [FirestoreProperty] public string Type { get; set; } = string.Empty;
    [FirestoreProperty] public string Title { get; set; } = string.Empty;
    [FirestoreProperty] public string Description { get; set; } = string.Empty;
    [FirestoreProperty] public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;
}
