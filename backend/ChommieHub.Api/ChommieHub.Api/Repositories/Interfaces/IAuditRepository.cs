using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    public interface IAuditRepository
    {
        Task<AuditRecord> AddAsync(AuditRecord record, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<AuditRecord>> GetRecentAsync(int limit, CancellationToken cancellationToken = default);
    }
}