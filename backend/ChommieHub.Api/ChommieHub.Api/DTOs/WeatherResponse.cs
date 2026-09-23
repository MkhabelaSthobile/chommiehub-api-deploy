namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Represents current weather information for a ChommieHub accommodation.
    /// The maintenance advisory allows residence managers to consider weather conditions when planning outdoor maintenance work.
    /// </summary>
    public sealed class WeatherResponse
    {
        public string AccommodationId { get; init; } = string.Empty;
        public string AccommodationName { get; init; } = string.Empty;
        public double TemperatureC { get; init; }
        public double PrecipitationMm { get; init; }
        public double WindSpeedKph { get; init; }
        public int WeatherCode { get; init; }
        public string Summary { get; init; } = string.Empty;
        public string OutdoorMaintenanceAdvisory { get; init; } = string.Empty;
        public DateTime RetrievedAtUtc { get; init; }
    }
}