using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services.Interfaces
{
    /// <summary>
    /// Defines the business operations available for visitor requests.
    /// </summary>
    public interface IVisitorRequestService
    {
        Task<VisitorRequestResponse> CreateAsync(
            CreateVisitorRequest request,
            CancellationToken cancellationToken = default);

        Task<IReadOnlyList<VisitorRequestResponse>> GetByResidentIdAsync(
            string residentId,
            CancellationToken cancellationToken = default);

        Task<IReadOnlyList<VisitorRequestResponse>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default);

        Task<VisitorRequestResponse?> UpdateStatusAsync(
            string id,
            UpdateVisitorRequestStatusRequest request,
            CancellationToken cancellationToken = default);
    }
}