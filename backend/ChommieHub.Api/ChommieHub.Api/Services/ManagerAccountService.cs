using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;

namespace ChommieHub.Api.Services
{

    public sealed class ManagerAccountService : IManagerAccountService
    {
        private readonly IManagerRepository _managerRepository;

        public ManagerAccountService(IManagerRepository managerRepository)
        {
            _managerRepository = managerRepository;
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
