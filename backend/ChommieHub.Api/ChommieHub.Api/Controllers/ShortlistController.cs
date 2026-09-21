using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    // Exposes REST endpoints for adding, removing, and listing
    // a student's shortlisted accommodations.
    
    [ApiController]
    [Route("api/students/{studentId}/shortlist")]
    public sealed class ShortlistController : ControllerBase
    {
        private readonly IShortlistService _shortlistService;
        private readonly ILogger<ShortlistController> _logger;

        public ShortlistController(IShortlistService shortlistService, ILogger<ShortlistController> logger)
        {
            _shortlistService = shortlistService;
            _logger = logger;
        }

        [HttpGet]
        public async Task<ActionResult<IReadOnlyList<AccommodationResponse>>> GetShortlist(
            string studentId,
            CancellationToken cancellationToken)
        {
            IReadOnlyList<AccommodationResponse> shortlist = await _shortlistService
                .GetShortlistAsync(studentId, cancellationToken);

            return Ok(shortlist);
        }

        [HttpPost("{accommodationId}")]
        public async Task<IActionResult> AddToShortlist(
            string studentId,
            string accommodationId,
            CancellationToken cancellationToken)
        {
            await _shortlistService.AddAsync(studentId, accommodationId, cancellationToken);

            _logger.LogInformation("Student {StudentId} shortlisted accommodation {AccommodationId}.", studentId, accommodationId);

            return NoContent();
        }

        [HttpDelete("{accommodationId}")]
        public async Task<IActionResult> RemoveFromShortlist(
            string studentId,
            string accommodationId,
            CancellationToken cancellationToken)
        {
            await _shortlistService.RemoveAsync(studentId, accommodationId, cancellationToken);

            _logger.LogInformation("Student {StudentId} removed accommodation {AccommodationId} from shortlist.", studentId, accommodationId);

            return NoContent();
        }
    }
}