using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services.Interfaces;

namespace ChommieHub.Api.Services
{
    public sealed class AdminActionService : IAdminActionService
    {
        private const int MaxReasonLength = 500;

        private readonly IAccommodationRepository _accommodationRepository;
        private readonly IManagerRepository _managerRepository;
        private readonly IStudentRepository _studentRepository;
        private readonly IApplicationRepository _applicationRepository;
        private readonly IResidentRepository _residentRepository;
        private readonly IRoomRepository _roomRepository;
        private readonly IAuditRepository _auditRepository;
        private readonly IFirebaseAccountAdmin _accountAdmin;
        private readonly IUserNotifier _notifier;

        public AdminActionService(
            IAccommodationRepository accommodationRepository,
            IManagerRepository managerRepository,
            IStudentRepository studentRepository,
            IApplicationRepository applicationRepository,
            IResidentRepository residentRepository,
            IRoomRepository roomRepository,
            IAuditRepository auditRepository,
            IFirebaseAccountAdmin accountAdmin,
            IUserNotifier notifier)
        {
            _accommodationRepository = accommodationRepository;
            _managerRepository = managerRepository;
            _studentRepository = studentRepository;
            _applicationRepository = applicationRepository;
            _residentRepository = residentRepository;
            _roomRepository = roomRepository;
            _auditRepository = auditRepository;
            _accountAdmin = accountAdmin;
            _notifier = notifier;
        }

        public async Task<AdminActionResponse> ApproveListingAsync(
            string adminId,
            string listingId,
            CancellationToken cancellationToken = default)
        {
            Accommodation listing = await GetListingAsync(listingId, cancellationToken);

            if (listing.IsArchived || listing.ReviewStatus != ListingStatuses.Pending)
            {
                throw new InvalidOperationException("Only a pending listing can be approved.");
            }

            listing.ReviewStatus = ListingStatuses.Approved;
            listing.ReviewReason = string.Empty;
            listing.ReviewedByUid = adminId;
            listing.ReviewedAtUtc = DateTime.UtcNow;
            listing.UpdatedAtUtc = DateTime.UtcNow;

            await _accommodationRepository.UpdateAsync(listing, cancellationToken);

            await WriteAuditAsync(
                adminId, AuditActions.ListingApproved, "Accommodation", listing.Id, listing.Name,
                string.Empty, cancellationToken);

            await NotifyListingManagerAsync(
                listing.Id,
                "Accommodation approved",
                $"{listing.Name} was approved and is now visible to students.",
                cancellationToken);

            return new AdminActionResponse(
                AuditActions.ListingApproved, "Accommodation", listing.Id, "Listing approved.");
        }

        public async Task<AdminActionResponse> RejectListingAsync(
            string adminId,
            string listingId,
            string? reason,
            CancellationToken cancellationToken = default)
        {
            string cleanReason = RequireReason(reason);
            Accommodation listing = await GetListingAsync(listingId, cancellationToken);

            if (listing.IsArchived || listing.ReviewStatus != ListingStatuses.Pending)
            {
                throw new InvalidOperationException("Only a pending listing can be rejected.");
            }

            listing.ReviewStatus = ListingStatuses.Rejected;
            listing.ReviewReason = cleanReason;
            listing.ReviewedByUid = adminId;
            listing.ReviewedAtUtc = DateTime.UtcNow;
            listing.UpdatedAtUtc = DateTime.UtcNow;

            await _accommodationRepository.UpdateAsync(listing, cancellationToken);

            await WriteAuditAsync(
                adminId, AuditActions.ListingRejected, "Accommodation", listing.Id, listing.Name,
                cleanReason, cancellationToken);

            await NotifyListingManagerAsync(
                listing.Id,
                "Accommodation rejected",
                $"{listing.Name} was not approved: {cleanReason}",
                cancellationToken);

            return new AdminActionResponse(
                AuditActions.ListingRejected, "Accommodation", listing.Id, "Listing rejected.");
        }

        public async Task<AdminActionResponse> ArchiveListingAsync(
            string adminId,
            string listingId,
            string? reason,
            CancellationToken cancellationToken = default)
        {
            string cleanReason = RequireReason(reason);
            Accommodation listing = await GetListingAsync(listingId, cancellationToken);

            if (listing.IsArchived)
            {
                throw new InvalidOperationException("This listing is already archived.");
            }

            listing.IsArchived = true;
            listing.UpdatedAtUtc = DateTime.UtcNow;

            await _accommodationRepository.UpdateAsync(listing, cancellationToken);

            await WriteAuditAsync(
                adminId, AuditActions.ListingArchived, "Accommodation", listing.Id, listing.Name,
                cleanReason, cancellationToken);

            await NotifyListingManagerAsync(
                listing.Id,
                "Accommodation archived",
                $"{listing.Name} was archived by an administrator: {cleanReason}",
                cancellationToken);

            return new AdminActionResponse(
                AuditActions.ListingArchived, "Accommodation", listing.Id, "Listing archived.");
        }

        public async Task<AdminActionResponse> UnarchiveListingAsync(
            string adminId,
            string listingId,
            CancellationToken cancellationToken = default)
        {
            Accommodation listing = await GetListingAsync(listingId, cancellationToken);

            if (!listing.IsArchived)
            {
                throw new InvalidOperationException("This listing is not archived.");
            }

            listing.IsArchived = false;
            listing.UpdatedAtUtc = DateTime.UtcNow;

            await _accommodationRepository.UpdateAsync(listing, cancellationToken);

            await WriteAuditAsync(
                adminId, AuditActions.ListingUnarchived, "Accommodation", listing.Id, listing.Name,
                string.Empty, cancellationToken);

            await NotifyListingManagerAsync(
                listing.Id,
                "Accommodation restored",
                $"{listing.Name} was restored by an administrator.",
                cancellationToken);

            return new AdminActionResponse(
                AuditActions.ListingUnarchived, "Accommodation", listing.Id, "Listing restored.");
        }

        public async Task<AdminActionResponse> DeleteListingAsync(
            string adminId,
            string listingId,
            string? reason,
            CancellationToken cancellationToken = default)
        {
            string cleanReason = RequireReason(reason);
            Accommodation listing = await GetListingAsync(listingId, cancellationToken);

            IReadOnlyList<Resident> residents =
                await _residentRepository.GetByAccommodationIdAsync(listingId, cancellationToken);
            IReadOnlyList<Room> rooms =
                await _roomRepository.GetByAccommodationIdAsync(listingId, cancellationToken);
            IReadOnlyList<Application> applications =
                await _applicationRepository.GetByAccommodationIdAsync(listingId, cancellationToken);

            if (residents.Any(resident => resident.IsActive) || rooms.Count > 0 || applications.Count > 0)
            {
                throw new InvalidOperationException(
                    "This listing has rooms, applications or residents. Archive it instead.");
            }

            Manager? manager =
                await _managerRepository.GetByAccommodationIdAsync(listingId, cancellationToken);

            await _accommodationRepository.DeleteAsync(listingId, cancellationToken);

            if (manager is not null)
            {
                manager.AccommodationId = string.Empty;
                manager.UpdatedAtUtc = DateTime.UtcNow;
                await _managerRepository.UpdateAsync(manager, cancellationToken);
            }

            await WriteAuditAsync(
                adminId, AuditActions.ListingDeleted, "Accommodation", listing.Id, listing.Name,
                cleanReason, cancellationToken);

            if (manager is not null)
            {
                await _notifier.NotifyAsync(
                    manager.Id,
                    "Accommodation removed",
                    $"{listing.Name} was removed by an administrator: {cleanReason}");
            }

            return new AdminActionResponse(
                AuditActions.ListingDeleted, "Accommodation", listing.Id, "Listing deleted.");
        }

        private async Task<Accommodation> GetListingAsync(
            string listingId,
            CancellationToken cancellationToken)
        {
            Accommodation? listing =
                await _accommodationRepository.GetByIdAsync(listingId, cancellationToken);

            return listing ?? throw new KeyNotFoundException("The accommodation was not found.");
        }

        private async Task NotifyListingManagerAsync(
            string accommodationId,
            string title,
            string body,
            CancellationToken cancellationToken)
        {
            Manager? manager =
                await _managerRepository.GetByAccommodationIdAsync(accommodationId, cancellationToken);

            if (manager is not null)
            {
                await _notifier.NotifyAsync(manager.Id, title, body);
            }
        }

        private async Task WriteAuditAsync(
            string adminId,
            string action,
            string targetType,
            string targetId,
            string targetName,
            string reason,
            CancellationToken cancellationToken)
        {
            await _auditRepository.AddAsync(
                new AuditRecord
                {
                    AdminId = adminId,
                    Action = action,
                    TargetType = targetType,
                    TargetId = targetId,
                    TargetName = targetName,
                    Reason = reason
                },
                cancellationToken);
        }

        private static string RequireReason(string? reason)
        {
            string trimmed = reason?.Trim() ?? string.Empty;

            if (trimmed.Length == 0)
            {
                throw new ArgumentException("A reason is required.");
            }

            if (trimmed.Length > MaxReasonLength)
            {
                throw new ArgumentException(
                    $"The reason must be {MaxReasonLength} characters or fewer.");
            }

            return trimmed;
        }
    }
}