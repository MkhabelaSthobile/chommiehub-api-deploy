using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using FirebaseAdmin;
using FirebaseAdmin.Auth;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Manager profiles are provisioned by administrators, not by public registration.
    /// A manager may read only their own profile; administrators may read any profile.
    /// </summary>
    [ApiController]
    [Route("api/managers")]
    public sealed class ManagersController : ControllerBase
    {
        private readonly IManagerAccountService _managerAccountService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;
        private readonly FirebaseApp _firebaseApp;
        private readonly ILogger<ManagersController> _logger;

        public ManagersController(
            IManagerAccountService managerAccountService,
            FirebaseTokenVerificationService tokenVerifier,
            FirebaseApp firebaseApp,
            ILogger<ManagersController> logger)
        {
            _managerAccountService = managerAccountService;
            _tokenVerifier = tokenVerifier;
            _firebaseApp = firebaseApp;
            _logger = logger;
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<ManagerResponse>> GetById(
            string id,
            CancellationToken cancellationToken)
        {
            var (userId, token, error) = await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            // Never expose another manager's personal information to a student
            // or to a manager with a different Firebase UID.
            if (!string.Equals(userId, id, StringComparison.Ordinal)
                && !await _tokenVerifier.VerifyAdminAsync(token))
            {
                return StatusCode(StatusCodes.Status403Forbidden, new
                {
                    message = "You may only read your own manager profile."
                });
            }

            ManagerResponse? manager = await _managerAccountService.GetByIdAsync(
                id, cancellationToken);

            return manager is null
                ? NotFound(new { message = "Manager profile was not found." })
                : Ok(manager);
        }

        [HttpGet("by-firebase-uid/{firebaseUid}")]
        public Task<ActionResult<ManagerResponse>> GetByFirebaseUid(
            string firebaseUid,
            CancellationToken cancellationToken)
            => GetById(firebaseUid, cancellationToken);

        /// <summary>
        /// An administrator appoints a manager who already has a Firebase account.
        /// The UID is supplied by the administrator, never by a public sign-up flow.
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<ManagerResponse>> Create(
            [FromBody] CreateManagerRequest request,
            CancellationToken cancellationToken)
        {
            var (_, token, error) = await GetAuthenticatedUserAsync();

            if (error is not null)
            {
                return error;
            }

            if (!await _tokenVerifier.VerifyAdminAsync(token))
            {
                return StatusCode(StatusCodes.Status403Forbidden, new
                {
                    message = "Administrator authorization is required."
                });
            }

            try
            {
                // An administrator must appoint an EXISTING Firebase user.
                // This prevents orphaned Firestore manager profiles.
                UserRecord account = await FirebaseAuth.GetAuth(_firebaseApp).GetUserAsync(
                    request.FirebaseUid.Trim(), cancellationToken);

                if (account.Disabled)
                {
                    return Conflict(new { message = "Enable the Firebase account before appointing a manager." });
                }

                if (!string.Equals(account.Email, request.Email, StringComparison.OrdinalIgnoreCase))
                {
                    return BadRequest(new { message = "The manager email must match the Firebase account." });
                }

                if (account.CustomClaims is not null
                    && account.CustomClaims.TryGetValue("role", out object? role)
                    && string.Equals(role as string, "admin", StringComparison.OrdinalIgnoreCase))
                {
                    return Conflict(new { message = "An administrator cannot be appointed as a manager." });
                }

                ManagerResponse manager = await _managerAccountService.CreateAsync(
                    request, cancellationToken);

                _logger.LogInformation("Manager profile {ManagerId} was appointed.", manager.Id);

                return CreatedAtAction(nameof(GetById), new { id = manager.Id }, manager);
            }
            catch (FirebaseAuthException exception) when (exception.AuthErrorCode == AuthErrorCode.UserNotFound)
            {
                return NotFound(new { message = "The Firebase account does not exist." });
            }
            catch (ArgumentException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
            catch (InvalidOperationException exception)
            {
                return Conflict(new { message = exception.Message });
            }
        }

        // The old POST /api/managers/register endpoint was intentionally removed.
        // Publicly choosing the Manager role must not create privileged access.

        private async Task<(string? UserId, string? Token, ActionResult? Error)>
            GetAuthenticatedUserAsync()
        {
            string authorization = Request.Headers.Authorization.ToString();

            if (!authorization.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
            {
                return (null, null, Unauthorized(new
                {
                    message = "A valid Firebase ID token is required."
                }));
            }

            string token = authorization["Bearer ".Length..].Trim();
            string? uid = await _tokenVerifier.VerifyUserIdAsync(token);

            return string.IsNullOrWhiteSpace(uid)
                ? (null, null, Unauthorized(new { message = "Invalid or revoked Firebase ID token." }))
                : (uid, token, null);
        }
    }
}
