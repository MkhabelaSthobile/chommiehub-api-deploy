using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// REST API endpoints for resident digital inspections and manager confirmation.
    /// </summary>
    [ApiController]
    [Route("api/inspections")]
    public sealed class InspectionsController : ControllerBase
    {
        private readonly IInspectionService _service;
        private readonly ILogger<InspectionsController> _logger;

        public InspectionsController(IInspectionService service, ILogger<InspectionsController> logger)
        {
            _service = service;
            _logger = logger;
        }

        [HttpPost]
        public async Task<ActionResult<InspectionResponse>> Create(CreateInspectionRequest request, CancellationToken cancellationToken)
        {
            try
            {
                InspectionResponse inspection = await _service.CreateAsync(request, cancellationToken);

                _logger.LogInformation("Inspection {InspectionId} created for resident {ResidentId}.", inspection.Id, inspection.ResidentId);

                return Created($"/api/inspections/{inspection.Id}", inspection);
            }
            catch (InvalidOperationException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }

        [HttpGet("resident/{residentId}")]
        public async Task<ActionResult<IReadOnlyList<InspectionResponse>>> GetByResident(string residentId, CancellationToken cancellationToken)
        {
            try
            {
                return Ok(await _service.GetByResidentIdAsync(residentId, cancellationToken));
            }
            catch (InvalidOperationException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }

        [HttpGet("accommodation/{accommodationId}")]
        public async Task<ActionResult<IReadOnlyList<InspectionResponse>>> GetByAccommodation(string accommodationId, CancellationToken cancellationToken)
        {
            try
            {
                return Ok(await _service.GetByAccommodationIdAsync(accommodationId, cancellationToken));
            }
            catch (InvalidOperationException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }

        [HttpPatch("{id}/confirm")]
        public async Task<ActionResult<InspectionResponse>> Confirm(string id, ConfirmInspectionRequest request, CancellationToken cancellationToken)
        {
            try
            {
                InspectionResponse? inspection = await _service.ConfirmAsync(id, request, cancellationToken);

                if (inspection is null)
                {
                    return NotFound(new { message = "Inspection was not found." });
                }

                _logger.LogInformation("Inspection {InspectionId} confirmed by residence management.", id);

                return Ok(inspection);
            }
            catch (InvalidOperationException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }
    }
}