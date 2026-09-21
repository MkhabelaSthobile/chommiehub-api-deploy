using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    /// <summary>
    /// Represents a visitor request submitted by a resident.
    /// </summary>
    [FirestoreData]
    public sealed class VisitorRequest
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string ResidentId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string AccommodationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string VisitorName { get; set; } = string.Empty;

        [FirestoreProperty]
        public string VisitorIdentification { get; set; } = string.Empty;

        [FirestoreProperty]
        public DateTime ArrivalDateUtc { get; set; }

        [FirestoreProperty]
        public DateTime DepartureDateUtc { get; set; }

        [FirestoreProperty]
        public bool IsSleepover { get; set; }

        [FirestoreProperty]
        public string Status { get; set; } = VisitorRequestStatuses.Pending;

        [FirestoreProperty]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;

        [FirestoreProperty]
        public DateTime? ReviewedAtUtc { get; set; }
    }
}