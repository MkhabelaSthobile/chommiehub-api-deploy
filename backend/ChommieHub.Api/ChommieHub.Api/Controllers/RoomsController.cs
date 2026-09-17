using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides REST endpoints for retrieving and updating individual rooms
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public sealed class RoomsController : ControllerBase
    {
        private readonly IRoomService _roomService;
        private readonly ILogger<RoomsController> _logger;

        public RoomsController(IRoomService roomService, ILogger<RoomsController> logger)
        {
            _roomService = roomService;
            _logger = logger;
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<RoomResponse>> GetById(string id, CancellationToken cancellationToken)
        {
            RoomResponse? room = await _roomService.GetByIdAsync(
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
        ///updates room information such as type, price, capacity and availability.
        /// </summary>
        [HttpPut("{id}")]
        public async Task<ActionResult<RoomResponse>> Update(string id, UpdateRoomRequest request, CancellationToken cancellationToken)
        {
            RoomResponse? room = await _roomService.UpdateAsync(
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

            _logger.LogInformation("Room {RoomId} was updated.", id);

            return Ok(room);
        }
    }
}