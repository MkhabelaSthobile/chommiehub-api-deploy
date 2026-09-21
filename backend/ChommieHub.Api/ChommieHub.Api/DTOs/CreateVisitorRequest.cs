namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Data required when a resident submits a visitor request.
    /// </summary>
    public sealed class CreateVisitorRequest
    {
        public string ResidentId { get; set; } = string.Empty;
        public string AccommodationId { get; set; } = string.Empty;
        public string VisitorName { get; set; } = string.Empty;
        public string VisitorIdentification { get; set; } = string.Empty;
        public DateTime ArrivalDateUtc { get; set; }
        public DateTime DepartureDateUtc { get; set; }
        public bool IsSleepover { get; set; }
    }
}