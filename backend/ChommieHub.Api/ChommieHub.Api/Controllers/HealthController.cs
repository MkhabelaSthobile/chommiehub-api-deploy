using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides a simple health endpoint used to verify that the ChommieHub REST API is running successfully.
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public class HealthController : ControllerBase
    {
        private readonly ILogger<HealthController> _logger;

        public HealthController(ILogger<HealthController> logger)
        {
            _logger = logger;
        }

        /// <summary>
        /// Returns the current health status of the API.
        /// </summary>
        [HttpGet]
        public IActionResult GetHealth()
        {
            _logger.LogInformation("ChommieHub API health check requested at {TimeUtc}", DateTime.UtcNow);

            return Ok(new
            {
                status = "Healthy",
                service = "ChommieHub.Api",
                timestampUtc = DateTime.UtcNow
            });
        }
    }
}