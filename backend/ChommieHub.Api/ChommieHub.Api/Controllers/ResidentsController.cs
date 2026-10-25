
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Exposes resident information required by resident-facing
    /// Android features.
    /// </summary>
    [ApiController]
    [Route("api/residents")]
    public sealed class ResidentsController : ControllerBase
    {
        private readonly IManagerService _managerService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;

        public ResidentsController(
            IManagerService managerService,
            FirebaseTokenVerificationService tokenVerifier)
        {
            _managerService = managerService;
            _tokenVerifier = tokenVerifier;
        }

        /// <summary>
        /// Retrieves the active resident record belonging to the
        /// authenticated student.
        /// </summary>
        [HttpGet("student/{studentId}")]
        public async Task<ActionResult<ResidentResponse>> GetByStudentId(
            string studentId,
            CancellationToken cancellationToken)
        {
            if (string.IsNullOrWhiteSpace(studentId))
            {
                return BadRequest(new
                {
                    message = "Student ID is required."
                });
            }

            // Require a Firebase ID token.
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

            // Verify the token and obtain the authenticated user's UID.
            string? firebaseUid =
                await _tokenVerifier.VerifyUserIdAsync(idToken);

            if (string.IsNullOrWhiteSpace(firebaseUid))
            {
                return Unauthorized(new
                {
                    message = "Invalid or revoked Firebase ID token."
                });
            }

            // A student may only retrieve their own resident record.
            if (!string.Equals(
                firebaseUid,
                studentId,
                StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "You can only access your own resident information."
                });
            }

            ResidentResponse? resident =
                await _managerService.GetResidentByStudentIdAsync(
                    studentId,
                    cancellationToken);

            if (resident is null)
            {
                return NotFound(new
                {
                    message = "An active resident record was not found for this student."
                });
            }

            return Ok(resident);
        }
    }
}
