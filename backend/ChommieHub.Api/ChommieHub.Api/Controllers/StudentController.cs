using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Exposes student-facing operations: profile management, settings, and accommodation
    /// application submission/retrieval. The route id is always the Firebase Authentication UID,
    /// which is preserved as Student.Id per the Person B identity contract.
    /// </summary>
    [ApiController]
    [Route("api/students")]
    public sealed class StudentController : ControllerBase
    {
        private readonly IStudentService _studentService;
        private readonly ILogger<StudentController> _logger;

        public StudentController(IStudentService studentService, ILogger<StudentController> logger)
        {
            _studentService = studentService;
            _logger = logger;
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<StudentResponse>> GetById(string id, CancellationToken cancellationToken)
        {
            StudentResponse? student = await _studentService.GetByIdAsync(id, cancellationToken);

            if (student is null)
            {
                return NotFound(new { message = "Student profile was not found." });
            }

            return Ok(student);
        }

        [HttpPost]
        public async Task<ActionResult<StudentResponse>> Create(CreateStudentRequest request, CancellationToken cancellationToken)
        {
            StudentResponse student = await _studentService.CreateAsync(request, cancellationToken);

            _logger.LogInformation("Student profile {StudentId} was created.", student.Id);

            return CreatedAtAction(nameof(GetById), new { id = student.Id }, student);
        }

        [HttpPut("{id}")]
        public async Task<ActionResult<StudentResponse>> Update(string id, UpdateStudentRequest request, CancellationToken cancellationToken)
        {
            StudentResponse? student = await _studentService.UpdateAsync(id, request, cancellationToken);

            if (student is null)
            {
                return NotFound(new { message = "Student profile was not found." });
            }

            return Ok(student);
        }

        [HttpPut("{id}/settings")]
        public async Task<ActionResult<StudentResponse>> UpdateSettings(string id, UpdateStudentSettingsRequest request, CancellationToken cancellationToken)
        {
            StudentResponse? student = await _studentService.UpdateSettingsAsync(id, request, cancellationToken);

            if (student is null)
            {
                return NotFound(new { message = "Student profile was not found." });
            }

            _logger.LogInformation("Settings for student {StudentId} were updated.", id);

            return Ok(student);
        }

        /// <summary>
        /// Submits a new accommodation application. This enters the same Firestore
        /// "applications" collection already reviewed by the manager workflow.
        /// </summary>
        [HttpPost("{id}/applications")]
        public async Task<ActionResult<StudentApplicationResponse>> SubmitApplication(string id, SubmitApplicationRequest request, CancellationToken cancellationToken)
        {
            StudentApplicationResponse application = await _studentService.SubmitApplicationAsync(id, request, cancellationToken);

            _logger.LogInformation("Student {StudentId} submitted application {ApplicationId}.", id, application.Id);

            return Created($"/api/students/{id}/applications/{application.Id}", application);
        }

        /// <summary>
        /// Returns only the applications belonging to this student's Firebase UID, reflecting
        /// the status set by manager review/approval/decline/room-assignment.
        /// </summary>
        [HttpGet("{id}/applications")]
        public async Task<ActionResult<IReadOnlyList<StudentApplicationResponse>>> GetApplications(string id, CancellationToken cancellationToken)
        {
            IReadOnlyList<StudentApplicationResponse> applications = await _studentService.GetApplicationsAsync(id, cancellationToken);

            return Ok(applications);
        }
    }
}
