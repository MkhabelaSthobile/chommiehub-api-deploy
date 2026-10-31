using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{

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

        public async Task<ManagerResponse?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            Manager? manager = await _managerRepository.GetByIdAsync(id, cancellationToken);

            return manager is null ? null : MapToResponse(manager);
        }

        public async Task<ManagerResponse> CreateAsync(CreateManagerRequest request, CancellationToken cancellationToken = default)
        {
            var manager = new Manager
            {
                FirebaseUid = request.FirebaseUid,
                Name = request.Name,
                Surname = request.Surname,
                Email = request.Email,
                PhoneNumber = request.PhoneNumber,
                AccommodationId = request.AccommodationId
            };

            Manager created = await _managerRepository.CreateAsync(manager, cancellationToken);

            return MapToResponse(created);
        }

        public async Task<ManagerResponse> RegisterAsync(
            string firebaseUid,
            RegisterManagerRequest request,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(firebaseUid))
            {
                throw new ArgumentException("A Firebase UID is required.", nameof(firebaseUid));
            }

            if (await _managerRepository.GetByIdAsync(firebaseUid, cancellationToken) is not null)
            {
                throw new InvalidOperationException(
                    "A manager profile already exists for this account.");
            }

            if (await _studentRepository.GetByIdAsync(firebaseUid, cancellationToken) is not null)
            {
                throw new InvalidOperationException(
                    "This account is already registered as a student.");
            }

            var manager = new Manager
            {
                FirebaseUid = firebaseUid,
                Name = request.Name.Trim(),
                Surname = request.Surname.Trim(),
                Email = request.Email.Trim(),
                PhoneNumber = request.PhoneNumber.Trim(),
                AccommodationId = string.Empty
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
            CreatedAtUtc = manager.CreatedAtUtc,
            UpdatedAtUtc = manager.UpdatedAtUtc
        };
    }
}