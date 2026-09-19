using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines persistence operations for student accommodation applications.
    /// Both students and managers use the same Firestore applications collection.
    /// </summary>
    public interface IApplicationRepository
    {
        // Student creates a new accommodation application.
        Task<Application> CreateAsync(
            Application application,
            CancellationToken cancellationToken = default
        );

        // Student retrieves all of their submitted applications.
        Task<IReadOnlyList<Application>> GetByStudentIdAsync(
            string studentId,
            CancellationToken cancellationToken = default
        );

        // Manager retrieves applications belonging to an accommodation.
        Task<IReadOnlyList<Application>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default
        );

        // Retrieve one application by its Firestore document ID.
        Task<Application?> GetByIdAsync(
            string id,
            CancellationToken cancellationToken = default
        );

        // Manager updates an application when reviewing/assigning a room.
        Task<bool> UpdateAsync(
            Application application,
            CancellationToken cancellationToken = default
        );
    }
}