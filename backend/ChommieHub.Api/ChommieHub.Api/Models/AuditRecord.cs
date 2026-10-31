using Google.Cloud.Firestore;

namespace ChommieHub.Api.Models
{
    /// <summary>
    /// One administrator action: who did what, to whom, and why.
    /// </summary>
    [FirestoreData]
    public sealed class AuditRecord
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty]
        public string AdminId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Action { get; set; } = string.Empty;

        /// <summary>Accommodation, Student or Manager.</summary>
        [FirestoreProperty]
        public string TargetType { get; set; } = string.Empty;

        [FirestoreProperty]
        public string TargetId { get; set; } = string.Empty;

        [FirestoreProperty]
        public string TargetName { get; set; } = string.Empty;

        [FirestoreProperty]
        public string Reason { get; set; } = string.Empty;

        [FirestoreProperty]
        public DateTime CreatedAtUtc { get; set; } = DateTime.UtcNow;
    }
}