using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{
    public sealed class ManagerService : IManagerService
    {
        private readonly IApplicationRepository _applicationRepository;
        private readonly IResidentRepository _residentRepository;
        private readonly IRoomRepository _roomRepository;

        public ManagerService(IApplicationRepository applicationRepository, IResidentRepository residentRepository, IRoomRepository roomRepository)
        {
            _applicationRepository = applicationRepository;
            _residentRepository = residentRepository;
            _roomRepository = roomRepository;
        }

        public async Task<IReadOnlyList<ManagerApplicationSummaryResponse>> GetApplicationsAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Application> applications = await _applicationRepository.GetByAccommodationIdAsync(
                accommodationId,
                cancellationToken);

            return applications
                .Select(MapApplicationSummary)
                .ToList();
        }

        public async Task<ManagerApplicationDetailResponse?> GetApplicationAsync(string applicationId, CancellationToken cancellationToken = default)
        {
            Application? application = await _applicationRepository.GetByIdAsync(
                applicationId,
                cancellationToken);

            return application is null 
                ? null : MapApplicationDetail(application);
        }

        public async Task<ManagerApplicationDetailResponse?> ReviewApplicationAsync(string applicationId, ReviewApplicationRequest request, CancellationToken cancellationToken = default)
        {
            Application? application = await _applicationRepository.GetByIdAsync(
                applicationId,
                cancellationToken);

            if (application is null)
            {
                return null;
            }

            string decision = request.Decision.Trim();

            bool approved = decision.Equals(ApplicationStatuses.Approved, StringComparison.OrdinalIgnoreCase);

            bool declined = decision.Equals(ApplicationStatuses.Declined, StringComparison.OrdinalIgnoreCase);

            if (!approved && !declined)
            {
                throw new InvalidOperationException( "Decision must be either Approved or Declined.");
            }

            if (application.Status == ApplicationStatuses.Approved || application.Status == ApplicationStatuses.Declined)
            {
                throw new InvalidOperationException("This application has already been reviewed.");
            }

            application.Status = approved
                ? ApplicationStatuses.Approved
                : ApplicationStatuses.Declined;

            application.ReviewNotes = request.ReviewNotes.Trim();

            application.DateReviewedUtc = DateTime.UtcNow;

            await _applicationRepository.UpdateAsync(
                application,
                cancellationToken);

            return MapApplicationDetail(application);
        }

        public async Task<ResidentResponse?> AssignRoomAsync(string applicationId, AssignRoomRequest request, CancellationToken cancellationToken = default)
        {
            Application? application = await _applicationRepository.GetByIdAsync(
                applicationId,
                cancellationToken);

            if (application is null)
            {
                return null;
            }

            if (application.Status != ApplicationStatuses.Approved)
            {
                throw new InvalidOperationException("Only approved applications can receive a room assignment.");
            }

            Resident? existingResident = await _residentRepository.GetByApplicationIdAsync(
                applicationId,
                cancellationToken);

            if (existingResident is not null)
            {
                throw new InvalidOperationException("A resident has already been created for this application.");
            }

            Room? room = await _roomRepository.GetByIdAsync(
                request.RoomId,
                cancellationToken);

            if (room is null)
            {
                throw new KeyNotFoundException("The selected room was not found.");
            }

            if (room.AccommodationId != application.AccommodationId)
            {
                throw new InvalidOperationException("The selected room does not belong to the application's accommodation.");
            }

            if (!room.Availability)
            {
                throw new InvalidOperationException("The selected room is not available.");
            }

            room.Availability = false;
            room.UpdatedAtUtc = DateTime.UtcNow;

            await _roomRepository.UpdateAsync(
                room,
                cancellationToken);

            application.AssignedRoomId = room.Id;

            await _applicationRepository.UpdateAsync(
                application,
                cancellationToken);

            var resident = new Resident
            {
                StudentId = application.StudentId,
                AccommodationId = application.AccommodationId,
                RoomId = room.Id,
                ApplicationId = application.Id,
                IsActive = true,
                CreatedAtUtc = DateTime.UtcNow
            };

            Resident createdResident = await _residentRepository.CreateAsync(
                resident,
                cancellationToken);

            return MapResident(createdResident);
        }

        public async Task<IReadOnlyList<ResidentResponse>> GetResidentsAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Resident> residents =  await _residentRepository.GetByAccommodationIdAsync(
                accommodationId,
                cancellationToken);

            return residents
                .Select(MapResident)
                .ToList();
        }

        public async Task<ManagerDashboardResponse> GetDashboardAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Application> applications = await _applicationRepository.GetByAccommodationIdAsync(
                accommodationId,
                cancellationToken);

            IReadOnlyList<Resident> residents = await _residentRepository.GetByAccommodationIdAsync(
                accommodationId,
                cancellationToken);

            IReadOnlyList<Room> rooms = await _roomRepository.GetByAccommodationIdAsync(
                accommodationId, 
                cancellationToken);

            return new ManagerDashboardResponse
            {
                AccommodationId = accommodationId,

                TotalApplications = applications.Count,

                PendingApplications = applications.Count(application => application.Status == ApplicationStatuses.Submitted || application.Status == ApplicationStatuses.UnderReview),

                ApprovedApplications = applications.Count(application => application.Status == ApplicationStatuses.Approved),

                DeclinedApplications = applications.Count(application => application.Status == ApplicationStatuses.Declined),

                ActiveResidents = residents.Count(resident => resident.IsActive),

                TotalRooms = rooms.Count,

                AvailableRooms = rooms.Count(room => room.Availability),

                UnavailableRooms = rooms.Count(room => !room.Availability)
            };
        }

        private static ManagerApplicationSummaryResponse MapApplicationSummary(Application application)
        {
            return new ManagerApplicationSummaryResponse
            {
                Id = application.Id,
                StudentId = application.StudentId,
                AccommodationId = application.AccommodationId,
                RoomTypePreference = application.RoomTypePreference,
                Status = application.Status,
                AssignedRoomId = application.AssignedRoomId,
                DateSubmittedUtc = application.DateSubmittedUtc
            };
        }

        private static ManagerApplicationDetailResponse MapApplicationDetail(Application application)
        {
            return new ManagerApplicationDetailResponse
            {
                Id = application.Id,
                StudentId = application.StudentId,
                AccommodationId = application.AccommodationId,
                RoomTypePreference = application.RoomTypePreference,
                RentalPeriod = application.RentalPeriod,
                SupportingInfo = application.SupportingInfo,
                Status = application.Status,
                AssignedRoomId = application.AssignedRoomId,
                ReviewNotes = application.ReviewNotes,
                DateSubmittedUtc = application.DateSubmittedUtc,
                DateReviewedUtc = application.DateReviewedUtc
            };
        }

        private static ResidentResponse MapResident(Resident resident)
        {
            return new ResidentResponse
            {
                Id = resident.Id,
                StudentId = resident.StudentId,
                AccommodationId = resident.AccommodationId,
                RoomId = resident.RoomId,
                ApplicationId = resident.ApplicationId,
                IsActive = resident.IsActive,
                CreatedAtUtc = resident.CreatedAtUtc
            };
        }
    }
}