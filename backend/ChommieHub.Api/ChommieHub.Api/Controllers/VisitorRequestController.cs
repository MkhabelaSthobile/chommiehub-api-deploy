
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides authenticated visitor request submission,
    /// resident tracking, and manager review endpoints.
    /// </summary>
    [ApiController]
    [Route("api/visitors")]
    public sealed class VisitorRequestController : ControllerBase
    {
        private readonly IVisitorRequestService _visitorRequestService;
        private readonly IVisitorRequestRepository _visitorRequestRepository;
        private readonly IResidentRepository _residentRepository;
        private readonly IManagerAccountService _managerAccountService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;

        public VisitorRequestController(
            IVisitorRequestService visitorRequestService,
            IVisitorRequestRepository visitorRequestRepository,
            IResidentRepository residentRepository,
            IManagerAccountService managerAccountService,
            FirebaseTokenVerificationService tokenVerifier)
        {
            _visitorRequestService = visitorRequestService;
            _visitorRequestRepository = visitorRequestRepository;
            _residentRepository = residentRepository;
            _managerAccountService = managerAccountService;
            _tokenVerifier = tokenVerifier;
        }

        /// <summary>
        /// Verifies the Firebase ID token and returns the caller's UID.
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
        /// Checks whether the caller manages the specified accommodation.
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
        /// Checks whether a resident belongs to the manager's accommodation.
        /// </summary>
        private async Task<bool> IsResidentInAccommodationAsync(
            string residentId,
            string accommodationId,
            CancellationToken cancellationToken)
        {
            IReadOnlyList<Resident> residents =
                await _residentRepository.GetByAccommodationIdAsync(
                    accommodationId,
                    cancellationToken);

            return residents.Any(resident =>
                string.Equals(
                    resident.Id,
                    residentId,
                    StringComparison.Ordinal));
        }

        /// <summary>
        /// Creates a visitor request for the authenticated
        /// student's own active residence.
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<VisitorRequestResponse>> CreateAsync(
            [FromBody] CreateVisitorRequest request,
            CancellationToken cancellationToken)
        {
            var (userId, error) =
                await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            Resident? resident =
                await _residentRepository.GetByStudentIdAsync(
                    userId!,
                    cancellationToken);

            if (resident is null ||
                !resident.IsActive ||
                !string.Equals(
                    resident.Id,
                    request.ResidentId,
                    StringComparison.Ordinal) ||
                !string.Equals(
                    resident.AccommodationId,
                    request.AccommodationId,
                    StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "Visitor requests must belong to your own active residence."
                });
            }

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
                return BadRequest(new
                {
                    message = exception.Message
                });
            }
        }

        /// <summary>
        /// Returns visitor requests for the authenticated
        /// resident or the assigned accommodation manager.
        /// </summary>
        [HttpGet("resident/{residentId}")]
        public async Task<ActionResult<IReadOnlyList<VisitorRequestResponse>>>
            GetByResidentAsync(
                string residentId,
                CancellationToken cancellationToken)
        {
            var (userId, error) =
                await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            Resident? ownResident =
                await _residentRepository.GetByStudentIdAsync(
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
                ManagerResponse? manager =
                    await _managerAccountService.GetByIdAsync(
                        userId!,
                        cancellationToken);

                if (manager is null ||
                    !string.Equals(
                        manager.FirebaseUid,
                        userId,
                        StringComparison.Ordinal) ||
                    string.IsNullOrWhiteSpace(manager.AccommodationId) ||
                    !await IsResidentInAccommodationAsync(
                        residentId,
                        manager.AccommodationId,
                        cancellationToken))
                {
                    return StatusCode(403, new
                    {
                        message = "You cannot view this resident's visitor requests."
                    });
                }
            }

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
                return BadRequest(new
                {
                    message = exception.Message
                });
            }
        }

        /// <summary>
        /// Returns visitor requests for the assigned manager's
        /// accommodation only.
        /// </summary>
        [HttpGet("accommodation/{accommodationId}")]
        public async Task<ActionResult<IReadOnlyList<VisitorRequestResponse>>>
            GetByAccommodationAsync(
                string accommodationId,
                CancellationToken cancellationToken)
        {
            var (userId, error) =
                await GetAuthenticatedUserAsync();

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
                    message = "Only the assigned accommodation manager can view these visitor requests."
                });
            }

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
                return BadRequest(new
                {
                    message = exception.Message
                });
            }
        }

        /// <summary>
        /// Updates a visitor request's status.
        /// Only the manager assigned to its accommodation
        /// may approve or reject the request.
        /// </summary>
        [HttpPatch("{id}/status")]
        public async Task<ActionResult<VisitorRequestResponse>> UpdateStatusAsync(
            string id,
            [FromBody] UpdateVisitorRequestStatusRequest request,
            CancellationToken cancellationToken)
        {
            var (userId, error) =
                await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            // Retrieve the existing request before authorizing.
            // Never trust an accommodation ID supplied by the caller.
            VisitorRequest? existingRequest =
                await _visitorRequestRepository.GetByIdAsync(
                    id,
                    cancellationToken);

            if (existingRequest is null)
            {
                return NotFound(new
                {
                    message = "Visitor request was not found."
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
                    message = "Only the assigned accommodation manager can update this visitor request."
                });
            }

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
                return BadRequest(new
                {
                    message = exception.Message
                });
            }
        }
    }
}
