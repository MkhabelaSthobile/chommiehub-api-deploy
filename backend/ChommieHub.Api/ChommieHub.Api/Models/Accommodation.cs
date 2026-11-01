using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    /// <summary>
    /// Represents an accommodation property available through ChommieHub
    /// </summary>
    [FirestoreData]
    public sealed class Accommodation
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Name { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Address { get; set; } = string.Empty;

        [FirestoreProperty]
        public double Latitude { get; set; }

        [FirestoreProperty]
        public double Longitude { get; set; }

        [FirestoreProperty]
        public string Description { get; set; } = string.Empty;

        [FirestoreProperty]
        public string PhoneNumber { get; set; } = string.Empty;

        [FirestoreProperty]
        public List<string> Amenities { get; set; } = [];

        [FirestoreProperty]
        public string InstitutionArea { get; set; } = string.Empty;

        [FirestoreProperty]
        public bool IsActive { get; set; } = true;

        [FirestoreProperty]
        public string ReviewStatus { get; set; } = ListingStatuses.Approved;

        [FirestoreProperty]
        public string ReviewReason { get; set; } = string.Empty;

        [FirestoreProperty]
        public string ReviewedByUid { get; set; } = string.Empty;

        [FirestoreProperty]
        public DateTime? SubmittedAtUtc { get; set; }

        [FirestoreProperty]
        public DateTime? ReviewedAtUtc { get; set; }

        /// <summary>Set by an administrator to hide a listing without deleting it.</summary>
        [FirestoreProperty]
        public bool IsArchived { get; set; }

        [FirestoreProperty]
        public List<string> ImageUrls { get; set; } = [];

        public bool IsVisibleToStudents => ReviewStatus == ListingStatuses.Approved && !IsArchived;

        [FirestoreProperty]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;

        [FirestoreProperty]
        public DateTime UpdatedAtUtc { get; set; } = DateTime.UtcNow;
    }
}