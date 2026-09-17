using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Implements the core residence-manager workflow including application review, room assignment, resident creation and dashboard statistics
    /// </summary>
    public sealed class ManagerService : IManagerService
    {
        private readonly IApplicationRepository _applicationRepository;
        private readonly IResidentRepository _residentRepository;
        private readonly IRoomRepository _roomRepository;
        private readonly IManagerWorkflowRepository _managerWorkflowRepository;

        public ManagerService(IApplicationRepository applicationRepository, IResidentRepository residentRepository, IRoomRepository roomRepository, IManagerWorkflowRepository managerWorkflowRepository)
        {
            _applicationRepository = applicationRepository;
            _residentRepository = residentRepository;
            _roomRepository = roomRepository;
            _managerWorkflowRepository = managerWorkflowRepository;
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

            //Only final manager decisions are accepted by this operation.
            bool approved = decision.Equals(ApplicationStatuses.Approved, StringComparison.OrdinalIgnoreCase);

            bool declined = decision.Equals(ApplicationStatuses.Declined, StringComparison.OrdinalIgnoreCase);

            if (!approved && !declined)
            {
                throw new InvalidOperationException( "Decision must be either Approved or Declined.");
            }

            //Prevent an already-finalised application from being reviewed a second time
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

        /// <summary>
        /// Assigns a room through an atomic Firestore transaction so that the room, application and resident records cannot become partially updated
        /// </summary>
        public async Task<ResidentResponse?> AssignRoomAsync(string applicationId, AssignRoomRequest request, CancellationToken cancellationToken = default)
        {
            Resident? resident = await _managerWorkflowRepository.AssignRoomAsync( 
                applicationId,
                request.RoomId,
                cancellationToken);

            return resident is null
                ? null
                : MapResident(resident);
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

        /// <summary>
        /// Calculates live dashboard totals from applications, residents and rooms belonging to the selected accommodation
        /// </summary>
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