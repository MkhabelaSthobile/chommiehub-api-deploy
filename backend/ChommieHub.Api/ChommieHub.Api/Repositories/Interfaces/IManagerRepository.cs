using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{

    public interface IManagerRepository
    {
        Task<Manager?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<Manager> CreateAsync(Manager manager, CancellationToken cancellationToken = default);
    }
}
