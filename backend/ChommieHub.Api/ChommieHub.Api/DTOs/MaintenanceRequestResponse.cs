namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Represents maintenance request information returned by the API.
    /// </summary>
    public sealed class MaintenanceRequestResponse
    {
        public string Id { get; set; } = string.Empty;
        public string ResidentId { get; set; } = string.Empty;
        public string AccommodationId { get; set; } = string.Empty;
        public string RoomId { get; set; } = string.Empty;
        public string Category { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
        public string? PhotoUrl { get; set; }
        public string Status { get; set; } = string.Empty;
        public DateTime CreatedAtUtc { get; set; }
        public DateTime? UpdatedAtUtc { get; set; }
    }
}