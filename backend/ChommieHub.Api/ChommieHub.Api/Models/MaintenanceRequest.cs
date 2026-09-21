using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    /// <summary>
    /// Represents a maintenance request submitted by a resident.
    /// </summary>
    [FirestoreData]
    public sealed class MaintenanceRequest
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string ResidentId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string AccommodationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string RoomId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Category { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Description { get; set; } = string.Empty;

        [FirestoreProperty]
        public string? PhotoUrl { get; set; }

        [FirestoreProperty]
        public string Status { get; set; } = "Pending";

        [FirestoreProperty]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;

        [FirestoreProperty]
        public DateTime? UpdatedAtUtc { get; set; }
    }
}