using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services
{
    // A residence manager's own accommodation: draft, edit and submit for review.
    public interface IManagerListingService
    {
        Task<ManagerAccommodationResponse?> GetMyAccommodationAsync(
            string managerId,
            CancellationToken cancellationToken = default);

        Task<ManagerAccommodationResponse> SaveDraftAsync(
            string managerId,
            CreateAccommodationRequest request,
            CancellationToken cancellationToken = default);

        Task<ManagerAccommodationResponse> SubmitForReviewAsync(
            string managerId,
            CancellationToken cancellationToken = default);
    }
}