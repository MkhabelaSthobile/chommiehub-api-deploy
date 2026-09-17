using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    [FirestoreData]
    public sealed class Resident
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string StudentId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string AccommodationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string RoomId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string ApplicationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public bool IsActive { get; set; } = true;

        [FirestoreProperty]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;
    }
}