using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services.Interfaces
{
    /// <summary>
    /// Defines the weather information required by ChommieHub residence operations.
    /// </summary>
    public interface IWeatherService
    {
        Task<WeatherResponse> GetCurrentAsync(string accommodationId, string accommodationName, double latitude, double longitude, CancellationToken cancellationToken = default);
    }
}