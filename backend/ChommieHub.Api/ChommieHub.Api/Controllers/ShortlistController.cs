
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides authenticated endpoints for viewing, adding,
    /// and removing a student's shortlisted accommodations.
    /// </summary>
    [ApiController]
    [Route("api/students/{studentId}/shortlist")]
    public sealed class ShortlistController : ControllerBase
    {
        private readonly IShortlistService _shortlistService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;
        private readonly ILogger<ShortlistController> _logger;

        public ShortlistController(
            IShortlistService shortlistService,
            FirebaseTokenVerificationService tokenVerifier,
            ILogger<ShortlistController> logger)
        {
            _shortlistService = shortlistService;
            _tokenVerifier = tokenVerifier;
            _logger = logger;
        }

        /// <summary>
        /// Verifies the Firebase ID token and ensures that
        /// the authenticated user owns the requested shortlist.
        /// </summary>
        private async Task<ActionResult?> AuthorizeStudentAsync(
            string studentId)
        {
            if (string.IsNullOrWhiteSpace(studentId))
            {
                return BadRequest(new
                {
                    message = "Student ID is required."
                });
            }

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

            if (!string.Equals(
                firebaseUid,
                studentId,
                StringComparison.Ordinal))
            {
                return StatusCode(403, new
                {
                    message = "You can only access your own shortlist."
                });
            }

            return null;
        }

        /// <summary>
        /// Retrieves the authenticated student's shortlist.
        /// </summary>
        [HttpGet]
        public async Task<ActionResult<IReadOnlyList<AccommodationResponse>>>
            GetShortlist(
                string studentId,
                CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await AuthorizeStudentAsync(studentId);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            IReadOnlyList<AccommodationResponse> shortlist =
                await _shortlistService.GetShortlistAsync(
                    studentId,
                    cancellationToken);

            return Ok(shortlist);
        }

        /// <summary>
        /// Adds an accommodation to the authenticated
        /// student's shortlist.
        /// </summary>
        [HttpPost("{accommodationId}")]
        public async Task<IActionResult> AddToShortlist(
            string studentId,
            string accommodationId,
            CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await AuthorizeStudentAsync(studentId);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            await _shortlistService.AddAsync(
                studentId,
                accommodationId,
                cancellationToken);

            _logger.LogInformation(
                "Student {StudentId} shortlisted accommodation {AccommodationId}.",
                studentId,
                accommodationId);

            return NoContent();
        }

        /// <summary>
        /// Removes an accommodation from the authenticated
        /// student's shortlist.
        /// </summary>
        [HttpDelete("{accommodationId}")]
        public async Task<IActionResult> RemoveFromShortlist(
            string studentId,
            string accommodationId,
            CancellationToken cancellationToken)
        {
            ActionResult? authorizationError =
                await AuthorizeStudentAsync(studentId);

            if (authorizationError is not null)
            {
                return authorizationError;
            }

            await _shortlistService.RemoveAsync(
                studentId,
                accommodationId,
                cancellationToken);

            _logger.LogInformation(
                "Student {StudentId} removed accommodation {AccommodationId} from shortlist.",
                studentId,
                accommodationId);

            return NoContent();
        }
    }
}
