using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides API endpoints for resident visitor requests
    /// and manager visitor request management.
    /// </summary>
    [ApiController]
    [Route("api/visitors")]
    public sealed class VisitorRequestController : ControllerBase
    {
        private readonly IVisitorRequestService _visitorRequestService;

        public VisitorRequestController(
            IVisitorRequestService visitorRequestService)
        {
            _visitorRequestService = visitorRequestService;
        }

        /// <summary>
        /// Creates a new visitor request for a resident.
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<VisitorRequestResponse>> CreateAsync(
            [FromBody] CreateVisitorRequest request,
            CancellationToken cancellationToken)
        {
            try
            {
                VisitorRequestResponse created =
                    await _visitorRequestService.CreateAsync(
                        request,
                        cancellationToken);

                return CreatedAtAction(
                    nameof(GetByResidentAsync),
                    new { residentId = created.ResidentId },
                    created);
            }
            catch (InvalidOperationException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }

        /// <summary>
        /// Returns visitor requests submitted by a specific resident.
        /// </summary>
        [HttpGet("resident/{residentId}")]
        public async Task<ActionResult<IReadOnlyList<VisitorRequestResponse>>> GetByResidentAsync(
            string residentId,
            CancellationToken cancellationToken)
        {
            try
            {
                IReadOnlyList<VisitorRequestResponse> requests =
                    await _visitorRequestService.GetByResidentIdAsync(
                        residentId,
                        cancellationToken);

                return Ok(requests);
            }
            catch (InvalidOperationException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }

        /// <summary>
        /// Returns visitor requests for a specific accommodation.
        /// Used by residence managers.
        /// </summary>
        [HttpGet("accommodation/{accommodationId}")]
        public async Task<ActionResult<IReadOnlyList<VisitorRequestResponse>>> GetByAccommodationAsync(
            string accommodationId,
            CancellationToken cancellationToken)
        {
            try
            {
                IReadOnlyList<VisitorRequestResponse> requests =
                    await _visitorRequestService.GetByAccommodationIdAsync(
                        accommodationId,
                        cancellationToken);

                return Ok(requests);
            }
            catch (InvalidOperationException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }

        /// <summary>
        /// Updates the status of an existing visitor request.
        /// </summary>
        [HttpPatch("{id}/status")]
        public async Task<ActionResult<VisitorRequestResponse>> UpdateStatusAsync(
            string id,
            [FromBody] UpdateVisitorRequestStatusRequest request,
            CancellationToken cancellationToken)
        {
            try
            {
                VisitorRequestResponse? updated =
                    await _visitorRequestService.UpdateStatusAsync(
                        id,
                        request,
                        cancellationToken);

                if (updated is null)
                {
                    return NotFound(new
                    {
                        message = "Visitor request was not found."
                    });
                }

                return Ok(updated);
            }
            catch (InvalidOperationException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }
    }
}