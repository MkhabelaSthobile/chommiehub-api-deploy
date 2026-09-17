using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services
{
    /// <summary>
    ///Defines the business operations available for accommodation management
    /// </summary>
    public interface IAccommodationService
    {
        Task<IReadOnlyList<AccommodationResponse>> GetAllAsync(CancellationToken cancellationToken = default);

        Task<AccommodationResponse?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<AccommodationResponse> CreateAsync(CreateAccommodationRequest request, CancellationToken cancellationToken = default);

        Task<AccommodationResponse?> UpdateAsync(string id, UpdateAccommodationRequest request, CancellationToken cancellationToken = default);
    }
}