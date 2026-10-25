
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Exposes weather-aware residence information through the
    /// ChommieHub REST API.
    ///
    /// Android communicates with this API rather than directly
    /// calling the third-party weather provider.
    /// </summary>
    [ApiController]
    [Route("api/weather")]
    public sealed class WeatherController : ControllerBase
    {
        private readonly IAccommodationService _accommodationService;
        private readonly IWeatherService _weatherService;
        private readonly ILogger<WeatherController> _logger;

        public WeatherController(
            IAccommodationService accommodationService,
            IWeatherService weatherService,
            ILogger<WeatherController> logger)
        {
            _accommodationService = accommodationService;
            _weatherService = weatherService;
            _logger = logger;
        }

        /// <summary>
        /// Returns current weather and an outdoor-maintenance
        /// advisory for an accommodation.
        /// </summary>
        [HttpGet("accommodation/{accommodationId}")]
        public async Task<ActionResult<WeatherResponse>> GetForAccommodation(
            string accommodationId,
            CancellationToken cancellationToken)
        {
            if (string.IsNullOrWhiteSpace(accommodationId))
            {
                return BadRequest(new
                {
                    message = "A valid accommodation ID is required."
                });
            }

            accommodationId = accommodationId.Trim();

            if (accommodationId.Length > 128)
            {
                return BadRequest(new
                {
                    message = "The accommodation ID is too long."
                });
            }

            AccommodationResponse? accommodation =
                await _accommodationService.GetByIdAsync(
                    accommodationId,
                    cancellationToken
                );

            if (accommodation is null)
            {
                return NotFound(new
                {
                    message = "Accommodation was not found."
                });
            }

            try
            {
                WeatherResponse weather =
                    await _weatherService.GetCurrentAsync(
                        accommodation.Id,
                        accommodation.Name,
                        accommodation.Latitude,
                        accommodation.Longitude,
                        cancellationToken
                    );

                _logger.LogInformation(
                    "Weather request completed for accommodation {AccommodationId}.",
                    accommodationId
                );

                return Ok(weather);
            }
            catch (HttpRequestException exception)
            {
                _logger.LogWarning(
                    exception,
                    "External weather service failed for accommodation {AccommodationId}.",
                    accommodationId
                );

                return StatusCode(
                    StatusCodes.Status503ServiceUnavailable,
                    new
                    {
                        message = "The weather service is currently unavailable."
                    }
                );
            }
            catch (InvalidOperationException exception)
            {
                _logger.LogWarning(
                    exception,
                    "Weather information was unavailable for accommodation {AccommodationId}.",
                    accommodationId
                );

                // Do not expose internal exception messages to clients.
                return StatusCode(
                    StatusCodes.Status503ServiceUnavailable,
                    new
                    {
                        message = "Weather information is currently unavailable."
                    }
                );
            }
        }
    }
}
