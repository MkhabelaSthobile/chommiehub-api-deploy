using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines persistence operations for rooms and accommodation-based room queries
    /// </summary>
    public interface IRoomRepository
    {
        Task<IReadOnlyList<Room>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default);

        Task<Room?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<Room> CreateAsync(Room room, CancellationToken cancellationToken = default);

        Task<bool> UpdateAsync(Room room, CancellationToken cancellationToken = default);
    }
}