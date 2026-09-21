using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services.Interfaces;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Implements visitor request submission, tracking and manager review.
    /// </summary>
    public sealed class VisitorRequestService : IVisitorRequestService
    {
        private readonly IVisitorRequestRepository _visitorRequestRepository;

        public VisitorRequestService(
            IVisitorRequestRepository visitorRequestRepository)
        {
            _visitorRequestRepository = visitorRequestRepository;
        }

        public async Task<VisitorRequestResponse> CreateAsync(
            CreateVisitorRequest request,
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

            if (string.IsNullOrWhiteSpace(request.VisitorName))
            {
                throw new InvalidOperationException("Visitor name is required.");
            }

            if (string.IsNullOrWhiteSpace(request.VisitorIdentification))
            {
                throw new InvalidOperationException(
                    "Visitor identification is required.");
            }

            if (request.ArrivalDateUtc == default)
            {
                throw new InvalidOperationException(
                    "Arrival date is required.");
            }

            if (request.DepartureDateUtc == default)
            {
                throw new InvalidOperationException(
                    "Departure date is required.");
            }

            if (request.DepartureDateUtc < request.ArrivalDateUtc)
            {
                throw new InvalidOperationException(
                    "Departure date cannot be before the arrival date.");
            }

            VisitorRequest visitorRequest = new()
            {
                ResidentId = request.ResidentId.Trim(),
                AccommodationId = request.AccommodationId.Trim(),
                VisitorName = request.VisitorName.Trim(),
                VisitorIdentification = request.VisitorIdentification.Trim(),
                ArrivalDateUtc = request.ArrivalDateUtc,
                DepartureDateUtc = request.DepartureDateUtc,
                IsSleepover = request.IsSleepover,
                Status = VisitorRequestStatuses.Pending,
                CreatedAtUtc = DateTime.UtcNow
            };

            VisitorRequest created =
                await _visitorRequestRepository.CreateAsync(
                    visitorRequest,
                    cancellationToken);

            return MapResponse(created);
        }

        public async Task<IReadOnlyList<VisitorRequestResponse>> GetByResidentIdAsync(
            string residentId,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(residentId))
            {
                throw new InvalidOperationException("Resident ID is required.");
            }

            IReadOnlyList<VisitorRequest> requests =
                await _visitorRequestRepository.GetByResidentIdAsync(
                    residentId,
                    cancellationToken);

            return requests
                .Select(MapResponse)
                .ToList();
        }

        public async Task<IReadOnlyList<VisitorRequestResponse>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(accommodationId))
            {
                throw new InvalidOperationException(
                    "Accommodation ID is required.");
            }

            IReadOnlyList<VisitorRequest> requests =
                await _visitorRequestRepository.GetByAccommodationIdAsync(
                    accommodationId,
                    cancellationToken);

            return requests
                .Select(MapResponse)
                .ToList();
        }

        public async Task<VisitorRequestResponse?> UpdateStatusAsync(
            string id,
            UpdateVisitorRequestStatusRequest request,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(id))
            {
                throw new InvalidOperationException(
                    "Visitor request ID is required.");
            }

            string status = request.Status.Trim();

            if (!IsValidStatus(status))
            {
                throw new InvalidOperationException(
                    "Status must be Pending, Approved or Rejected.");
            }

            string normalisedStatus = NormaliseStatus(status);

            VisitorRequest? updated =
                await _visitorRequestRepository.UpdateStatusAsync(
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
                       VisitorRequestStatuses.Pending,
                       StringComparison.OrdinalIgnoreCase)
                   || status.Equals(
                       VisitorRequestStatuses.Approved,
                       StringComparison.OrdinalIgnoreCase)
                   || status.Equals(
                       VisitorRequestStatuses.Rejected,
                       StringComparison.OrdinalIgnoreCase);
        }

        private static string NormaliseStatus(string status)
        {
            if (status.Equals(
                VisitorRequestStatuses.Pending,
                StringComparison.OrdinalIgnoreCase))
            {
                return VisitorRequestStatuses.Pending;
            }

            if (status.Equals(
                VisitorRequestStatuses.Approved,
                StringComparison.OrdinalIgnoreCase))
            {
                return VisitorRequestStatuses.Approved;
            }

            return VisitorRequestStatuses.Rejected;
        }

        private static VisitorRequestResponse MapResponse(
            VisitorRequest request)
        {
            return new VisitorRequestResponse
            {
                Id = request.Id,
                ResidentId = request.ResidentId,
                AccommodationId = request.AccommodationId,
                VisitorName = request.VisitorName,
                VisitorIdentification = request.VisitorIdentification,
                ArrivalDateUtc = request.ArrivalDateUtc,
                DepartureDateUtc = request.DepartureDateUtc,
                IsSleepover = request.IsSleepover,
                Status = request.Status,
                CreatedAtUtc = request.CreatedAtUtc,
                ReviewedAtUtc = request.ReviewedAtUtc
            };
        }
    }
}