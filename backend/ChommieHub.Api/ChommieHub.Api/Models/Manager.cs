using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    /// <summary>
    /// Represents a residence manager profile linked to Firebase Authentication.
    /// Mirrors the Student identity contract: the Firebase UID is preserved as
    /// Manager.Id, so the two live in separate Firestore collections but are
    /// looked up the same way.
    /// </summary>
    [FirestoreData]
    public sealed class Manager
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

        /// <summary>
        /// The accommodation this manager administers. Until the administrator
        /// role is built, this is set from the accommodation ID the manager
        /// registers against (see CreateManagerRequest); an admin workflow can
        /// take over assigning/reassigning it later.
        /// </summary>
        [FirestoreProperty]
        public string AccommodationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;

        [FirestoreProperty]
        public DateTime UpdatedAtUtc { get; set; } = DateTime.UtcNow;
    }
}
