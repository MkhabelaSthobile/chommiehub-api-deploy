using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines persistence operations for resident visitor requests.
    /// </summary>
    public interface IVisitorRequestRepository
    {
        Task<VisitorRequest> CreateAsync(
            VisitorRequest request,
            CancellationToken cancellationToken = default);

        Task<IReadOnlyList<VisitorRequest>> GetByResidentIdAsync(
            string residentId,
            CancellationToken cancellationToken = default);

        Task<IReadOnlyList<VisitorRequest>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default);

        Task<VisitorRequest?> GetByIdAsync(
            string id,
            CancellationToken cancellationToken = default);

        Task<VisitorRequest?> UpdateStatusAsync(
            string id,
            string status,
            CancellationToken cancellationToken = default);
    }
}