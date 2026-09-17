namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Represents resident information returned to manager-facing API clients
    /// </summary>
    public sealed class ResidentResponse
    {
        public string Id { get; init; } = string.Empty;

        public string StudentId { get; init; } = string.Empty;

        public string AccommodationId { get; init; } = string.Empty;

        public string RoomId { get; init; } = string.Empty;

        public string ApplicationId { get; init; } = string.Empty;

        public bool IsActive { get; init; }

        public DateTime CreatedAtUtc { get; init; }
    }
}