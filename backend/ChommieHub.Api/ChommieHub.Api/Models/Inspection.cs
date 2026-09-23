using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    /// <summary>
    /// Represents a resident move-in or move-out room inspection stored in Firestore.
    /// PhotoReference stores the selected evidence reference.
    /// Dedicated cloud/blob photo storage.
    /// </summary>
    [FirestoreData]
    public sealed class Inspection
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string ResidentId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string StudentId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string AccommodationId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string RoomId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Type { get; set; } = string.Empty;

        [FirestoreProperty]
        public List<InspectionItem> Items { get; set; } = [];

        [FirestoreProperty]
        public string Status { get; set; } = "Submitted";

        [FirestoreProperty]
        public bool ManagerConfirmed { get; set; }

        [FirestoreProperty]
        public string ManagerNotes { get; set; } = string.Empty;

        [FirestoreProperty]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;

        [FirestoreProperty]
        public DateTime? ConfirmedAtUtc { get; set; }
    }

    /// <summary>
    /// Represents one room item recorded during an inspection.
    /// </summary>
    [FirestoreData]
    public sealed class InspectionItem
    {
        [FirestoreProperty]
        public string ItemName { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Condition { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Comments { get; set; } = string.Empty;

        [FirestoreProperty]
        public string PhotoReference { get; set; } = string.Empty;
    }
}