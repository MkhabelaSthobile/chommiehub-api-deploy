using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    // Represents one accommodation a student has shortlisted.
    [FirestoreData]
    public sealed class Shortlist
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string StudentId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string AccommodationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public DateTime DateAddedUtc { get; set; } = DateTime.UtcNow;
    }
}
