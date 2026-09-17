using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    public interface IResidentRepository
    {
        Task<IReadOnlyList<Resident>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default);

        Task<Resident?> GetByApplicationIdAsync(string applicationId, CancellationToken cancellationToken = default);

        Task<Resident> CreateAsync(Resident resident, CancellationToken cancellationToken = default);
    }
}