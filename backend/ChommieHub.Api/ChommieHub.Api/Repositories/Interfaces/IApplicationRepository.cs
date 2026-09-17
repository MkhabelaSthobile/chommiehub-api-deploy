using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines persistence operations required to retrieve and update accommodation applications
    /// </summary>
    public interface IApplicationRepository
    {
        Task<IReadOnlyList<Application>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default);

        Task<Application?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<bool> UpdateAsync(Application application, CancellationToken cancellationToken = default);
    }
}