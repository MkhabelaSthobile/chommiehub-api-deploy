using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Repositories.Interfaces;
using ChommieHub.Api.Services.Interfaces;

namespace ChommieHub.Api.Services
{
    // Coordinates shortlist business logic. Hydrates full accommodation
    public sealed class ShortlistService : IShortlistService
    {
        private readonly IShortlistRepository _shortlistRepository;
        private readonly IAccommodationRepository _accommodationRepository;

        public ShortlistService(
            IShortlistRepository shortlistRepository,
            IAccommodationRepository accommodationRepository)
        {
            _shortlistRepository = shortlistRepository;
            _accommodationRepository = accommodationRepository;
        }

        public async Task AddAsync(string studentId, string accommodationId, CancellationToken cancellationToken = default)
        {
            Accommodation? accommodation = await _accommodationRepository.GetByIdAsync(
                accommodationId, cancellationToken);

            if (accommodation is null || !accommodation.IsVisibleToStudents)
            {
                throw new InvalidOperationException(
                    "Only approved, non-archived accommodations can be shortlisted.");
            }

            await _shortlistRepository.AddAsync(studentId, accommodationId, cancellationToken);
        }

        public Task RemoveAsync(string studentId, string accommodationId, CancellationToken cancellationToken = default)
        {
            return _shortlistRepository.RemoveAsync(studentId, accommodationId, cancellationToken);
        }

        public async Task<IReadOnlyList<AccommodationResponse>> GetShortlistAsync(string studentId, CancellationToken cancellationToken = default)
        {
            IReadOnlyList<string> accommodationIds = await _shortlistRepository
                .GetAccommodationIdsAsync(studentId, cancellationToken);

            var results = new List<AccommodationResponse>();

            foreach (string accommodationId in accommodationIds)
            {
                Accommodation? accommodation = await _accommodationRepository
                    .GetByIdAsync(accommodationId, cancellationToken);

                // Skip accommodations that were removed, archived or are no longer approved.
                if (accommodation is not null && accommodation.IsVisibleToStudents)
                {
                    results.Add(MapToResponse(accommodation));
                }
            }

            return results;
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
