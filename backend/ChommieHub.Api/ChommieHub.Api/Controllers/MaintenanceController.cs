
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides authenticated API endpoints for resident maintenance
    /// requests and manager maintenance management.
    /// </summary>
    [ApiController]
    [Route("api/maintenance")]
    public sealed class MaintenanceController : ControllerBase
    {
        private readonly IMaintenanceService _maintenanceService;
        private readonly IMaintenanceRepository _maintenanceRepository;
        private readonly IResidentRepository _residentRepository;
        private readonly IManagerAccountService _managerAccountService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;

        public MaintenanceController(
            IMaintenanceService maintenanceService,
            IMaintenanceRepository maintenanceRepository,
            IResidentRepository residentRepository,
            IManagerAccountService managerAccountService,
            FirebaseTokenVerificationService tokenVerifier)
        {
            _maintenanceService = maintenanceService;
            _maintenanceRepository = maintenanceRepository;
            _residentRepository = residentRepository;
            _managerAccountService = managerAccountService;
            _tokenVerifier = tokenVerifier;
        }

        /// <summary>
        /// Verifies the Firebase bearer token and returns its UID.
        /// </summary>
        private async Task<(string? UserId, ActionResult? Error)>
            GetAuthenticatedUserAsync()
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

            string? userId =
                await _tokenVerifier.VerifyUserIdAsync(idToken);

            if (string.IsNullOrWhiteSpace(userId))
            {
                return (null, Unauthorized(new
                {
                    message = "Invalid or revoked Firebase ID token."
                }));
            }

            return (userId, null);
        }

        /// <summary>
        /// Verifies that a user manages the specified accommodation.
        /// </summary>
        private async Task<bool> IsAccommodationManagerAsync(
            string userId,
            string accommodationId,
            CancellationToken cancellationToken)
        {
            ManagerResponse? manager =
                await _managerAccountService.GetByIdAsync(
                    userId,
                    cancellationToken);

            return manager is not null &&
                string.Equals(
                    manager.FirebaseUid,
                    userId,
                    StringComparison.Ordinal) &&
                !string.IsNullOrWhiteSpace(manager.AccommodationId) &&
                string.Equals(
                    manager.AccommodationId,
                    accommodationId,
                    StringComparison.Ordinal);
        }

        /// <summary>
        /// Retrieves the authenticated student's active resident record.
        /// </summary>
        private Task<Resident?> GetActiveResidentAsync(
            string userId,
            CancellationToken cancellationToken)
        {
            return _residentRepository.GetByStudentIdAsync(
                userId,
                cancellationToken);
        }

        /// <summary>
        /// Creates a maintenance request for the authenticated student's
        /// own active residence and room.
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<MaintenanceRequestResponse>> CreateAsync(
            [FromBody] CreateMaintenanceRequest request,
            CancellationToken cancellationToken)
        {
            var (userId, error) = await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            Resident? resident =
                await GetActiveResidentAsync(
                    userId!,
                    cancellationToken);

            if (resident is null ||
                !resident.IsActive ||
                !string.Equals(
                    resident.Id,
                    request.ResidentId,
                    StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "You can only submit maintenance requests for your own active residence."
                });
            }

            if (!string.Equals(
                    resident.AccommodationId,
                    request.AccommodationId,
                    StringComparison.Ordinal) ||
                !string.Equals(
                    resident.RoomId,
                    request.RoomId,
                    StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "The accommodation or room does not match your resident record."
                });
            }

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
                return BadRequest(new
                {
                    message = exception.Message
                });
            }
        }

        /// <summary>
        /// Returns maintenance requests for the resident.
        /// Only the resident's student or their accommodation manager
        /// may view these requests.
        /// </summary>
        [HttpGet("resident/{residentId}")]
        public async Task<ActionResult<IReadOnlyList<MaintenanceRequestResponse>>>
            GetByResidentAsync(
                string residentId,
                CancellationToken cancellationToken)
        {
            var (userId, error) = await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            Resident? ownResident =
                await GetActiveResidentAsync(
                    userId!,
                    cancellationToken);

            bool ownsResident =
                ownResident is not null &&
                ownResident.IsActive &&
                string.Equals(
                    ownResident.Id,
                    residentId,
                    StringComparison.Ordinal);

            if (!ownsResident)
            {
                // A manager can read requests only for residents
                // belonging to their assigned accommodation.
                ManagerResponse? manager =
                    await _managerAccountService.GetByIdAsync(
                        userId!,
                        cancellationToken);

                if (manager is null ||
                    !string.Equals(
                        manager.FirebaseUid,
                        userId,
                        StringComparison.Ordinal) ||
                    string.IsNullOrWhiteSpace(manager.AccommodationId))
                {
                    return StatusCode(403, new
                    {
                        message = "You cannot view this resident's maintenance requests."
                    });
                }

                IReadOnlyList<Resident> residents =
                    await _residentRepository.GetByAccommodationIdAsync(
                        manager.AccommodationId,
                        cancellationToken);

                bool residentBelongsToAccommodation =
                    residents.Any(resident =>
                        string.Equals(
                            resident.Id,
                            residentId,
                            StringComparison.Ordinal));

                if (!residentBelongsToAccommodation)
                {
                    return StatusCode(403, new
                    {
                        message = "You cannot view this resident's maintenance requests."
                    });
                }
            }

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
                return BadRequest(new
                {
                    message = exception.Message
                });
            }
        }

        /// <summary>
        /// Returns maintenance requests for the authenticated
        /// manager's assigned accommodation.
        /// </summary>
        [HttpGet("accommodation/{accommodationId}")]
        public async Task<ActionResult<IReadOnlyList<MaintenanceRequestResponse>>>
            GetByAccommodationAsync(
                string accommodationId,
                CancellationToken cancellationToken)
        {
            var (userId, error) = await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            bool isManager =
                await IsAccommodationManagerAsync(
                    userId!,
                    accommodationId,
                    cancellationToken);

            if (!isManager)
            {
                return StatusCode(403, new
                {
                    message = "Only the assigned accommodation manager can view these requests."
                });
            }

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
                return BadRequest(new
                {
                    message = exception.Message
                });
            }
        }

        /// <summary>
        /// Updates a maintenance request's status.
        /// Only the manager assigned to its accommodation may do so.
        /// </summary>
        [HttpPatch("{id}/status")]
        public async Task<ActionResult<MaintenanceRequestResponse>>
            UpdateStatusAsync(
                string id,
                [FromBody] UpdateMaintenanceStatusRequest request,
                CancellationToken cancellationToken)
        {
            var (userId, error) = await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            MaintenanceRequest? existingRequest =
                await _maintenanceRepository.GetByIdAsync(
                    id,
                    cancellationToken);

            if (existingRequest is null)
            {
                return NotFound(new
                {
                    message = "Maintenance request was not found."
                });
            }

            bool isManager =
                await IsAccommodationManagerAsync(
                    userId!,
                    existingRequest.AccommodationId,
                    cancellationToken);

            if (!isManager)
            {
                return StatusCode(403, new
                {
                    message = "Only the assigned accommodation manager can update this request."
                });
            }

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
                return BadRequest(new
                {
                    message = exception.Message
                });
            }
        }
    }
}
