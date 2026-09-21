namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Represents a student's profile as returned to Android clients. Mirrors the fields
    /// consumed by StudentResponse.kt on the Android side.
    /// </summary>
    public sealed class StudentResponse
    {
        public string Id { get; init; } = string.Empty;

        public string FirebaseUid { get; init; } = string.Empty;

        public string Name { get; init; } = string.Empty;

        public string Surname { get; init; } = string.Empty;

        public string Email { get; init; } = string.Empty;

        public string PhoneNumber { get; init; } = string.Empty;

        public string PreferredLanguage { get; init; } = string.Empty;

        public bool NotificationsEnabled { get; init; }

        public string UserType { get; init; } = string.Empty;

        public string AccommodationId { get; init; } = string.Empty;

        public string RoomId { get; init; } = string.Empty;

        public DateTime CreatedAtUtc { get; init; }

        public DateTime UpdatedAtUtc { get; init; }
    }
}
