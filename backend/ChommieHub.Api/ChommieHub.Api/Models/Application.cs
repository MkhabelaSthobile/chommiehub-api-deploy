using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    [FirestoreData]
    public sealed class Application
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string StudentId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string AccommodationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string RoomTypePreference { get; set; } = string.Empty;

        [FirestoreProperty]
        public string RentalPeriod { get; set; } = string.Empty;

        [FirestoreProperty]
        public string SupportingInfo { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Status { get; set; } = ApplicationStatuses.Submitted;

        [FirestoreProperty]
        public string AssignedRoomId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string ReviewNotes { get; set; } = string.Empty;

        [FirestoreProperty]
        public DateTime DateSubmittedUtc { get; set; } = DateTime.UtcNow;

        [FirestoreProperty]
        public DateTime? DateReviewedUtc { get; set; }

        [FirestoreProperty]
        public DateTime UpdatedAtUtc { get; set; } = DateTime.UtcNow;
    }
}