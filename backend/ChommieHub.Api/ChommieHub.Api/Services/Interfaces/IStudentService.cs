using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Defines the business operations available for the student side of ChommieHub:
    /// profile, settings, and accommodation-application submission/retrieval.
    /// </summary>
    public interface IStudentService
    {
        Task<StudentResponse?> GetByIdAsync(string id, CancellationToken cancellationToken = default);

        Task<StudentResponse> CreateAsync(CreateStudentRequest request, CancellationToken cancellationToken = default);

        Task<StudentResponse?> UpdateAsync(string id, UpdateStudentRequest request, CancellationToken cancellationToken = default);

        Task<StudentResponse?> UpdateSettingsAsync(string id, UpdateStudentSettingsRequest request, CancellationToken cancellationToken = default);

        Task<StudentApplicationResponse> SubmitApplicationAsync(string studentId, SubmitApplicationRequest request, CancellationToken cancellationToken = default);

        Task<IReadOnlyList<StudentApplicationResponse>> GetApplicationsAsync(string studentId, CancellationToken cancellationToken = default);
    }
}
