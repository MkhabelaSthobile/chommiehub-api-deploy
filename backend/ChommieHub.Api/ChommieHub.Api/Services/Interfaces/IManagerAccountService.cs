using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services
{

    public interface IManagerAccountService
    {
        Task<ManagerResponse?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<ManagerResponse> CreateAsync(CreateManagerRequest request, CancellationToken cancellationToken = default);

        Task<ManagerResponse> RegisterAsync(string firebaseUid, RegisterManagerRequest request, CancellationToken cancellationToken = default);
    }
}
