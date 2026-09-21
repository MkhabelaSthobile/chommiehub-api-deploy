namespace ChommieHub.Api.Repositories.Interfaces
{
    // Defines persistence operations for a student's shortlist.
    public interface IShortlistRepository
    {
        Task AddAsync(string studentId, string accommodationId, CancellationToken cancellationToken = default);

        Task RemoveAsync(string studentId, string accommodationId, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<string>> GetAccommodationIdsAsync(string studentId, CancellationToken cancellationToken = default);
    }
}
