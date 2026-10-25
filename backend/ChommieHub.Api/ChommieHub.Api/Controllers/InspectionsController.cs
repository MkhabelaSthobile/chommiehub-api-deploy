
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// REST API endpoints for authenticated resident digital
    /// inspections and manager confirmation.
    /// </summary>
    [ApiController]
    [Route("api/inspections")]
    public sealed class InspectionsController : ControllerBase
    {
        private readonly IInspectionService _service;
        private readonly IInspectionRepository _inspectionRepository;
        private readonly IResidentRepository _residentRepository;
        private readonly IManagerAccountService _managerAccountService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;
        private readonly ILogger<InspectionsController> _logger;


        public InspectionsController(
            IInspectionService service,
            IInspectionRepository inspectionRepository,
            IResidentRepository residentRepository,
            IManagerAccountService managerAccountService,
            FirebaseTokenVerificationService tokenVerifier,
            ILogger<InspectionsController> logger)
        {
            _service = service;
            _inspectionRepository = inspectionRepository;
            _residentRepository = residentRepository;
            _managerAccountService = managerAccountService;
            _tokenVerifier = tokenVerifier;
            _logger = logger;
        }


        /// <summary>
        /// Verifies the Firebase ID token and returns the
        /// authenticated user's Firebase UID.
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
        /// Returns the authenticated student's active resident record.
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
        /// Checks whether the authenticated user manages
        /// the specified accommodation.
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
        /// Retrieves a resident record belonging to an accommodation.
        /// This supports manager authorization for resident inspections.
        /// </summary>
        private async Task<Resident?> GetResidentInAccommodationAsync(
            string residentId,
            string accommodationId,
            CancellationToken cancellationToken)
        {
            IReadOnlyList<Resident> residents =
                await _residentRepository.GetByAccommodationIdAsync(
                    accommodationId,
                    cancellationToken);

            return residents.FirstOrDefault(resident =>
                string.Equals(
                    resident.Id,
                    residentId,
                    StringComparison.Ordinal));
        }

        /// <summary>
        /// Creates an inspection for the authenticated student's
        /// own active resident record.
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<InspectionResponse>> Create(
            CreateInspectionRequest request,
            CancellationToken cancellationToken)
        {
            var (userId, error) =
                await GetAuthenticatedUserAsync();

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
                    message = "You can only create inspections for your own active residence."
                });
            }


            // Ensure the inspection belongs to the authenticated student
            // and matches their actual accommodation and room.
            if (!string.Equals(
                    request.StudentId,
                    userId,
                    StringComparison.Ordinal) ||
                !string.Equals(
                    request.AccommodationId,
                    resident.AccommodationId,
                    StringComparison.Ordinal) ||
                !string.Equals(
                    request.RoomId,
                    resident.RoomId,
                    StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "Inspection student, accommodation and room must match your active residence."
                });
            }


            try
            {
                InspectionResponse inspection =
                    await _service.CreateAsync(
                        request,
                        cancellationToken);

                _logger.LogInformation(
                    "Inspection {InspectionId} created for resident {ResidentId}.",
                    inspection.Id,
                    inspection.ResidentId);

                return Created(
                    $"/api/inspections/{inspection.Id}",
                    inspection);
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
        /// Returns inspections for the authenticated student's
        /// resident record or the accommodation's manager.
        /// </summary>
        [HttpGet("resident/{residentId}")]
        public async Task<ActionResult<IReadOnlyList<InspectionResponse>>>
            GetByResident(
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
                        message = "You cannot view this resident's inspections."
                    });
                }

                Resident? resident =
                    await GetResidentInAccommodationAsync(
                        residentId,
                        manager.AccommodationId,
                        cancellationToken);

                if (resident is null)
                {
                    return StatusCode(403, new
                    {
                        message = "You cannot view this resident's inspections."
                    });
                }
            }

            try
            {
                IReadOnlyList<InspectionResponse> inspections =
                    await _service.GetByResidentIdAsync(
                        residentId,
                        cancellationToken);

                return Ok(inspections);
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
        /// Returns inspections for the authenticated manager's
        /// assigned accommodation.
        /// </summary>
        [HttpGet("accommodation/{accommodationId}")]
        public async Task<ActionResult<IReadOnlyList<InspectionResponse>>>
            GetByAccommodation(
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
                    message = "Only the assigned accommodation manager can view these inspections."
                });
            }

            try
            {
                IReadOnlyList<InspectionResponse> inspections =
                    await _service.GetByAccommodationIdAsync(
                        accommodationId,
                        cancellationToken);

                return Ok(inspections);
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
        /// Confirms an existing inspection.
        /// Only the manager assigned to the inspection's
        /// accommodation may confirm it.
        /// </summary>
        [HttpPatch("{id}/confirm")]
        public async Task<ActionResult<InspectionResponse>> Confirm(
    string id,
    ConfirmInspectionRequest request,
    CancellationToken cancellationToken)
        {
            var (userId, error) =
                await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            // Load the existing inspection before making changes.
            Inspection? existingInspection =
                await _inspectionRepository.GetByIdAsync(
                    id,
                    cancellationToken);

            if (existingInspection is null)
            {
                return NotFound(new
                {
                    message = "Inspection was not found."
                });
            }

            // Verify that the caller manages the accommodation
            // associated with this inspection.
            bool isManager =
                await IsAccommodationManagerAsync(
                    userId!,
                    existingInspection.AccommodationId,
                    cancellationToken);

            if (!isManager)
            {
                return StatusCode(403, new
                {
                    message = "Only the assigned accommodation manager can confirm this inspection."
                });
            }

            try
            {
                InspectionResponse? inspection =
                    await _service.ConfirmAsync(
                        id,
                        request,
                        cancellationToken);

                if (inspection is null)
                {
                    return NotFound(new
                    {
                        message = "Inspection was not found."
                    });
                }

                _logger.LogInformation(
                    "Inspection {InspectionId} confirmed by residence management.",
                    id);

                return Ok(inspection);
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
