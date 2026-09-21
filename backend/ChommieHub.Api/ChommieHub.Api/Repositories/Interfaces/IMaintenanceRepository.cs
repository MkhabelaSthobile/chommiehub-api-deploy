using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines persistence operations for resident maintenance requests.
    /// </summary>
    public interface IMaintenanceRepository
    {
        Task<MaintenanceRequest> CreateAsync(
            MaintenanceRequest request,
            CancellationToken cancellationToken = default);

        Task<IReadOnlyList<MaintenanceRequest>> GetByResidentIdAsync(
            string residentId,
            CancellationToken cancellationToken = default);

        Task<IReadOnlyList<MaintenanceRequest>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default);

        Task<MaintenanceRequest?> GetByIdAsync(
            string id,
            CancellationToken cancellationToken = default);

        Task<MaintenanceRequest?> UpdateStatusAsync(
            string id,
            string status,
            CancellationToken cancellationToken = default);
    }
}