using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    public interface IApplicationRepository
    {
        Task<IReadOnlyList<Application>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default);

        Task<Application?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<bool> UpdateAsync(Application application, CancellationToken cancellationToken = default);
    }
}