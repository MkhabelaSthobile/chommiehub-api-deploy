using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Exposes residence-manager operations including dashboard statistics, application review, room assignment and resident retrieval
    /// </summary>
    [ApiController]
    [Route("api/manager")]
    public sealed class ManagerController : ControllerBase
    {
        private readonly IManagerService _managerService;
        private readonly ILogger<ManagerController> _logger;

        public ManagerController(IManagerService managerService, ILogger<ManagerController> logger)
        {
            _managerService = managerService;
            _logger = logger;
        }

        [HttpGet("accommodations/{accommodationId}/dashboard")]
        public async Task<ActionResult<ManagerDashboardResponse>> GetDashboard(string accommodationId, CancellationToken cancellationToken)
        {
            ManagerDashboardResponse dashboard = await _managerService.GetDashboardAsync(
                accommodationId,
                cancellationToken);

            return Ok(dashboard);
        }

        [HttpGet("accommodations/{accommodationId}/applications")]
        public async Task<ActionResult<IReadOnlyList<ManagerApplicationSummaryResponse>>> GetApplications(string accommodationId, CancellationToken cancellationToken)
        {
            IReadOnlyList<ManagerApplicationSummaryResponse> applications = await _managerService.GetApplicationsAsync(
                accommodationId,
                cancellationToken);

            return Ok(applications);
        }

        [HttpGet("applications/{applicationId}")]
        public async Task<ActionResult<ManagerApplicationDetailResponse>> GetApplication(string applicationId, CancellationToken cancellationToken)
        {
            ManagerApplicationDetailResponse? application =  await _managerService.GetApplicationAsync(
                applicationId,
                cancellationToken);

            if (application is null)
            {
                return NotFound(new
                {
                    message = "Application was not found."
                });
            }

            return Ok(application);
        }

        /// <summary>
        ///Records a manager's approval or decline decision for an accommodation application.
        /// </summary>
        [HttpPatch("applications/{applicationId}/review")]
        public async Task<ActionResult<ManagerApplicationDetailResponse>> ReviewApplication(string applicationId, ReviewApplicationRequest request, CancellationToken cancellationToken)
        {
            try
            {
                ManagerApplicationDetailResponse? 
                    application = await _managerService.ReviewApplicationAsync(
                        applicationId,
                        request,
                         cancellationToken);

                if (application is null)
                {
                    return NotFound(new
                    {
                        message = "Application was not found."
                    });
                }

                _logger.LogInformation("Application {ApplicationId} was reviewed with decision {Decision}.", applicationId, request.Decision);

                return Ok(application);
            }
            catch (InvalidOperationException exception)
            {
                return Conflict(new
                {
                    message = exception.Message
                });
            }
        }

        /// <summary>
        /// Assigns an available room to an approved application and creates the corresponding resident record
        /// </summary>
        [HttpPost("applications/{applicationId}/assign-room")]
        public async Task<ActionResult<ResidentResponse>> AssignRoom(string applicationId, AssignRoomRequest request, CancellationToken cancellationToken)
        {
            try
            {
                ResidentResponse? resident = await _managerService.AssignRoomAsync(
                    applicationId,
                    request,
                    cancellationToken);

                if (resident is null)
                {
                    return NotFound(new
                    {
                        message = "Application was not found."
                    });
                }

                _logger.LogInformation("Room {RoomId} was assigned for application {ApplicationId}.", request.RoomId, applicationId);

                return Created($"/api/manager/residents/{resident.Id}", resident);
            }
            catch (KeyNotFoundException exception)
            {
                return NotFound(new
                {
                    message = exception.Message
                });
            }
            catch (InvalidOperationException exception)
            {
                return Conflict(new
                {
                    message = exception.Message
                });
            }
        }

        [HttpGet("accommodations/{accommodationId}/residents")]
        public async Task<ActionResult<IReadOnlyList<ResidentResponse>>> GetResidents(string accommodationId, CancellationToken cancellationToken)
        {
            IReadOnlyList<ResidentResponse> residents = await _managerService.GetResidentsAsync(
                accommodationId,
                cancellationToken);

            return Ok(residents);
        }
    }
}