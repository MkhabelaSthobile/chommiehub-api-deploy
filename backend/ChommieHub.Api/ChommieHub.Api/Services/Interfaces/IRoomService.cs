using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Defines business operations for retrieving, creating and updating rooms
    /// </summary>
    public interface IRoomService
    {
        Task<IReadOnlyList<RoomResponse>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default);

        Task<RoomResponse?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<RoomResponse?> CreateAsync(string accommodationId, CreateRoomRequest request, CancellationToken cancellationToken = default);

        Task<RoomResponse?> UpdateAsync(string id, UpdateRoomRequest request, CancellationToken cancellationToken = default);
    }
}