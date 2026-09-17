using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines atomic persistence operations that span multiple
    /// residence-management documents.
    /// </summary>
    public interface IManagerWorkflowRepository
    {
        Task<Resident?> AssignRoomAsync(
            string applicationId,
            string roomId,
            CancellationToken cancellationToken = default);
    }
}