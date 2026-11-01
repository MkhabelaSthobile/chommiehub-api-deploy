using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{
    public sealed class AdminQueryService : IAdminQueryService
    {
        private const int DefaultAuditLimit = 50;
        private const int MaxAuditLimit = 200;
        private const int DashboardActivityCount = 5;
        private const string ArchivedFilter = "Archived";

        private static readonly string[] ListingFilters =
        [
            ListingStatuses.Draft,
            ListingStatuses.Pending,
            ListingStatuses.Approved,
            ListingStatuses.Rejected,
            ArchivedFilter
        ];

        private readonly IStudentRepository _studentRepository;
        private readonly IManagerRepository _managerRepository;
        private readonly IAccommodationRepository _accommodationRepository;
        private readonly IApplicationRepository _applicationRepository;
        private readonly IResidentRepository _residentRepository;
        private readonly IRoomRepository _roomRepository;
        private readonly IAuditRepository _auditRepository;

        public AdminQueryService(
            IStudentRepository studentRepository,
            IManagerRepository managerRepository,
            IAccommodationRepository accommodationRepository,
            IApplicationRepository applicationRepository,
            IResidentRepository residentRepository,
            IRoomRepository roomRepository,
            IAuditRepository auditRepository)
        {
            _studentRepository = studentRepository;
            _managerRepository = managerRepository;
            _accommodationRepository = accommodationRepository;
            _applicationRepository = applicationRepository;
            _residentRepository = residentRepository;
            _roomRepository = roomRepository;
            _auditRepository = auditRepository;
        }

        public async Task<AdminDashboardResponse> GetDashboardAsync(CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Student> students = await _studentRepository.GetAllAsync(cancellationToken);
            IReadOnlyList<Manager> managers = await _managerRepository.GetAllAsync(cancellationToken);
            IReadOnlyList<Accommodation> accommodations = await _accommodationRepository.GetAllAsync(cancellationToken);
            IReadOnlyList<Application> applications = await _applicationRepository.GetAllAsync(cancellationToken);
            IReadOnlyList<AuditRecord> recent = await _auditRepository.GetRecentAsync(
                DashboardActivityCount,
                cancellationToken);

            int residents = students.Count(IsResident);

            return new AdminDashboardResponse(
                Students: students.Count - residents,
                Residents: residents,
                Managers: managers.Count,
                ListingsTotal: accommodations.Count,
                ListingsDraft: CountByStatus(accommodations, ListingStatuses.Draft),
                ListingsPending: CountByStatus(accommodations, ListingStatuses.Pending),
                ListingsApproved: CountByStatus(accommodations, ListingStatuses.Approved),
                ListingsRejected: CountByStatus(accommodations, ListingStatuses.Rejected),
                ListingsArchived: accommodations.Count(accommodation => accommodation.IsArchived),
                ApplicationsTotal: applications.Count,
                ApplicationsOpen: applications.Count(application =>
                    application.Status is ApplicationStatuses.Submitted or ApplicationStatuses.UnderReview),
                RecentActivity: recent.Select(MapAudit).ToList());
        }

        public async Task<IReadOnlyList<AdminUserSummary>> GetUsersAsync(
            string? role,
            string? search,
            CancellationToken cancellationToken = default)
        {
            string? roleFilter = NormalizeRole(role);

            IReadOnlyList<Student> students = await _studentRepository.GetAllAsync(cancellationToken);
            IReadOnlyList<Manager> managers = await _managerRepository.GetAllAsync(cancellationToken);
            IReadOnlyList<Accommodation> accommodations = await _accommodationRepository.GetAllAsync(cancellationToken);

            Dictionary<string, string> accommodationNames = accommodations
                .ToDictionary(accommodation => accommodation.Id, accommodation => accommodation.Name);

            var users = new List<AdminUserSummary>();

            foreach (Student student in students)
            {
                string studentRole = IsResident(student) ? AccountRoles.Resident : AccountRoles.Student;

                if (roleFilter is null || roleFilter == studentRole)
                {
                    users.Add(ToSummary(student, studentRole, accommodationNames));
                }
            }

            if (roleFilter is null || roleFilter == AccountRoles.Manager)
            {
                users.AddRange(managers.Select(manager => ToSummary(manager, accommodationNames)));
            }

            IEnumerable<AdminUserSummary> result = users;

            if (!string.IsNullOrWhiteSpace(search))
            {
                string term = search.Trim();

                result = result.Where(user =>
                    Contains(user.Name, term)
                    || Contains(user.Surname, term)
                    || Contains(user.Email, term));
            }

            return result
                .OrderBy(user => user.Surname, StringComparer.OrdinalIgnoreCase)
                .ThenBy(user => user.Name, StringComparer.OrdinalIgnoreCase)
                .ToList();
        }

        public async Task<AdminUserDetailResponse?> GetUserAsync(
            string id,
            CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Accommodation> accommodations = await _accommodationRepository.GetAllAsync(cancellationToken);
            IReadOnlyList<Manager> managers = await _managerRepository.GetAllAsync(cancellationToken);

            Dictionary<string, string> accommodationNames = accommodations
                .ToDictionary(accommodation => accommodation.Id, accommodation => accommodation.Name);

            Dictionary<string, Manager> managersByAccommodation = ManagersByAccommodation(managers);

            Student? student = await _studentRepository.GetByIdAsync(id, cancellationToken);

            if (student is not null)
            {
                string studentRole = IsResident(student) ? AccountRoles.Resident : AccountRoles.Student;

                Accommodation? accommodation = string.IsNullOrWhiteSpace(student.AccommodationId)
                    ? null
                    : accommodations.FirstOrDefault(item => item.Id == student.AccommodationId);

                IReadOnlyList<Application> applications =
                    await _applicationRepository.GetByStudentIdAsync(id, cancellationToken);

                return new AdminUserDetailResponse(
                    ToSummary(student, studentRole, accommodationNames),
                    accommodation is null ? null : ToListingSummary(accommodation, managersByAccommodation),
                    applications
                        .Select(application => new AdminApplicationSummary(
                            application.Id,
                            application.AccommodationId,
                            accommodationNames.GetValueOrDefault(application.AccommodationId, string.Empty),
                            application.Status,
                            application.DateSubmittedUtc))
                        .ToList());
            }

            Manager? manager = await _managerRepository.GetByIdAsync(id, cancellationToken);

            if (manager is null)
            {
                return null;
            }

            Accommodation? managerAccommodation = string.IsNullOrWhiteSpace(manager.AccommodationId)
                ? null
                : accommodations.FirstOrDefault(item => item.Id == manager.AccommodationId);

            return new AdminUserDetailResponse(
                ToSummary(manager, accommodationNames),
                managerAccommodation is null ? null : ToListingSummary(managerAccommodation, managersByAccommodation),
                []);
        }

        public async Task<IReadOnlyList<AdminListingSummary>> GetListingsAsync(
            string? status,
            CancellationToken cancellationToken = default)
        {
            string? filter = NormalizeListingFilter(status);

            IReadOnlyList<Accommodation> accommodations = await _accommodationRepository.GetAllAsync(cancellationToken);
            IReadOnlyList<Manager> managers = await _managerRepository.GetAllAsync(cancellationToken);

            Dictionary<string, Manager> managersByAccommodation = ManagersByAccommodation(managers);

            IEnumerable<Accommodation> query = accommodations;

            if (filter == ArchivedFilter)
            {
                query = query.Where(accommodation => accommodation.IsArchived);
            }
            else if (filter is not null)
            {
                query = query.Where(accommodation =>
                    !accommodation.IsArchived && accommodation.ReviewStatus == filter);
            }

            return query
                .OrderBy(accommodation => accommodation.SubmittedAtUtc ?? DateTime.MaxValue)
                .ThenBy(accommodation => accommodation.Name, StringComparer.OrdinalIgnoreCase)
                .Select(accommodation => ToListingSummary(accommodation, managersByAccommodation))
                .ToList();
        }

        public async Task<AdminListingDetailResponse?> GetListingAsync(
            string id,
            CancellationToken cancellationToken = default)
        {
            Accommodation? accommodation = await _accommodationRepository.GetByIdAsync(id, cancellationToken);

            if (accommodation is null)
            {
                return null;
            }

            Manager? manager = await _managerRepository.GetByAccommodationIdAsync(id, cancellationToken);
            IReadOnlyList<Resident> residents = await _residentRepository.GetByAccommodationIdAsync(id, cancellationToken);
            IReadOnlyList<Room> rooms = await _roomRepository.GetByAccommodationIdAsync(id, cancellationToken);
            IReadOnlyList<Application> applications =
                await _applicationRepository.GetByAccommodationIdAsync(id, cancellationToken);

            var names = new Dictionary<string, string> { [accommodation.Id] = accommodation.Name };

            var residentSummaries = new List<AdminUserSummary>();

            foreach (Resident resident in residents.Where(item => item.IsActive))
            {
                Student? student = await _studentRepository.GetByIdAsync(resident.StudentId, cancellationToken);

                if (student is not null)
                {
                    residentSummaries.Add(ToSummary(student, AccountRoles.Resident, names));
                }
            }

            return new AdminListingDetailResponse(
                MapAccommodation(accommodation),
                manager is null ? null : ToSummary(manager, names),
                residentSummaries,
                rooms
                    .Select(room => new AdminRoomSummary(
                        room.Id,
                        room.RoomType,
                        room.Price,
                        room.Capacity,
                        room.Availability))
                    .ToList(),
                applications.Count);
        }

        public async Task<IReadOnlyList<AuditRecordResponse>> GetAuditAsync(
            int limit,
            CancellationToken cancellationToken = default)
        {
            int safeLimit = limit <= 0 ? DefaultAuditLimit : Math.Min(limit, MaxAuditLimit);

            IReadOnlyList<AuditRecord> records =
                await _auditRepository.GetRecentAsync(safeLimit, cancellationToken);

            return records.Select(MapAudit).ToList();
        }

        private static bool IsResident(Student student) =>
            string.Equals(student.UserType, "Resident", StringComparison.OrdinalIgnoreCase);

        private static int CountByStatus(IReadOnlyList<Accommodation> accommodations, string status) =>
            accommodations.Count(accommodation =>
                !accommodation.IsArchived && accommodation.ReviewStatus == status);

        private static bool Contains(string value, string term) =>
            value.Contains(term, StringComparison.OrdinalIgnoreCase);

        private static string? NormalizeRole(string? role)
        {
            if (string.IsNullOrWhiteSpace(role))
            {
                return null;
            }

            string? match = new[] { AccountRoles.Student, AccountRoles.Resident, AccountRoles.Manager }
                .FirstOrDefault(item => string.Equals(item, role.Trim(), StringComparison.OrdinalIgnoreCase));

            return match ?? throw new ArgumentException("Role must be Student, Resident or Manager.");
        }

        private static string? NormalizeListingFilter(string? status)
        {
            if (string.IsNullOrWhiteSpace(status))
            {
                return null;
            }

            string? match = ListingFilters
                .FirstOrDefault(item => string.Equals(item, status.Trim(), StringComparison.OrdinalIgnoreCase));

            return match ?? throw new ArgumentException(
                "Status must be Draft, Pending, Approved, Rejected or Archived.");
        }

        private static Dictionary<string, Manager> ManagersByAccommodation(IReadOnlyList<Manager> managers) =>
            managers
                .Where(manager => !string.IsNullOrWhiteSpace(manager.AccommodationId))
                .GroupBy(manager => manager.AccommodationId)
                .ToDictionary(group => group.Key, group => group.First());

        private static AdminUserSummary ToSummary(
            Student student,
            string role,
            Dictionary<string, string> accommodationNames) =>
            new(
                student.Id,
                student.Name,
                student.Surname,
                student.Email,
                student.PhoneNumber,
                role,
                student.AccommodationId,
                accommodationNames.GetValueOrDefault(student.AccommodationId, string.Empty),
                student.IsDisabled,
                student.CreatedAtUtc);

        private static AdminUserSummary ToSummary(
            Manager manager,
            Dictionary<string, string> accommodationNames) =>
            new(
                manager.Id,
                manager.Name,
                manager.Surname,
                manager.Email,
                manager.PhoneNumber,
                AccountRoles.Manager,
                manager.AccommodationId,
                accommodationNames.GetValueOrDefault(manager.AccommodationId, string.Empty),
                manager.IsDisabled,
                manager.CreatedAtUtc);

        private static AdminListingSummary ToListingSummary(
            Accommodation accommodation,
            Dictionary<string, Manager> managersByAccommodation)
        {
            managersByAccommodation.TryGetValue(accommodation.Id, out Manager? manager);

            return new AdminListingSummary(
                accommodation.Id,
                accommodation.Name,
                accommodation.Address,
                accommodation.InstitutionArea,
                accommodation.ReviewStatus,
                accommodation.IsArchived,
                accommodation.IsActive,
                manager?.Id ?? string.Empty,
                manager is null ? string.Empty : $"{manager.Name} {manager.Surname}".Trim(),
                accommodation.SubmittedAtUtc,
                accommodation.ReviewedAtUtc);
        }

        private static ManagerAccommodationResponse MapAccommodation(Accommodation accommodation) =>
            new()
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

        private static AuditRecordResponse MapAudit(AuditRecord record) =>
            new(
                record.Id,
                record.AdminId,
                record.Action,
                record.TargetType,
                record.TargetId,
                record.TargetName,
                record.Reason,
                record.CreatedAtUtc);
    }
}