using System.Net.Http.Json;
using System.Text.Json.Serialization;
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services.Interfaces;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Retrieves current weather information from Open-Meteo and converts the
    /// response into information relevant to student accommodation operations.
    ///
    /// Open-Meteo is used for the Part 2 prototype because it does not require
    /// a private API key and therefore does not introduce another secret into
    /// the Android application or GitHub repository.
    ///
    /// Reference:
    /// https://open-meteo.com/en/docs
    /// </summary>
    public sealed class WeatherService : IWeatherService
    {
        private readonly HttpClient _httpClient;
        private readonly ILogger<WeatherService> _logger;

        public WeatherService(HttpClient httpClient, ILogger<WeatherService> logger)
        {
            _httpClient = httpClient;
            _logger = logger;
        }

        public async Task<WeatherResponse> GetCurrentAsync(string accommodationId, string accommodationName, double latitude, double longitude, CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(accommodationId))
            {
                throw new ArgumentException("Accommodation ID is required.", nameof(accommodationId));
            }

            if (latitude is < -90 or > 90)
            {
                throw new ArgumentOutOfRangeException(nameof(latitude), "Latitude must be between -90 and 90.");
            }

            if (longitude is < -180 or > 180)
            {
                throw new ArgumentOutOfRangeException(nameof(longitude), "Longitude must be between -180 and 180.");
            }

            string url = FormattableString.Invariant($"https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m,precipitation,wind_speed_10m,weather_code&timezone=auto");

            OpenMeteoResponse? response = await _httpClient.GetFromJsonAsync<OpenMeteoResponse>(url, cancellationToken);

            if (response?.Current is null)
            {
                throw new InvalidOperationException("Weather information is currently unavailable.");
            }

            string summary = WeatherAdvisoryEngine.DescribeWeather(response.Current.WeatherCode);
            string advisory = WeatherAdvisoryEngine.BuildMaintenanceAdvisory(response.Current.Precipitation, response.Current.WindSpeed10m, response.Current.WeatherCode);

            _logger.LogInformation("Weather information loaded for accommodation {AccommodationId}.", accommodationId);

            return new WeatherResponse
            {
                AccommodationId = accommodationId,
                AccommodationName = accommodationName,
                TemperatureC = response.Current.Temperature2m,
                PrecipitationMm = response.Current.Precipitation,
                WindSpeedKph = response.Current.WindSpeed10m,
                WeatherCode = response.Current.WeatherCode,
                Summary = summary,
                OutdoorMaintenanceAdvisory = advisory,
                RetrievedAtUtc = DateTime.UtcNow
            };
        }

        private sealed class OpenMeteoResponse
        {
            [JsonPropertyName("current")]
            public OpenMeteoCurrent? Current { get; set; }
        }

        private sealed class OpenMeteoCurrent
        {
            [JsonPropertyName("temperature_2m")]
            public double Temperature2m { get; set; }

            [JsonPropertyName("precipitation")]
            public double Precipitation { get; set; }

            [JsonPropertyName("wind_speed_10m")]
            public double WindSpeed10m { get; set; }

            [JsonPropertyName("weather_code")]
            public int WeatherCode { get; set; }
        }
    }
}