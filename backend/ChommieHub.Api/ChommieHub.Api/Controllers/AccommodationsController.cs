using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Exposes REST endpoints for accommodation and room discovery and management
    ///The controller delegates business logic to the service layer rather than communicating with Firestore directly
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public sealed class AccommodationsController : ControllerBase
    {
        private readonly IAccommodationService _accommodationService;
        private readonly IRoomService _roomService;
        private readonly ILogger<AccommodationsController> _logger;

        public AccommodationsController(IAccommodationService accommodationService, IRoomService roomService, ILogger<AccommodationsController> logger)
        {
            _accommodationService = accommodationService;
            _roomService = roomService;
            _logger = logger;
        }

        [HttpGet]
        public async Task<ActionResult<IReadOnlyList<AccommodationResponse>>> GetAll(CancellationToken cancellationToken)
        {
            IReadOnlyList<AccommodationResponse> accommodations = await _accommodationService.GetAllAsync(cancellationToken);

            return Ok(accommodations);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<AccommodationResponse>> GetById(string id, CancellationToken cancellationToken)
        {
            AccommodationResponse? accommodation = await _accommodationService.GetByIdAsync(
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
        /// Creates a new accommodation after ASP.NET Core validates the incoming request
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<AccommodationResponse>> Create(CreateAccommodationRequest request, CancellationToken cancellationToken)
        {
            AccommodationResponse accommodation = await _accommodationService.CreateAsync(
                request,
                cancellationToken);

            _logger.LogInformation("Accommodation {AccommodationId} was created.", accommodation.Id);

            return CreatedAtAction(
                nameof(GetById),
                new { id = accommodation.Id },
                accommodation);
        }

        [HttpPut("{id}")]
        public async Task<ActionResult<AccommodationResponse>> Update(string id, UpdateAccommodationRequest request, CancellationToken cancellationToken)
        {
            AccommodationResponse? accommodation = await _accommodationService.UpdateAsync(
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

            _logger.LogInformation("Accommodation {AccommodationId} was updated.", id);

            return Ok(accommodation);
        }

        [HttpGet("{id}/rooms")]
        public async Task<ActionResult<IReadOnlyList<RoomResponse>>> GetRooms(string id, CancellationToken cancellationToken)
        {
            AccommodationResponse? accommodation = await _accommodationService.GetByIdAsync(
                id,
                cancellationToken);

            if (accommodation is null)
            {
                return NotFound(new
                {
                    message = "Accommodation was not found."
                });
            }

            IReadOnlyList<RoomResponse> rooms = await _roomService.GetByAccommodationIdAsync(
                id,
                cancellationToken);

            return Ok(rooms);
        }

        /// <summary>
        /// Creates a room under an existing accommodation.
        ///a room cannot be created when the parent accommodation does not exist.
        /// </summary>
        [HttpPost("{id}/rooms")]
        public async Task<ActionResult<RoomResponse>> CreateRoom(string id, CreateRoomRequest request, CancellationToken cancellationToken)
        {
            RoomResponse? room = await _roomService.CreateAsync(
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

            _logger.LogInformation("Room {RoomId} was created for accommodation {AccommodationId}.", room.Id, id);

            return Created($"/api/rooms/{room.Id}", room);
        }
    }
}