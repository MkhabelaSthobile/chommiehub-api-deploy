using ChommieHub.Api.Models;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Defines persistence operations for ChommieHub student profiles
    /// The Firebase user identifier will provide the link between authentication and the backend student record
    /// </summary>
    public interface IStudentRepository
    {
        Task<Student?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<Student> CreateAsync(Student student, CancellationToken cancellationToken = default);

        Task<bool> UpdateAsync(Student student, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<Student>> GetAllAsync(CancellationToken cancellationToken = default);
    }
}