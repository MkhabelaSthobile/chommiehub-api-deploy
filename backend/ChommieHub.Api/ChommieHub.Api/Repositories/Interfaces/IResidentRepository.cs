using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines persistence operations for active residence records.
    /// </summary>
    public interface IResidentRepository
    {
        Task<IReadOnlyList<Resident>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default);

        Task<Resident?> GetByApplicationIdAsync(
            string applicationId,
            CancellationToken cancellationToken = default);

        Task<Resident?> GetByStudentIdAsync(
            string studentId,
            CancellationToken cancellationToken = default);

        Task<Resident> CreateAsync(
            Resident resident,
            CancellationToken cancellationToken = default);
    }
}