using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services
{
    // Administrator actions on accommodation listings and accounts.
    // Every action is written to the audit log.
   
    public interface IAdminActionService
    {
        Task<AdminActionResponse> ApproveListingAsync(
            string adminId,
            string listingId,
            CancellationToken cancellationToken = default);

        Task<AdminActionResponse> RejectListingAsync(
            string adminId,
            string listingId,
            string? reason,
            CancellationToken cancellationToken = default);

        Task<AdminActionResponse> ArchiveListingAsync(
            string adminId,
            string listingId,
            string? reason,
            CancellationToken cancellationToken = default);

        Task<AdminActionResponse> UnarchiveListingAsync(
            string adminId,
            string listingId,
            CancellationToken cancellationToken = default);

        Task<AdminActionResponse> DeleteListingAsync(
            string adminId,
            string listingId,
            string? reason,
            CancellationToken cancellationToken = default);
    }
}