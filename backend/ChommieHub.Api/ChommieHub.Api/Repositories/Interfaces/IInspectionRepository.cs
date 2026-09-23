using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines Firestore-independent persistence operations for digital inspections.
    /// </summary>
    public interface IInspectionRepository
    {
        Task<Inspection> CreateAsync(Inspection inspection, CancellationToken cancellationToken = default);
        Task<IReadOnlyList<Inspection>> GetByResidentIdAsync(string residentId, CancellationToken cancellationToken = default);
        Task<IReadOnlyList<Inspection>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default);
        Task<Inspection?> GetByIdAsync(string id, CancellationToken cancellationToken = default);
        Task<Inspection?> ConfirmAsync(string id, string managerNotes, CancellationToken cancellationToken = default);
    }
}