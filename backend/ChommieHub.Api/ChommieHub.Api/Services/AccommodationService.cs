using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{
    public sealed class AccommodationService : IAccommodationService
    {
        private readonly IAccommodationRepository _repository;

        public AccommodationService(
            IAccommodationRepository repository)
        {
            _repository = repository;
        }

        public async Task<IReadOnlyList<AccommodationResponse>> GetAllAsync(CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Accommodation> accommodations = await _repository.GetAllAsync(cancellationToken);

            return accommodations
                .Select(MapToResponse)
                .ToList();
        }

        public async Task<AccommodationResponse?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            Accommodation? accommodation = await _repository.GetByIdAsync(id, cancellationToken);

            return accommodation is null
                ? null
                : MapToResponse(accommodation);
        }

        public async Task<AccommodationResponse> CreateAsync(CreateAccommodationRequest request, CancellationToken cancellationToken = default)
        {
            var accommodation = new Accommodation
            {
                Name = request.Name.Trim(),
                Address = request.Address.Trim(),
                Latitude = request.Latitude,
                Longitude = request.Longitude,
                Description = request.Description.Trim(),
                PhoneNumber = request.PhoneNumber.Trim(),
                Amenities = request.Amenities,
                InstitutionArea = request.InstitutionArea.Trim(),
                IsActive = true,
                CreatedAtUtc = DateTime.UtcNow,
                UpdatedAtUtc = DateTime.UtcNow
            };

            Accommodation created = await _repository.CreateAsync(accommodation, cancellationToken);

            return MapToResponse(created);
        }

        public async Task<AccommodationResponse?> UpdateAsync(string id, UpdateAccommodationRequest request, CancellationToken cancellationToken = default)
        {
            Accommodation? accommodation = await _repository.GetByIdAsync(id, cancellationToken);

            if (accommodation is null)
            {
                return null;
            }

            accommodation.Name = request.Name.Trim();
            accommodation.Address = request.Address.Trim();
            accommodation.Latitude = request.Latitude;
            accommodation.Longitude = request.Longitude;
            accommodation.Description = request.Description.Trim();
            accommodation.PhoneNumber = request.PhoneNumber.Trim();
            accommodation.Amenities = request.Amenities;
            accommodation.InstitutionArea = request.InstitutionArea.Trim();
            accommodation.IsActive = request.IsActive;
            accommodation.UpdatedAtUtc = DateTime.UtcNow;

            await _repository.UpdateAsync(accommodation, cancellationToken);

            return MapToResponse(accommodation);
        }

        private static AccommodationResponse MapToResponse(Accommodation accommodation)
        {
            return new AccommodationResponse
            {
                Id = accommodation.Id,
                Name = accommodation.Name,
                Address = accommodation.Address,
                Latitude = accommodation.Latitude,
                Longitude = accommodation.Longitude,
                Description = accommodation.Description,
                PhoneNumber = accommodation.PhoneNumber,
                Amenities = accommodation.Amenities,
                InstitutionArea = accommodation.InstitutionArea,
                IsActive = accommodation.IsActive
            };
        }
    }
}