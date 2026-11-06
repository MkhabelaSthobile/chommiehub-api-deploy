using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{
    public sealed class ManagerListingService : IManagerListingService
    {
        private readonly IManagerRepository _managerRepository;
        private readonly IAccommodationRepository _accommodationRepository;

        public ManagerListingService(
            IManagerRepository managerRepository,
            IAccommodationRepository accommodationRepository)
        {
            _managerRepository = managerRepository;
            _accommodationRepository = accommodationRepository;
        }

        public async Task<ManagerAccommodationResponse?> GetMyAccommodationAsync(
            string managerId,
            CancellationToken cancellationToken = default)
        {
            Manager manager = await GetManagerAsync(managerId, cancellationToken);

            if (string.IsNullOrWhiteSpace(manager.AccommodationId))
            {
                return null;
            }

            Accommodation? accommodation = await _accommodationRepository.GetByIdAsync(
                manager.AccommodationId,
                cancellationToken);

            return accommodation is null ? null : MapToResponse(accommodation);
        }

        public async Task<ManagerAccommodationResponse> SaveDraftAsync(
            string managerId,
            CreateAccommodationRequest request,
            CancellationToken cancellationToken = default)
        {
            Manager manager = await GetManagerAsync(managerId, cancellationToken);

            if (string.IsNullOrWhiteSpace(manager.AccommodationId))
            {
                var draft = new Accommodation
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
                    ReviewStatus = ListingStatuses.Draft,
                    CreatedAtUtc = DateTime.UtcNow,
                    UpdatedAtUtc = DateTime.UtcNow
                };

                Accommodation created = await _accommodationRepository.CreateAsync(
                    draft,
                    cancellationToken);

                manager.AccommodationId = created.Id;
                manager.UpdatedAtUtc = DateTime.UtcNow;
                await _managerRepository.UpdateAsync(manager, cancellationToken);

                return MapToResponse(created);
            }

            Accommodation accommodation = await GetOwnAccommodationAsync(manager, cancellationToken);

            if (accommodation.IsArchived
                || accommodation.ReviewStatus is not (ListingStatuses.Draft or ListingStatuses.Rejected))
            {
                throw new InvalidOperationException(
                    "Only a draft or rejected accommodation can be edited here.");
            }

            accommodation.Name = request.Name.Trim();
            accommodation.Address = request.Address.Trim();
            accommodation.Latitude = request.Latitude;
            accommodation.Longitude = request.Longitude;
            accommodation.Description = request.Description.Trim();
            accommodation.PhoneNumber = request.PhoneNumber.Trim();
            accommodation.Amenities = request.Amenities;
            accommodation.InstitutionArea = request.InstitutionArea.Trim();
            accommodation.UpdatedAtUtc = DateTime.UtcNow;

            await _accommodationRepository.UpdateAsync(accommodation, cancellationToken);

            return MapToResponse(accommodation);
        }

        public async Task<ManagerAccommodationResponse> SubmitForReviewAsync(
            string managerId,
            CancellationToken cancellationToken = default)
        {
            Manager manager = await GetManagerAsync(managerId, cancellationToken);
            Accommodation accommodation = await GetOwnAccommodationAsync(manager, cancellationToken);

            if (accommodation.IsArchived
                || accommodation.ReviewStatus is not (ListingStatuses.Draft or ListingStatuses.Rejected))
            {
                throw new InvalidOperationException(
                    "Only a draft or rejected accommodation can be submitted for review.");
            }

            if (string.IsNullOrWhiteSpace(accommodation.Name)
                || string.IsNullOrWhiteSpace(accommodation.Address)
                || string.IsNullOrWhiteSpace(accommodation.Description)
                || string.IsNullOrWhiteSpace(accommodation.InstitutionArea))
            {
                throw new ArgumentException(
                    "Complete the name, address, description and area before submitting.");
            }

            if (accommodation.Latitude == 0 && accommodation.Longitude == 0)
            {
                throw new ArgumentException("Set the residence location before submitting.");
            }

            accommodation.ReviewStatus = ListingStatuses.Pending;
            accommodation.ReviewReason = string.Empty;
            accommodation.ReviewedByUid = string.Empty;
            accommodation.ReviewedAtUtc = null;
            accommodation.SubmittedAtUtc = DateTime.UtcNow;
            accommodation.UpdatedAtUtc = DateTime.UtcNow;

            await _accommodationRepository.UpdateAsync(accommodation, cancellationToken);

            return MapToResponse(accommodation);
        }

        private async Task<Manager> GetManagerAsync(string managerId, CancellationToken cancellationToken)
        {
            Manager? manager = await _managerRepository.GetByIdAsync(managerId, cancellationToken);

            if (manager is null || manager.IsDisabled)
            {
                throw new KeyNotFoundException("An active manager profile is required.");
            }

            return manager;
        }

        private async Task<Accommodation> GetOwnAccommodationAsync(
            Manager manager,
            CancellationToken cancellationToken)
        {
            if (string.IsNullOrWhiteSpace(manager.AccommodationId))
            {
                throw new InvalidOperationException("Save your accommodation details first.");
            }

            Accommodation? accommodation = await _accommodationRepository.GetByIdAsync(
                manager.AccommodationId,
                cancellationToken);

            return accommodation
                ?? throw new InvalidOperationException("Save your accommodation details first.");
        }

        private static ManagerAccommodationResponse MapToResponse(Accommodation accommodation)
        {
            return new ManagerAccommodationResponse
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
                IsActive = accommodation.IsActive,
                ReviewStatus = accommodation.ReviewStatus,
                ReviewReason = accommodation.ReviewReason,
                SubmittedAtUtc = accommodation.SubmittedAtUtc,
                ReviewedAtUtc = accommodation.ReviewedAtUtc,
                IsArchived = accommodation.IsArchived,
                ImageUrls = accommodation.ImageUrls
            };
        }
    }
}