using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    /// <summary>
    /// Represents a ChommieHub student profile linked to Firebase Authentication.
    /// </summary>
    [FirestoreData]
    public sealed class Student
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string FirebaseUid { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Name { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Surname { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Email { get; set; } = string.Empty;

        [FirestoreProperty]
        public string PhoneNumber { get; set; } = string.Empty;

        [FirestoreProperty]
        public string PreferredLanguage { get; set; } = "English";

        [FirestoreProperty]
        public bool NotificationsEnabled { get; set; } = true;

        [FirestoreProperty]
        public string UserType { get; set; } = "Applicant";

        [FirestoreProperty]
        public string AccommodationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string RoomId { get; set; } = string.Empty;

        [FirestoreProperty]
        public bool IsDisabled { get; set; }

        [FirestoreProperty]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;

        [FirestoreProperty]
        public DateTime UpdatedAtUtc { get; set; } = DateTime.UtcNow;
    }
}