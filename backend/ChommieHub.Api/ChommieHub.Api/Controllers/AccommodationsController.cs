
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides public accommodation discovery endpoints and
    /// authenticated accommodation management endpoints.
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public sealed class AccommodationsController : ControllerBase
    {
        private readonly IAccommodationService _accommodationService;
        private readonly IRoomService _roomService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;
        private readonly IManagerAccountService _managerAccountService;
        private readonly ILogger<AccommodationsController> _logger;

        public AccommodationsController(
            IAccommodationService accommodationService,
            IRoomService roomService,
            FirebaseTokenVerificationService tokenVerifier,
            IManagerAccountService managerAccountService,
            ILogger<AccommodationsController> logger)
        {
            _accommodationService = accommodationService;
            _roomService = roomService;
            _tokenVerifier = tokenVerifier;
            _managerAccountService = managerAccountService;
            _logger = logger;
        }

        /// <summary>
        /// Extracts and verifies the caller's Firebase ID token.
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
        /// Requires the Firebase admin custom claim.
        /// </summary>
        private async Task<ActionResult?> RequireAdminAsync()
        {
            string authorization =
                Request.Headers.Authorization.ToString();

            if (string.IsNullOrWhiteSpace(authorization) ||
                !authorization.StartsWith(
                    "Bearer ",
                    StringComparison.OrdinalIgnoreCase))
            {
                return Unauthorized(new
                {
                    message = "A Firebase ID token is required."
                });
            }

            string idToken =
                authorization["Bearer ".Length..].Trim();

            if (string.IsNullOrWhiteSpace(idToken))
            {
                return Unauthorized(new
                {
                    message = "A Firebase ID token is required."
                });
            }

            bool isAdmin =
                await _tokenVerifier.VerifyAdminAsync(idToken);

            if (!isAdmin)
            {
                return StatusCode(403, new
                {
                    message = "Firebase administrator access is required."
                });
            }

            return null;
        }

        /// <summary>
        /// Allows an administrator or the manager assigned
        /// to the specified accommodation.
        /// </summary>
        private async Task<ActionResult?> RequireAccommodationManagerOrAdminAsync(
            string accommodationId,
            CancellationToken cancellationToken)
        {
            var (userId, error) =
                await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            string authorization =
                Request.Headers.Authorization.ToString();

            string idToken =
                authorization["Bearer ".Length..].Trim();

            if (await _tokenVerifier.VerifyAdminAsync(idToken))
            {
                return null;
            }

            ManagerResponse? manager =
                await _managerAccountService.GetByIdAsync(
                    userId!,
                    cancellationToken);

            if (manager is null ||
                !string.Equals(
                    manager.FirebaseUid,
                    userId,
                    StringComparison.Ordinal) ||
                !string.Equals(
                    manager.AccommodationId,
                    accommodationId,
                    StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "You are not authorized to manage this accommodation."
                });
            }

            return null;
        }

        /// <summary>
        /// Public accommodation listing.
        /// </summary>
        [HttpGet]
        public async Task<ActionResult<IReadOnlyList<AccommodationResponse>>>
            GetAll(CancellationToken cancellationToken)
        {
            IReadOnlyList<AccommodationResponse> accommodations =
                await _accommodationService.GetAllAsync(
                    cancellationToken);

            return Ok(accommodations);
        }

        /// <summary>
        /// Public accommodation details.
        /// </summary>
        [HttpGet("{id}")]
        public async Task<ActionResult<AccommodationResponse>> GetById(
            string id,
            CancellationToken cancellationToken)
        {
            AccommodationResponse? accommodation =
                await _accommodationService.GetByIdAsync(
                    id,
                    cancellationToken);

            if (accommodation is null)
            {
                return NotFound(new
                {
                    message = "Accommodation was not found."
                });
            }

            return Ok(accommodation);
        }

        /// <summary>
        /// Creates an accommodation. Firebase admins only.
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<AccommodationResponse>> Create(
            CreateAccommodationRequest request,
            CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await RequireAdminAsync();

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            AccommodationResponse accommodation =
                await _accommodationService.CreateAsync(
                    request,
                    cancellationToken);

            _logger.LogInformation(
                "Accommodation {AccommodationId} was created.",
                accommodation.Id);

            return CreatedAtAction(
                nameof(GetById),
                new { id = accommodation.Id },
                accommodation);
        }

        /// <summary>
        /// Updates an accommodation. Firebase admins or the
        /// accommodation's assigned manager only.
        /// </summary>
        [HttpPut("{id}")]
        public async Task<ActionResult<AccommodationResponse>> Update(
            string id,
            UpdateAccommodationRequest request,
            CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await RequireAccommodationManagerOrAdminAsync(
                    id,
                    cancellationToken);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            AccommodationResponse? accommodation =
                await _accommodationService.UpdateAsync(
                    id,
                    request,
                    cancellationToken);

            if (accommodation is null)
            {
                return NotFound(new
                {
                    message = "Accommodation was not found."
                });
            }

            _logger.LogInformation(
                "Accommodation {AccommodationId} was updated.",
                id);

            return Ok(accommodation);
        }

        /// <summary>
        /// Public room listing for an accommodation.
        /// </summary>
        [HttpGet("{id}/rooms")]
        public async Task<ActionResult<IReadOnlyList<RoomResponse>>> GetRooms(
            string id,
            CancellationToken cancellationToken)
        {
            AccommodationResponse? accommodation =
                await _accommodationService.GetByIdAsync(
                    id,
                    cancellationToken);

            if (accommodation is null)
            {
                return NotFound(new
                {
                    message = "Accommodation was not found."
                });
            }

            IReadOnlyList<RoomResponse> rooms =
                await _roomService.GetByAccommodationIdAsync(
                    id,
                    cancellationToken);

            return Ok(rooms);
        }

        /// <summary>
        /// Creates a room. Firebase admins or the accommodation's
        /// assigned manager only.
        /// </summary>
        [HttpPost("{id}/rooms")]
        public async Task<ActionResult<RoomResponse>> CreateRoom(
            string id,
            CreateRoomRequest request,
            CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await RequireAccommodationManagerOrAdminAsync(
                    id,
                    cancellationToken);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            RoomResponse? room =
                await _roomService.CreateAsync(
                    id,
                    request,
                    cancellationToken);

            if (room is null)
            {
                return NotFound(new
                {
                    message = "Accommodation was not found."
                });
            }

            _logger.LogInformation(
                "Room {RoomId} was created for accommodation {AccommodationId}.",
                room.Id,
                id);

            return Created($"/api/rooms/{room.Id}", room);
        }
    }
}
