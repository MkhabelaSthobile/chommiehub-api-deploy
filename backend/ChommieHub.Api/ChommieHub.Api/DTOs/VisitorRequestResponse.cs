namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Represents visitor request information returned by the API.
    /// </summary>
    public sealed class VisitorRequestResponse
    {
        public string Id { get; set; } = string.Empty;
        public string ResidentId { get; set; } = string.Empty;
        public string AccommodationId { get; set; } = string.Empty;
        public string VisitorName { get; set; } = string.Empty;
        public string VisitorIdentification { get; set; } = string.Empty;
        public DateTime ArrivalDateUtc { get; set; }
        public DateTime DepartureDateUtc { get; set; }
        public bool IsSleepover { get; set; }
        public string Status { get; set; } = string.Empty;
        public DateTime CreatedAtUtc { get; set; }
        public DateTime? ReviewedAtUtc { get; set; }
    }
}