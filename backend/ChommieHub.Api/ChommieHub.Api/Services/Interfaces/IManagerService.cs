using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Defines residence-manager business operations including application review, room assignment, resident management and dashboard reporting
    /// </summary>
    public interface IManagerService
    {
        Task<IReadOnlyList<ManagerApplicationSummaryResponse>> GetApplicationsAsync(string accommodationId, CancellationToken cancellationToken = default);

        Task<ManagerApplicationDetailResponse?> GetApplicationAsync(string applicationId, CancellationToken cancellationToken = default);

        Task<ManagerApplicationDetailResponse?> ReviewApplicationAsync(string applicationId, ReviewApplicationRequest request, CancellationToken cancellationToken = default);

        Task<ResidentResponse?> AssignRoomAsync(string applicationId, AssignRoomRequest request, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<ResidentResponse>> GetResidentsAsync(string accommodationId, CancellationToken cancellationToken = default);

        Task<ResidentResponse?> GetResidentByStudentIdAsync(string studentId, CancellationToken cancellationToken = default);

        Task<ManagerDashboardResponse> GetDashboardAsync(string accommodationId, CancellationToken cancellationToken = default);
    }
}