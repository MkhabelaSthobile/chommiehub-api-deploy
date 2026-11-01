using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services
{
    // Read-only views for the administrator: dashboard, accounts, listings and audit log.
    
    public interface IAdminQueryService
    {
        Task<AdminDashboardResponse> GetDashboardAsync(CancellationToken cancellationToken = default);

        Task<IReadOnlyList<AdminUserSummary>> GetUsersAsync(
            string? role,
            string? search,
            CancellationToken cancellationToken = default);

        Task<AdminUserDetailResponse?> GetUserAsync(string id, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<AdminListingSummary>> GetListingsAsync(
            string? status,
            CancellationToken cancellationToken = default);

        Task<AdminListingDetailResponse?> GetListingAsync(string id, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<AuditRecordResponse>> GetAuditAsync(int limit, CancellationToken cancellationToken = default);
    }
}