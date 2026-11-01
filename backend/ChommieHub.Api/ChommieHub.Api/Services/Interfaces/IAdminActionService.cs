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

        Task<AdminActionResponse> DisableAccountAsync(
            string adminId,
            string accountId,
            string? reason,
            CancellationToken cancellationToken = default);

        Task<AdminActionResponse> EnableAccountAsync(
            string adminId,
            string accountId,
            CancellationToken cancellationToken = default);

        Task<AdminActionResponse> DeleteAccountAsync(
            string adminId,
            string accountId,
            string? reason,
            CancellationToken cancellationToken = default);
    }
}