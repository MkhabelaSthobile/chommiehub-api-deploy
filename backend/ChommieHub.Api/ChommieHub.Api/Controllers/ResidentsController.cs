using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Exposes resident information required by resident-facing Android features.
    /// </summary>
    [ApiController]
    [Route("api/residents")]
    public sealed class ResidentsController : ControllerBase
    {
        private readonly IManagerService _managerService;

        public ResidentsController(
     IManagerService managerService)
        {
            _managerService = managerService;
        }

        /// <summary>
        /// Retrieves the active resident record belonging to a student.
        /// </summary>
        [HttpGet("student/{studentId}")]
        public async Task<ActionResult<ResidentResponse>> GetByStudentId(
            string studentId,
            CancellationToken cancellationToken)
        {
            if (string.IsNullOrWhiteSpace(studentId))
            {
                return BadRequest(new
                {
                    message = "Student ID is required."
                });
            }

            ResidentResponse? resident =
                await _managerService.GetResidentByStudentIdAsync(
                    studentId,
                    cancellationToken);

            if (resident is null)
            {
                return NotFound(new
                {
                    message = "An active resident record was not found for this student."
                });
            }

            return Ok(resident);
        }
    }
}