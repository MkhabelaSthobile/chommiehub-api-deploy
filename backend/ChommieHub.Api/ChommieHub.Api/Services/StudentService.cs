using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Coordinates student-facing business logic: profile management, settings, and
    /// accommodation-application submission/retrieval. Preserves the Firebase UID identity
    /// contract (Firebase UID -> Student.Id -> Application.StudentId) throughout.
    /// </summary>
    public sealed class StudentService : IStudentService
    {
        private readonly IStudentRepository _studentRepository;
        private readonly IApplicationRepository _applicationRepository;
        private readonly IAccommodationRepository _accommodationRepository;
        private readonly IRoomRepository _roomRepository;

        public StudentService(IStudentRepository studentRepository, IApplicationRepository applicationRepository, IAccommodationRepository accommodationRepository, IRoomRepository roomRepository)
        {
            _studentRepository = studentRepository;
            _applicationRepository = applicationRepository;
            _accommodationRepository = accommodationRepository;
            _roomRepository = roomRepository;
        }

        public async Task<StudentResponse?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            Student? student = await _studentRepository.GetByIdAsync(id, cancellationToken);
            return student is null ? null : MapToResponse(student);
        }

        public async Task<StudentResponse> CreateAsync(CreateStudentRequest request, CancellationToken cancellationToken = default)
        {
            var student = new Student
            {
                FirebaseUid = request.FirebaseUid,
                Name = request.Name,
                Surname = request.Surname,
                Email = request.Email,
                PhoneNumber = request.PhoneNumber,
                PreferredLanguage = string.IsNullOrWhiteSpace(request.PreferredLanguage) ? "English" : request.PreferredLanguage,
                NotificationsEnabled = true,
                UserType = "Applicant"
            };

            Student created = await _studentRepository.CreateAsync(student, cancellationToken);
            return MapToResponse(created);
        }

        public async Task<StudentResponse?> UpdateAsync(string id, UpdateStudentRequest request, CancellationToken cancellationToken = default)
        {
            Student? existing = await _studentRepository.GetByIdAsync(id, cancellationToken);

            if (existing is null)
            {
                return null;
            }

            existing.Name = request.Name;
            existing.Surname = request.Surname;
            existing.PhoneNumber = request.PhoneNumber;
            existing.PreferredLanguage = string.IsNullOrWhiteSpace(request.PreferredLanguage) ? "English" : request.PreferredLanguage;
            existing.UpdatedAtUtc = DateTime.UtcNow;

            bool updated = await _studentRepository.UpdateAsync(existing, cancellationToken);

            return updated ? MapToResponse(existing) : null;
        }

        public async Task<StudentResponse?> UpdateSettingsAsync(string id, UpdateStudentSettingsRequest request, CancellationToken cancellationToken = default)
        {
            Student? existing = await _studentRepository.GetByIdAsync(id, cancellationToken);

            if (existing is null)
            {
                return null;
            }

            existing.PreferredLanguage = string.IsNullOrWhiteSpace(request.PreferredLanguage) ? "English" : request.PreferredLanguage;
            existing.NotificationsEnabled = request.NotificationsEnabled;
            existing.UpdatedAtUtc = DateTime.UtcNow;

            bool updated = await _studentRepository.UpdateAsync(existing, cancellationToken);

            return updated ? MapToResponse(existing) : null;
        }

        public async Task<StudentApplicationResponse> SubmitApplicationAsync(string studentId, SubmitApplicationRequest request, CancellationToken cancellationToken = default)
        {
            Student? student = await _studentRepository.GetByIdAsync(studentId, cancellationToken);

            if (student is null)
            {
                throw new KeyNotFoundException("Student profile was not found.");
            }

            Accommodation? accommodation = await _accommodationRepository.GetByIdAsync(request.AccommodationId, cancellationToken);

            if (accommodation is null)
            {
                throw new KeyNotFoundException("The selected accommodation was not found.");
            }

            if (!accommodation.IsActive)
            {
                throw new InvalidOperationException("The selected accommodation is not currently accepting applications.");
            }

            Room? room = await _roomRepository.GetByIdAsync(request.RoomId, cancellationToken);

            if (room is null)
            {
                throw new KeyNotFoundException("The selected room was not found.");
            }

            if (room.AccommodationId != accommodation.Id)
            {
                throw new InvalidOperationException("The selected room does not belong to the selected accommodation.");
            }

            if (!room.Availability)
            {
                throw new InvalidOperationException("The selected room is no longer available.");
            }

            var application = new Application
            {
                StudentId = studentId,
                AccommodationId = accommodation.Id,
                RequestedRoomId = room.Id,
                RoomTypePreference = room.RoomType,
                RentalPeriod = request.RentalPeriod.Trim(),
                SupportingInfo = request.SupportingInfo.Trim(),
                Status = ApplicationStatuses.Submitted,
                DateSubmittedUtc = DateTime.UtcNow
            };

            Application created = await _applicationRepository.CreateAsync(application, cancellationToken);

            return MapToResponse(created, accommodation.Name);
        }

        public async Task<IReadOnlyList<StudentApplicationResponse>> GetApplicationsAsync(string studentId, CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Application> applications = await _applicationRepository.GetByStudentIdAsync(studentId, cancellationToken);

            var responses = new List<StudentApplicationResponse>(applications.Count);
            var accommodationNameCache = new Dictionary<string, string>();

            foreach (Application application in applications)
            {
                if (!accommodationNameCache.TryGetValue(application.AccommodationId, out string? name))
                {
                    name = await ResolveAccommodationNameAsync(application.AccommodationId, cancellationToken);
                    accommodationNameCache[application.AccommodationId] = name;
                }

                responses.Add(MapToResponse(application, name));
            }

            return responses;
        }

        private async Task<string> ResolveAccommodationNameAsync(string accommodationId, CancellationToken cancellationToken)
        {
            if (string.IsNullOrWhiteSpace(accommodationId))
            {
                return string.Empty;
            }

            Accommodation? accommodation = await _accommodationRepository.GetByIdAsync(accommodationId, cancellationToken);
            return accommodation?.Name ?? string.Empty;
        }

        private static StudentResponse MapToResponse(Student student) => new()
        {
            Id = student.Id,
            FirebaseUid = student.FirebaseUid,
            Name = student.Name,
            Surname = student.Surname,
            Email = student.Email,
            PhoneNumber = student.PhoneNumber,
            PreferredLanguage = student.PreferredLanguage,
            NotificationsEnabled = student.NotificationsEnabled,
            UserType = student.UserType,
            AccommodationId = student.AccommodationId,
            RoomId = student.RoomId,
            CreatedAtUtc = student.CreatedAtUtc,
            UpdatedAtUtc = student.UpdatedAtUtc
        };

        private static StudentApplicationResponse MapToResponse(Application application, string accommodationName) => new()
        {
            Id = application.Id,
            AccommodationId = application.AccommodationId,
            AccommodationName = accommodationName,
            RequestedRoomId = application.RequestedRoomId,
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
}