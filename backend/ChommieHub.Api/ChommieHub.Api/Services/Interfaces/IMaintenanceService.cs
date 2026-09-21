using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services.Interfaces
{
    /// <summary>
    /// Defines the business operations available for maintenance requests.
    /// </summary>
    public interface IMaintenanceService
    {
        Task<MaintenanceRequestResponse> CreateAsync(
            CreateMaintenanceRequest request,
            CancellationToken cancellationToken = default);

        Task<IReadOnlyList<MaintenanceRequestResponse>> GetByResidentIdAsync(
            string residentId,
            CancellationToken cancellationToken = default);

        Task<IReadOnlyList<MaintenanceRequestResponse>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default);

        Task<MaintenanceRequestResponse?> UpdateStatusAsync(
            string id,
            UpdateMaintenanceStatusRequest request,
            CancellationToken cancellationToken = default);
    }
}