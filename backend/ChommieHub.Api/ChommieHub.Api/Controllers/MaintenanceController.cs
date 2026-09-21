using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides API endpoints for resident maintenance requests
    /// and manager maintenance management.
    /// </summary>
    [ApiController]
    [Route("api/maintenance")]
    public sealed class MaintenanceController : ControllerBase
    {
        private readonly IMaintenanceService _maintenanceService;

        public MaintenanceController(IMaintenanceService maintenanceService)
        {
            _maintenanceService = maintenanceService;
        }

        /// <summary>
        /// Creates a new maintenance request for a resident.
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<MaintenanceRequestResponse>> CreateAsync(
            [FromBody] CreateMaintenanceRequest request,
            CancellationToken cancellationToken)
        {
            try
            {
                MaintenanceRequestResponse created =
                    await _maintenanceService.CreateAsync(
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
        /// Returns maintenance requests submitted by a specific resident.
        /// </summary>
        [HttpGet("resident/{residentId}")]
        public async Task<ActionResult<IReadOnlyList<MaintenanceRequestResponse>>> GetByResidentAsync(
            string residentId,
            CancellationToken cancellationToken)
        {
            try
            {
                IReadOnlyList<MaintenanceRequestResponse> requests =
                    await _maintenanceService.GetByResidentIdAsync(
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
        /// Returns maintenance requests belonging to an accommodation.
        /// Used by residence managers.
        /// </summary>
        [HttpGet("accommodation/{accommodationId}")]
        public async Task<ActionResult<IReadOnlyList<MaintenanceRequestResponse>>> GetByAccommodationAsync(
            string accommodationId,
            CancellationToken cancellationToken)
        {
            try
            {
                IReadOnlyList<MaintenanceRequestResponse> requests =
                    await _maintenanceService.GetByAccommodationIdAsync(
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
        /// Updates the status of an existing maintenance request.
        /// </summary>
        [HttpPatch("{id}/status")]
        public async Task<ActionResult<MaintenanceRequestResponse>> UpdateStatusAsync(
            string id,
            [FromBody] UpdateMaintenanceStatusRequest request,
            CancellationToken cancellationToken)
        {
            try
            {
                MaintenanceRequestResponse? updated =
                    await _maintenanceService.UpdateStatusAsync(
                        id,
                        request,
                        cancellationToken);

                if (updated is null)
                {
                    return NotFound(new
                    {
                        message = "Maintenance request was not found."
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