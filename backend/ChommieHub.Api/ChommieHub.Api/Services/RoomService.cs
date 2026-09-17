using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{
    /// <summary>
    ///Coordinates room-related business rules and prevents rooms from being created under accommodation records that do not exist
    /// </summary>
    public sealed class RoomService : IRoomService
    {
        private readonly IRoomRepository _roomRepository;
        private readonly IAccommodationRepository _accommodationRepository;

        public RoomService(IRoomRepository roomRepository, IAccommodationRepository accommodationRepository)
        {
            _roomRepository = roomRepository;
            _accommodationRepository = accommodationRepository;
        }

        public async Task<IReadOnlyList<RoomResponse>>
            GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Room> rooms = await _roomRepository.GetByAccommodationIdAsync(
                accommodationId,
                cancellationToken);

            return rooms
                .Select(MapToResponse)
                .ToList();
        }

        public async Task<RoomResponse?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            Room? room = await _roomRepository.GetByIdAsync(
                id, 
                cancellationToken);

            return room is null
                ? null
                : MapToResponse(room);
        }

        public async Task<RoomResponse?> CreateAsync(string accommodationId, CreateRoomRequest request, CancellationToken cancellationToken = default)
        {
            // A room must belong to a valid accommodation before it can be persisted
            Accommodation? accommodation = await _accommodationRepository.GetByIdAsync(
                accommodationId,
                cancellationToken);

            if (accommodation is null)
            {
                return null;
            }

            var room = new Room
            {
                AccommodationId = accommodationId,
                RoomType = request.RoomType.Trim(),
                Price = request.Price,
                Capacity = request.Capacity,
                Availability = request.Availability,
                CreatedAtUtc = DateTime.UtcNow,
                UpdatedAtUtc = DateTime.UtcNow
            };

            Room created = await _roomRepository.CreateAsync(
                room,
                cancellationToken);

            return MapToResponse(created);
        }

        public async Task<RoomResponse?> UpdateAsync(string id, UpdateRoomRequest request, CancellationToken cancellationToken = default)
        {
            Room? room = await _roomRepository.GetByIdAsync(
                id,
                cancellationToken);

            if (room is null)
            {
                return null;
            }

            room.RoomType = request.RoomType.Trim();
            room.Price = request.Price;
            room.Capacity = request.Capacity;
            room.Availability = request.Availability;
            room.UpdatedAtUtc = DateTime.UtcNow;

            await _roomRepository.UpdateAsync(
                room,
                cancellationToken);

            return MapToResponse(room);
        }

        /// <summary>
        /// Converts the internal room model into the API response representation
        /// </summary>
        private static RoomResponse MapToResponse(Room room)
        {
            return new RoomResponse
            {
                Id = room.Id,
                AccommodationId = room.AccommodationId,
                RoomType = room.RoomType,
                Price = room.Price,
                Capacity = room.Capacity,
                Availability = room.Availability
            };
        }
    }
}