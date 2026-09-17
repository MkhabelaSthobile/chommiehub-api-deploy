using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    public interface IApplicationRepository
    {
        Task<Application?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<Application>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<Application>> GetByStudentIdAsync(string studentId, CancellationToken cancellationToken = default);

        Task<Application> CreateAsync(Application application, CancellationToken cancellationToken = default);

        Task<bool> UpdateAsync(Application application, CancellationToken cancellationToken = default);
    }
}