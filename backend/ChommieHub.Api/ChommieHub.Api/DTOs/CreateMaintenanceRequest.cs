namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Data required when a resident submits a maintenance request.
    /// </summary>
    public sealed class CreateMaintenanceRequest
    {
        public string ResidentId { get; set; } = string.Empty;

        public string AccommodationId { get; set; } = string.Empty;

        public string RoomId { get; set; } = string.Empty;

        public string Category { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;

        public string? PhotoUrl { get; set; }
    }
}