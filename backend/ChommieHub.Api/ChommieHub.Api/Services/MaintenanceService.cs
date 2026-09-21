using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services.Interfaces;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Implements resident maintenance submission, tracking and manager status updates.
    /// </summary>
    public sealed class MaintenanceService : IMaintenanceService
    {
        private readonly IMaintenanceRepository _maintenanceRepository;

        public MaintenanceService(IMaintenanceRepository maintenanceRepository)
        {
            _maintenanceRepository = maintenanceRepository;
        }

        public async Task<MaintenanceRequestResponse> CreateAsync(
            CreateMaintenanceRequest request,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(request.ResidentId))
            {
                throw new InvalidOperationException("Resident ID is required.");
            }

            if (string.IsNullOrWhiteSpace(request.AccommodationId))
            {
                throw new InvalidOperationException("Accommodation ID is required.");
            }

            if (string.IsNullOrWhiteSpace(request.RoomId))
            {
                throw new InvalidOperationException("Room ID is required.");
            }

            if (string.IsNullOrWhiteSpace(request.Category))
            {
                throw new InvalidOperationException("Maintenance category is required.");
            }

            if (string.IsNullOrWhiteSpace(request.Description))
            {
                throw new InvalidOperationException("Maintenance description is required.");
            }

            MaintenanceRequest maintenanceRequest = new()
            {
                ResidentId = request.ResidentId.Trim(),
                AccommodationId = request.AccommodationId.Trim(),
                RoomId = request.RoomId.Trim(),
                Category = request.Category.Trim(),
                Description = request.Description.Trim(),
                PhotoUrl = string.IsNullOrWhiteSpace(request.PhotoUrl)
                    ? null
                    : request.PhotoUrl.Trim(),
                Status = MaintenanceStatuses.Pending,
                CreatedAtUtc = DateTime.UtcNow
            };

            MaintenanceRequest created =
                await _maintenanceRepository.CreateAsync(
                    maintenanceRequest,
                    cancellationToken);

            return MapResponse(created);
        }

        public async Task<IReadOnlyList<MaintenanceRequestResponse>> GetByResidentIdAsync(
            string residentId,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(residentId))
            {
                throw new InvalidOperationException("Resident ID is required.");
            }

            IReadOnlyList<MaintenanceRequest> requests =
                await _maintenanceRepository.GetByResidentIdAsync(
                    residentId,
                    cancellationToken);

            return requests
                .Select(MapResponse)
                .ToList();
        }

        public async Task<IReadOnlyList<MaintenanceRequestResponse>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(accommodationId))
            {
                throw new InvalidOperationException("Accommodation ID is required.");
            }

            IReadOnlyList<MaintenanceRequest> requests =
                await _maintenanceRepository.GetByAccommodationIdAsync(
                    accommodationId,
                    cancellationToken);

            return requests
                .Select(MapResponse)
                .ToList();
        }

        public async Task<MaintenanceRequestResponse?> UpdateStatusAsync(
            string id,
            UpdateMaintenanceStatusRequest request,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(id))
            {
                throw new InvalidOperationException("Maintenance request ID is required.");
            }

            string status = request.Status.Trim();

            if (!IsValidStatus(status))
            {
                throw new InvalidOperationException(
                    "Status must be Pending, In Progress or Resolved.");
            }

            string normalisedStatus = NormaliseStatus(status);

            MaintenanceRequest? updated =
                await _maintenanceRepository.UpdateStatusAsync(
                    id,
                    normalisedStatus,
                    cancellationToken);

            return updated is null
                ? null
                : MapResponse(updated);
        }

        private static bool IsValidStatus(string status)
        {
            return status.Equals(
                       MaintenanceStatuses.Pending,
                       StringComparison.OrdinalIgnoreCase)
                   || status.Equals(
                       MaintenanceStatuses.InProgress,
                       StringComparison.OrdinalIgnoreCase)
                   || status.Equals(
                       MaintenanceStatuses.Resolved,
                       StringComparison.OrdinalIgnoreCase);
        }

        private static string NormaliseStatus(string status)
        {
            if (status.Equals(
                MaintenanceStatuses.Pending,
                StringComparison.OrdinalIgnoreCase))
            {
                return MaintenanceStatuses.Pending;
            }

            if (status.Equals(
                MaintenanceStatuses.InProgress,
                StringComparison.OrdinalIgnoreCase))
            {
                return MaintenanceStatuses.InProgress;
            }

            return MaintenanceStatuses.Resolved;
        }

        private static MaintenanceRequestResponse MapResponse(
            MaintenanceRequest request)
        {
            return new MaintenanceRequestResponse
            {
                Id = request.Id,
                ResidentId = request.ResidentId,
                AccommodationId = request.AccommodationId,
                RoomId = request.RoomId,
                Category = request.Category,
                Description = request.Description,
                PhotoUrl = request.PhotoUrl,
                Status = request.Status,
                CreatedAtUtc = request.CreatedAtUtc,
                UpdatedAtUtc = request.UpdatedAtUtc
            };
        }
    }
}