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

        public StudentService(
            IStudentRepository studentRepository,
            IApplicationRepository applicationRepository,
            IAccommodationRepository accommodationRepository)
        {
            _studentRepository = studentRepository;
            _applicationRepository = applicationRepository;
            _accommodationRepository = accommodationRepository;
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
            var application = new Application
            {
                StudentId = studentId,
                AccommodationId = request.AccommodationId,
                RoomTypePreference = request.RoomTypePreference,
                RentalPeriod = request.RentalPeriod,
                SupportingInfo = request.SupportingInfo,
                Status = ApplicationStatuses.Submitted,
                DateSubmittedUtc = DateTime.UtcNow
            };

            Application created = await _applicationRepository.CreateAsync(application, cancellationToken);

            string accommodationName = await ResolveAccommodationNameAsync(created.AccommodationId, cancellationToken);

            return MapToResponse(created, accommodationName);
        }

        public async Task<IReadOnlyList<StudentApplicationResponse>> GetApplicationsAsync(string studentId, CancellationToken cancellationToken = default)
        {
            IReadOnlyList<Application> applications = await _applicationRepository.GetByStudentIdAsync(studentId, cancellationToken);

            var responses = new List<StudentApplicationResponse>(applications.Count);

            // Accommodation names are cached per request so a student with several
            // applications to the same residence does not trigger repeat Firestore reads.
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
