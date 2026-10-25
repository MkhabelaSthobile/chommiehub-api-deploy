
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Exposes authenticated student-facing operations:
    /// profile management, settings, and accommodation applications.
    /// Student IDs must match the authenticated Firebase UID.
    /// </summary>
    [ApiController]
    [Route("api/students")]
    public sealed class StudentController : ControllerBase
    {
        private readonly IStudentService _studentService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;
        private readonly ILogger<StudentController> _logger;

        public StudentController(
            IStudentService studentService,
            FirebaseTokenVerificationService tokenVerifier,
            ILogger<StudentController> logger)
        {
            _studentService = studentService;
            _tokenVerifier = tokenVerifier;
            _logger = logger;
        }

        /// <summary>
        /// Verifies the Firebase ID token and ensures the requested
        /// student ID matches the authenticated Firebase UID.
        /// </summary>
        private async Task<ActionResult?> AuthorizeStudentAsync(
            string studentId)
        {
            string authorization =
                Request.Headers.Authorization.ToString();

            if (string.IsNullOrWhiteSpace(authorization) ||
                !authorization.StartsWith(
                    "Bearer ",
                    StringComparison.OrdinalIgnoreCase))
            {
                return Unauthorized(new
                {
                    message = "A Firebase ID token is required."
                });
            }

            string idToken =
                authorization["Bearer ".Length..].Trim();

            if (string.IsNullOrWhiteSpace(idToken))
            {
                return Unauthorized(new
                {
                    message = "A Firebase ID token is required."
                });
            }

            string? firebaseUid =
                await _tokenVerifier.VerifyUserIdAsync(idToken);

            if (string.IsNullOrWhiteSpace(firebaseUid))
            {
                return Unauthorized(new
                {
                    message = "Invalid or revoked Firebase ID token."
                });
            }

            if (string.IsNullOrWhiteSpace(studentId))
            {
                return BadRequest(new
                {
                    message = "Student ID is required."
                });
            }

            if (!string.Equals(
                firebaseUid,
                studentId,
                StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "You can only access your own student information."
                });
            }

            return null;
        }

        /// <summary>
        /// Retrieves the authenticated student's profile.
        /// </summary>
        [HttpGet("{id}")]
        public async Task<ActionResult<StudentResponse>> GetById(
            string id,
            CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await AuthorizeStudentAsync(id);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            StudentResponse? student =
                await _studentService.GetByIdAsync(
                    id,
                    cancellationToken);

            if (student is null)
            {
                return NotFound(new
                {
                    message = "Student profile was not found."
                });
            }

            return Ok(student);
        }

        /// <summary>
        /// Retrieves the authenticated student's profile by Firebase UID.
        /// Preserved for the Android login flow.
        /// </summary>
        [HttpGet("by-firebase-uid/{firebaseUid}")]
        public Task<ActionResult<StudentResponse>> GetByFirebaseUid(
            string firebaseUid,
            CancellationToken cancellationToken)
        {
            return GetById(firebaseUid, cancellationToken);
        }

        /// <summary>
        /// Creates a student profile for the authenticated Firebase user.
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<StudentResponse>> Create(
            CreateStudentRequest request,
            CancellationToken cancellationToken)
        {
            // A new profile has no route ID, so first verify
            // the caller's Firebase identity.
            string authorization =
                Request.Headers.Authorization.ToString();

            if (string.IsNullOrWhiteSpace(authorization) ||
                !authorization.StartsWith(
                    "Bearer ",
                    StringComparison.OrdinalIgnoreCase))
            {
                return Unauthorized(new
                {
                    message = "A Firebase ID token is required."
                });
            }

            string idToken =
                authorization["Bearer ".Length..].Trim();

            if (string.IsNullOrWhiteSpace(idToken))
            {
                return Unauthorized(new
                {
                    message = "A Firebase ID token is required."
                });
            }

            string? firebaseUid =
                await _tokenVerifier.VerifyUserIdAsync(idToken);

            if (string.IsNullOrWhiteSpace(firebaseUid))
            {
                return Unauthorized(new
                {
                    message = "Invalid or revoked Firebase ID token."
                });
            }

            // Important: The service must create the profile
            // using the authenticated Firebase UID, not an
            // arbitrary identity supplied in the request body.
            //
            // We will verify that contract in IStudentService
            // and StudentService before treating profile creation
            // as fully secured.

            // Never trust a Firebase UID supplied by the client.
            // The verified Firebase token is the source of identity.
            if (!string.Equals(
                request.FirebaseUid,
                firebaseUid,
                StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "The student Firebase UID must match the authenticated user."
                });
            }

            StudentResponse student =
                await _studentService.CreateAsync(
                    request,
                    cancellationToken);

            if (!string.Equals(
                student.Id,
                firebaseUid,
                StringComparison.Ordinal))
            {
                _logger.LogError(
                    "Student creation identity mismatch. Authenticated UID {FirebaseUid}, created student ID {StudentId}.",
                    firebaseUid,
                    student.Id);

                return StatusCode(500, new
                {
                    message = "Student profile identity does not match the authenticated user."
                });
            }

            _logger.LogInformation(
                "Student profile {StudentId} was created.",
                student.Id);

            return CreatedAtAction(
                nameof(GetById),
                new { id = student.Id },
                student);
        }

        /// <summary>
        /// Updates the authenticated student's profile.
        /// </summary>
        [HttpPut("{id}")]
        public async Task<ActionResult<StudentResponse>> Update(
            string id,
            UpdateStudentRequest request,
            CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await AuthorizeStudentAsync(id);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            StudentResponse? student =
                await _studentService.UpdateAsync(
                    id,
                    request,
                    cancellationToken);

            if (student is null)
            {
                return NotFound(new
                {
                    message = "Student profile was not found."
                });
            }

            return Ok(student);
        }

        /// <summary>
        /// Updates the authenticated student's settings.
        /// </summary>
        [HttpPut("{id}/settings")]
        public async Task<ActionResult<StudentResponse>> UpdateSettings(
            string id,
            UpdateStudentSettingsRequest request,
            CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await AuthorizeStudentAsync(id);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            StudentResponse? student =
                await _studentService.UpdateSettingsAsync(
                    id,
                    request,
                    cancellationToken);

            if (student is null)
            {
                return NotFound(new
                {
                    message = "Student profile was not found."
                });
            }

            return Ok(student);
        }

        /// <summary>
        /// Submits an accommodation application for the
        /// authenticated student only.
        /// </summary>
        [HttpPost("{id}/applications")]
        public async Task<ActionResult<StudentApplicationResponse>>
            SubmitApplication(
                string id,
                SubmitApplicationRequest request,
                CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await AuthorizeStudentAsync(id);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            try
            {
                StudentApplicationResponse application =
                    await _studentService.SubmitApplicationAsync(
                        id,
                        request,
                        cancellationToken);

                _logger.LogInformation(
                    "Student {StudentId} submitted application {ApplicationId} for room {RoomId}.",
                    id,
                    application.Id,
                    application.RequestedRoomId);

                return Created(
                    $"/api/students/{id}/applications/{application.Id}",
                    application);
            }
            catch (KeyNotFoundException exception)
            {
                return NotFound(new
                {
                    message = exception.Message
                });
            }
            catch (InvalidOperationException exception)
            {
                return Conflict(new
                {
                    message = exception.Message
                });
            }
        }

        /// <summary>
        /// Retrieves accommodation applications belonging
        /// to the authenticated student only.
        /// </summary>
        [HttpGet("{id}/applications")]
        public async Task<ActionResult<IReadOnlyList<StudentApplicationResponse>>>
            GetApplications(
                string id,
                CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await AuthorizeStudentAsync(id);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            IReadOnlyList<StudentApplicationResponse> applications =
                await _studentService.GetApplicationsAsync(
                    id,
                    cancellationToken);

            return Ok(applications);
        }
    }
}
