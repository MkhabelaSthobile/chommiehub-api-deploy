
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides public room details and authorized room management.
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public sealed class RoomsController : ControllerBase
    {
        private readonly IRoomService _roomService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;
        private readonly IManagerAccountService _managerAccountService;
        private readonly ILogger<RoomsController> _logger;

        public RoomsController(
            IRoomService roomService,
            FirebaseTokenVerificationService tokenVerifier,
            IManagerAccountService managerAccountService,
            ILogger<RoomsController> logger)
        {
            _roomService = roomService;
            _tokenVerifier = tokenVerifier;
            _managerAccountService = managerAccountService;
            _logger = logger;
        }

        /// <summary>
        /// Verifies that the caller is a Firebase admin or
        /// the manager assigned to the room's accommodation.
        /// </summary>
        private async Task<ActionResult?> AuthorizeRoomUpdateAsync(
            string accommodationId,
            CancellationToken cancellationToken)
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

            string? firebaseUid =
                await _tokenVerifier.VerifyUserIdAsync(idToken);

            if (string.IsNullOrWhiteSpace(firebaseUid))
            {
                return Unauthorized(new
                {
                    message = "Invalid or revoked Firebase ID token."
                });
            }

            // Firebase admins can update any room.
            if (await _tokenVerifier.VerifyAdminAsync(idToken))
            {
                return null;
            }

            // Managers can update rooms only within their
            // assigned accommodation.
            ManagerResponse? manager =
                await _managerAccountService.GetByIdAsync(
                    firebaseUid,
                    cancellationToken);

            if (manager is null ||
                !string.Equals(
                    manager.FirebaseUid,
                    firebaseUid,
                    StringComparison.Ordinal) ||
                string.IsNullOrWhiteSpace(manager.AccommodationId) ||
                !string.Equals(
                    manager.AccommodationId,
                    accommodationId,
                    StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "You are not authorized to update rooms in this accommodation."
                });
            }

            return null;
        }

        /// <summary>
        /// Public endpoint for retrieving room details.
        /// </summary>
        [HttpGet("{id}")]
        public async Task<ActionResult<RoomResponse>> GetById(
            string id,
            CancellationToken cancellationToken)
        {
            RoomResponse? room =
                await _roomService.GetByIdAsync(
                    id,
                    cancellationToken);

            if (room is null)
            {
                return NotFound(new
                {
                    message = "Room was not found."
                });
            }

            return Ok(room);
        }

        /// <summary>
        /// Updates room information such as type, price,
        /// capacity and availability.
        /// Requires Firebase admin or assigned manager access.
        /// </summary>
        [HttpPut("{id}")]
        public async Task<ActionResult<RoomResponse>> Update(
            string id,
            UpdateRoomRequest request,
            CancellationToken cancellationToken)
        {
            // Retrieve the existing room to determine its
            // accommodation. Never trust an accommodation ID
            // supplied by the client when authorizing access.
            RoomResponse? existingRoom =
                await _roomService.GetByIdAsync(
                    id,
                    cancellationToken);

            if (existingRoom is null)
            {
                return NotFound(new
                {
                    message = "Room was not found."
                });
            }

            ActionResult? authorizationError =
                await AuthorizeRoomUpdateAsync(
                    existingRoom.AccommodationId,
                    cancellationToken);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            RoomResponse? room =
                await _roomService.UpdateAsync(
                    id,
                    request,
                    cancellationToken);

            if (room is null)
            {
                return NotFound(new
                {
                    message = "Room was not found."
                });
            }

            _logger.LogInformation(
                "Room {RoomId} was updated.",
                id);

            return Ok(room);
        }
    }
}
