using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services.Interfaces
{
    // Defines the business operations available for a student's shortlist.
    public interface IShortlistService
    {
        Task AddAsync(string studentId, string accommodationId, CancellationToken cancellationToken = default);

        Task RemoveAsync(string studentId, string accommodationId, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<AccommodationResponse>> GetShortlistAsync(string studentId, CancellationToken cancellationToken = default);
    }
}
