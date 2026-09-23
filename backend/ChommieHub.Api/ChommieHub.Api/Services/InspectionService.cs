using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services.Interfaces;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Applies ChommieHub business rules to move-in and move-out inspections.
    /// </summary>
    public sealed class InspectionService : IInspectionService
    {
        private readonly IInspectionRepository _repository;

        public InspectionService(IInspectionRepository repository)
        {
            _repository = repository;
        }

        public async Task<InspectionResponse> CreateAsync(CreateInspectionRequest request, CancellationToken cancellationToken = default)
        {
            string type = NormaliseType(request.Type);

            if (string.IsNullOrWhiteSpace(request.ResidentId) || string.IsNullOrWhiteSpace(request.StudentId) || string.IsNullOrWhiteSpace(request.AccommodationId) || string.IsNullOrWhiteSpace(request.RoomId))
            {
                throw new InvalidOperationException("Resident, student, accommodation and room identifiers are required.");
            }

            if (request.Items.Count == 0)
            {
                throw new InvalidOperationException("At least one inspection item is required.");
            }

            if (request.Items.Any(item => string.IsNullOrWhiteSpace(item.ItemName) || string.IsNullOrWhiteSpace(item.Condition)))
            {
                throw new InvalidOperationException("Every inspection item requires an item name and condition.");
            }

            Inspection inspection = new()
            {
                ResidentId = request.ResidentId.Trim(),
                StudentId = request.StudentId.Trim(),
                AccommodationId = request.AccommodationId.Trim(),
                RoomId = request.RoomId.Trim(),
                Type = type,
                Items = request.Items.Select(item => new InspectionItem
                {
                    ItemName = item.ItemName.Trim(),
                    Condition = item.Condition.Trim(),
                    Comments = item.Comments.Trim(),
                    PhotoReference = item.PhotoReference.Trim()
                }).ToList(),
                Status = "Submitted",
                ManagerConfirmed = false,
                CreatedAtUtc = DateTime.UtcNow
            };

            Inspection savedInspection = await _repository.CreateAsync(inspection, cancellationToken);

            return Map(savedInspection);
        }

        public async Task<IReadOnlyList<InspectionResponse>> GetByResidentIdAsync(string residentId, CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(residentId))
            {
                throw new InvalidOperationException("Resident ID is required.");
            }

            IReadOnlyList<Inspection> inspections = await _repository.GetByResidentIdAsync(residentId, cancellationToken);

            return inspections.Select(Map).ToList();
        }

        public async Task<IReadOnlyList<InspectionResponse>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(accommodationId))
            {
                throw new InvalidOperationException("Accommodation ID is required.");
            }

            IReadOnlyList<Inspection> inspections = await _repository.GetByAccommodationIdAsync(accommodationId, cancellationToken);

            return inspections.Select(Map).ToList();
        }

        public async Task<InspectionResponse?> ConfirmAsync(string id, ConfirmInspectionRequest request, CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(id))
            {
                throw new InvalidOperationException("Inspection ID is required.");
            }

            Inspection? inspection = await _repository.ConfirmAsync(id, request.ManagerNotes.Trim(), cancellationToken);

            return inspection is null ? null : Map(inspection);
        }

        private static string NormaliseType(string type)
        {
            if (type.Equals("Move-In", StringComparison.OrdinalIgnoreCase) || type.Equals("Move In", StringComparison.OrdinalIgnoreCase))
            {
                return "Move-In";
            }

            if (type.Equals("Move-Out", StringComparison.OrdinalIgnoreCase) || type.Equals("Move Out", StringComparison.OrdinalIgnoreCase))
            {
                return "Move-Out";
            }

            throw new InvalidOperationException("Inspection type must be Move-In or Move-Out.");
        }

        private static InspectionResponse Map(Inspection inspection)
        {
            return new InspectionResponse
            {
                Id = inspection.Id,
                ResidentId = inspection.ResidentId,
                StudentId = inspection.StudentId,
                AccommodationId = inspection.AccommodationId,
                RoomId = inspection.RoomId,
                Type = inspection.Type,
                Items = inspection.Items.Select(item => new InspectionItemResponse
                {
                    ItemName = item.ItemName,
                    Condition = item.Condition,
                    Comments = item.Comments,
                    PhotoReference = item.PhotoReference
                }).ToList(),
                Status = inspection.Status,
                ManagerConfirmed = inspection.ManagerConfirmed,
                ManagerNotes = inspection.ManagerNotes,
                CreatedAtUtc = inspection.CreatedAtUtc,
                ConfirmedAtUtc = inspection.ConfirmedAtUtc
            };
        }
    }
}