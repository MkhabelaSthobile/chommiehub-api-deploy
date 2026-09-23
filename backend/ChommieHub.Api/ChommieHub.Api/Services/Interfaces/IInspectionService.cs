using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services.Interfaces
{
    /// <summary>
    /// Defines business operations for move-in and move-out inspections.
    /// </summary>
    public interface IInspectionService
    {
        Task<InspectionResponse> CreateAsync(CreateInspectionRequest request, CancellationToken cancellationToken = default);
        Task<IReadOnlyList<InspectionResponse>> GetByResidentIdAsync(string residentId, CancellationToken cancellationToken = default);
        Task<IReadOnlyList<InspectionResponse>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default);
        Task<InspectionResponse?> ConfirmAsync(string id, ConfirmInspectionRequest request, CancellationToken cancellationToken = default);
    }
}