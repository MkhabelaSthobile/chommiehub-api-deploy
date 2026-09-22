namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Represents a residence manager's profile as returned to Android clients.
    /// Mirrors the fields consumed by ManagerResponse.kt on the Android side.
    /// </summary>
    public sealed class ManagerResponse
    {
        public string Id { get; init; } = string.Empty;

        public string FirebaseUid { get; init; } = string.Empty;

        public string Name { get; init; } = string.Empty;

        public string Surname { get; init; } = string.Empty;

        public string Email { get; init; } = string.Empty;

        public string PhoneNumber { get; init; } = string.Empty;

        public string AccommodationId { get; init; } = string.Empty;

        public DateTime CreatedAtUtc { get; init; }

        public DateTime UpdatedAtUtc { get; init; }
    }
}
