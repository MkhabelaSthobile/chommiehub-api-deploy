
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    // Exposes residence-manager operations including dashboard statistics,
    // application review, room assignment and resident retrieval.
    
    [ApiController]
    [Route("api/manager")]
    public sealed class ManagerController : ControllerBase
    {
        private readonly FirebaseTokenVerificationService _tokenVerifier;
        private readonly IManagerAccountService _managerAccountService;
        private readonly IManagerService _managerService;
        private readonly ILogger<ManagerController> _logger;

        public ManagerController(
            IManagerService managerService,
            IManagerAccountService managerAccountService,
            FirebaseTokenVerificationService tokenVerifier,
            ILogger<ManagerController> logger)
        {
            _managerService = managerService;
            _managerAccountService = managerAccountService;
            _tokenVerifier = tokenVerifier;
            _logger = logger;
        }

        // Retrieves the authenticated manager's profile using
        // their verified Firebase UID.
        private async Task<(ManagerResponse? Manager, ActionResult? Error)>
            GetAuthenticatedManagerAsync(
                CancellationToken cancellationToken)
        {
            string authorization =
                Request.Headers.Authorization.ToString();

            if (string.IsNullOrWhiteSpace(authorization) ||
                !authorization.StartsWith(
                    "Bearer ",
                    StringComparison.OrdinalIgnoreCase))
            {
                return (null, Unauthorized(new
                {
                    message = "A Firebase ID token is required."
                }));
            }

            string idToken =
                authorization["Bearer ".Length..].Trim();

            if (string.IsNullOrWhiteSpace(idToken))
            {
                return (null, Unauthorized(new
                {
                    message = "A Firebase ID token is required."
                }));
            }

            string? firebaseUid =
                await _tokenVerifier.VerifyUserIdAsync(idToken);

            if (string.IsNullOrWhiteSpace(firebaseUid))
            {
                return (null, Unauthorized(new
                {
                    message = "Invalid or revoked Firebase ID token."
                }));
            }

            ManagerResponse? manager =
                await _managerAccountService.GetByIdAsync(
                    firebaseUid,
                    cancellationToken);

            if (manager is null ||
                !string.Equals(
                    manager.FirebaseUid,
                    firebaseUid,
                    StringComparison.Ordinal) ||
                string.IsNullOrWhiteSpace(manager.AccommodationId))
            {
                return (null, StatusCode(403, new
                {
                    message = "An authorized residence manager is required."
                }));
            }

            return (manager, null);
        }

        /// <summary>
        /// Ensures the manager is assigned to the requested accommodation.
        /// </summary>
        private ActionResult? CheckAccommodationAccess(
            ManagerResponse manager,
            string accommodationId)
        {
            if (!string.Equals(
                manager.AccommodationId,
                accommodationId,
                StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "You cannot access this accommodation."
                });
            }

            return null;
        }

        /// <summary>
        /// Returns dashboard statistics for the manager's accommodation.
        /// </summary>
        [HttpGet("accommodations/{accommodationId}/dashboard")]
        public async Task<ActionResult<ManagerDashboardResponse>> GetDashboard(
            string accommodationId,
            CancellationToken cancellationToken)
        {
            var (manager, error) =
                await GetAuthenticatedManagerAsync(cancellationToken);

            if (error is not null)
            {
                return error;
            }

            ActionResult? accessError =
                CheckAccommodationAccess(manager!, accommodationId);

            if (accessError is not null)
            {
                return accessError;
            }

            ManagerDashboardResponse dashboard =
                await _managerService.GetDashboardAsync(
                    accommodationId,
                    cancellationToken);

            return Ok(dashboard);
        }

        /// <summary>
        /// Returns applications for the manager's accommodation.
        /// </summary>
        [HttpGet("accommodations/{accommodationId}/applications")]
        public async Task<ActionResult<IReadOnlyList<ManagerApplicationSummaryResponse>>>
            GetApplications(
                string accommodationId,
                CancellationToken cancellationToken)
        {
            var (manager, error) =
                await GetAuthenticatedManagerAsync(cancellationToken);

            if (error is not null)
            {
                return error;
            }

            ActionResult? accessError =
                CheckAccommodationAccess(manager!, accommodationId);

            if (accessError is not null)
            {
                return accessError;
            }

            IReadOnlyList<ManagerApplicationSummaryResponse> applications =
                await _managerService.GetApplicationsAsync(
                    accommodationId,
                    cancellationToken);

            return Ok(applications);
        }

        /// <summary>
        /// Returns application details only if the manager
        /// belongs to the application's accommodation.
        /// </summary>
        [HttpGet("applications/{applicationId}")]
        public async Task<ActionResult<ManagerApplicationDetailResponse>>
            GetApplication(
                string applicationId,
                CancellationToken cancellationToken)
        {
            var (manager, error) =
                await GetAuthenticatedManagerAsync(cancellationToken);

            if (error is not null)
            {
                return error;
            }

            ManagerApplicationDetailResponse? application =
                await _managerService.GetApplicationAsync(
                    applicationId,
                    cancellationToken);

            if (application is null)
            {
                return NotFound(new
                {
                    message = "Application was not found."
                });
            }

            ActionResult? accessError =
                CheckAccommodationAccess(
                    manager!,
                    application.AccommodationId);

            if (accessError is not null)
            {
                return accessError;
            }

            return Ok(application);
        }

        /// <summary>
        /// Records an authorized manager's approval or decline
        /// decision for an accommodation application.
        /// </summary>
        [HttpPatch("applications/{applicationId}/review")]
        public async Task<ActionResult<ManagerApplicationDetailResponse>>
            ReviewApplication(
                string applicationId,
                [FromBody] ReviewApplicationRequest request,
                CancellationToken cancellationToken)
        {
            var (manager, error) =
                await GetAuthenticatedManagerAsync(cancellationToken);

            if (error is not null)
            {
                return error;
            }

            ManagerApplicationDetailResponse? existingApplication =
                await _managerService.GetApplicationAsync(
                    applicationId,
                    cancellationToken);

            if (existingApplication is null)
            {
                return NotFound(new
                {
                    message = "Application was not found."
                });
            }

            ActionResult? accessError =
                CheckAccommodationAccess(
                    manager!,
                    existingApplication.AccommodationId);

            if (accessError is not null)
            {
                return accessError;
            }

            try
            {
                ManagerApplicationDetailResponse? application =
                    await _managerService.ReviewApplicationAsync(
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

                _logger.LogInformation(
                    "Application {ApplicationId} was reviewed with decision {Decision}.",
                    applicationId,
                    application.Status);

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
        /// Assigns an available room to an approved application
        /// and creates the corresponding resident record.
        /// </summary>
        [HttpPost("applications/{applicationId}/assign-room")]
        public async Task<ActionResult<ResidentResponse>> AssignRoom(
            string applicationId,
            [FromBody] AssignRoomRequest request,
            CancellationToken cancellationToken)
        {
            var (manager, error) =
                await GetAuthenticatedManagerAsync(cancellationToken);

            if (error is not null)
            {
                return error;
            }

            ManagerApplicationDetailResponse? existingApplication =
                await _managerService.GetApplicationAsync(
                    applicationId,
                    cancellationToken);

            if (existingApplication is null)
            {
                return NotFound(new
                {
                    message = "Application was not found."
                });
            }

            ActionResult? accessError =
                CheckAccommodationAccess(
                    manager!,
                    existingApplication.AccommodationId);

            if (accessError is not null)
            {
                return accessError;
            }

            try
            {
                ResidentResponse? resident =
                    await _managerService.AssignRoomAsync(
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

                _logger.LogInformation(
                    "Room {RoomId} was assigned for application {ApplicationId}.",
                    request.RoomId,
                    applicationId);

                return Created(
                    $"/api/manager/residents/{resident.Id}",
                    resident);
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

        /// <summary>
        /// Returns residents belonging to the manager's accommodation.
        /// </summary>
        [HttpGet("accommodations/{accommodationId}/residents")]
        public async Task<ActionResult<IReadOnlyList<ResidentResponse>>>
            GetResidents(
                string accommodationId,
                CancellationToken cancellationToken)
        {
            var (manager, error) =
                await GetAuthenticatedManagerAsync(cancellationToken);

            if (error is not null)
            {
                return error;
            }

            ActionResult? accessError =
                CheckAccommodationAccess(manager!, accommodationId);

            if (accessError is not null)
            {
                return accessError;
            }

            IReadOnlyList<ResidentResponse> residents =
                await _managerService.GetResidentsAsync(
                    accommodationId,
                    cancellationToken);

            return Ok(residents);
        }
    }
}
