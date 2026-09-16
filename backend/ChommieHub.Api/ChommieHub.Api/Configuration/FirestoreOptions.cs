namespace ChommieHub.Api.Configuration
{
    /// <summary>
    /// Stores configuration values required to connect the ChommieHub API to Google Cloud Firestore
    /// </summary>
    public sealed class FirestoreOptions
    {
        public const string SectionName = "Firestore";

        public string ProjectId { get; set; } = string.Empty;
    }
}