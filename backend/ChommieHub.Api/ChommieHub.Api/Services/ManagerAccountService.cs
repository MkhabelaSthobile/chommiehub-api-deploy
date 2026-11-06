using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Creates manager profiles only through the administrator-protected controller.
    /// This service never exposes public manager self-registration.
    /// </summary>
    public sealed class ManagerAccountService : IManagerAccountService
    {
        private readonly IManagerRepository _managerRepository;
        private readonly IStudentRepository _studentRepository;

        public ManagerAccountService(
            IManagerRepository managerRepository,
            IStudentRepository studentRepository)
        {
            _managerRepository = managerRepository;
            _studentRepository = studentRepository;
        }

        public async Task<ManagerResponse?> GetByIdAsync(
            string id,
            CancellationToken cancellationToken = default)
        {
            Manager? manager = await _managerRepository.GetByIdAsync(id, cancellationToken);
            return manager is null ? null : MapToResponse(manager);
        }

        public async Task<ManagerResponse> CreateAsync(
            CreateManagerRequest request,
            CancellationToken cancellationToken = default)
        {
            if (request is null)
            {
                throw new ArgumentException("Manager information is required.", nameof(request));
            }

            string uid = request.FirebaseUid?.Trim() ?? string.Empty;

            if (uid.Length == 0)
            {
                throw new ArgumentException("A Firebase UID is required.", nameof(request));
            }

            // Prevent duplicate profiles and accidental student-to-manager privilege
            // escalation. The administrator must resolve an existing student role.
            if (await _managerRepository.GetByIdAsync(uid, cancellationToken) is not null)
            {
                throw new InvalidOperationException("A manager profile already exists for this Firebase account.");
            }

            if (await _studentRepository.GetByIdAsync(uid, cancellationToken) is not null)
            {
                throw new InvalidOperationException("This Firebase account already belongs to a student.");
            }

            if (string.IsNullOrWhiteSpace(request.Name)
                || string.IsNullOrWhiteSpace(request.Surname)
                || string.IsNullOrWhiteSpace(request.Email)
                || string.IsNullOrWhiteSpace(request.PhoneNumber))
            {
                throw new ArgumentException("Name, surname, email and phone are required.", nameof(request));
            }

            var manager = new Manager
            {
                FirebaseUid = uid,
                Name = request.Name.Trim(),
                Surname = request.Surname.Trim(),
                Email = request.Email.Trim(),
                PhoneNumber = request.PhoneNumber.Trim(),
                // A newly appointed manager initially has no accommodation.
                // They create a draft that the administrator must approve.
                AccommodationId = request.AccommodationId?.Trim() ?? string.Empty
            };

            Manager created = await _managerRepository.CreateAsync(manager, cancellationToken);
            return MapToResponse(created);
        }

        private static ManagerResponse MapToResponse(Manager manager) => new()
        {
            Id = manager.Id,
            FirebaseUid = manager.FirebaseUid,
            Name = manager.Name,
            Surname = manager.Surname,
            Email = manager.Email,
            PhoneNumber = manager.PhoneNumber,
            AccommodationId = manager.AccommodationId,
            IsDisabled = manager.IsDisabled,
            CreatedAtUtc = manager.CreatedAtUtc,
            UpdatedAtUtc = manager.UpdatedAtUtc
        };
    }
}
