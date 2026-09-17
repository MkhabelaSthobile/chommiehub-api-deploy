using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// defines persistence operations for accommodation records independently of the underlying database technology
    /// </summary>
    public interface IAccommodationRepository
    {
        Task<IReadOnlyList<Accommodation>> GetAllAsync(CancellationToken cancellationToken = default);

        Task<Accommodation?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<Accommodation> CreateAsync(Accommodation accommodation, CancellationToken cancellationToken = default);

        Task<bool> UpdateAsync(Accommodation accommodation, CancellationToken cancellationToken = default);
    }
}