using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    // Administrator-only endpoints: dashboard, accounts, listing review and audit log.
    // Every request is checked for a valid administrator token first.
    
    [ApiController]
    [Route("api/admin")]
    public sealed class AdminController : ControllerBase
    {
        private readonly IAdminAccessVerifier _accessVerifier;
        private readonly IAdminQueryService _queryService;
        private readonly IAdminActionService _actionService;

        public AdminController(
            IAdminAccessVerifier accessVerifier,
            IAdminQueryService queryService,
            IAdminActionService actionService)
        {
            _accessVerifier = accessVerifier;
            _queryService = queryService;
            _actionService = actionService;
        }

        // ---- Dashboard ----

        [HttpGet("dashboard")]
        public Task<ActionResult> GetDashboard(CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _queryService.GetDashboardAsync(cancellationToken)));
        }

        // ---- Accounts ----

        [HttpGet("users")]
        public Task<ActionResult> GetUsers(
            [FromQuery] string? role,
            [FromQuery] string? search,
            CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _queryService.GetUsersAsync(role, search, cancellationToken)));
        }

        [HttpGet("users/{id}")]
        public Task<ActionResult> GetUser(string id, CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
            {
                AdminUserDetailResponse? user = await _queryService.GetUserAsync(id, cancellationToken);

                if (user is null)
                {
                    return NotFound(new { message = "The account was not found." });
                }

                return Ok(user);
            });
        }

        [HttpPost("users/{id}/disable")]
        public Task<ActionResult> DisableUser(
            string id,
            [FromBody] AdminReasonRequest? request,
            CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _actionService.DisableAccountAsync(adminId, id, request?.Reason, cancellationToken)));
        }

        [HttpPost("users/{id}/enable")]
        public Task<ActionResult> EnableUser(string id, CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _actionService.EnableAccountAsync(adminId, id, cancellationToken)));
        }

        [HttpPost("users/{id}/delete")]
        public Task<ActionResult> DeleteUser(
            string id,
            [FromBody] AdminReasonRequest? request,
            CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _actionService.DeleteAccountAsync(adminId, id, request?.Reason, cancellationToken)));
        }

        // ---- Accommodation listings ----

        [HttpGet("listings")]
        public Task<ActionResult> GetListings(
            [FromQuery] string? status,
            CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _queryService.GetListingsAsync(status, cancellationToken)));
        }

        [HttpGet("listings/{id}")]
        public Task<ActionResult> GetListing(string id, CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
            {
                AdminListingDetailResponse? listing =
                    await _queryService.GetListingAsync(id, cancellationToken);

                if (listing is null)
                {
                    return NotFound(new { message = "The accommodation was not found." });
                }

                return Ok(listing);
            });
        }

        [HttpPost("listings/{id}/approve")]
        public Task<ActionResult> ApproveListing(string id, CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _actionService.ApproveListingAsync(adminId, id, cancellationToken)));
        }

        [HttpPost("listings/{id}/reject")]
        public Task<ActionResult> RejectListing(
            string id,
            [FromBody] AdminReasonRequest? request,
            CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _actionService.RejectListingAsync(adminId, id, request?.Reason, cancellationToken)));
        }

        [HttpPost("listings/{id}/archive")]
        public Task<ActionResult> ArchiveListing(
            string id,
            [FromBody] AdminReasonRequest? request,
            CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _actionService.ArchiveListingAsync(adminId, id, request?.Reason, cancellationToken)));
        }

        [HttpPost("listings/{id}/unarchive")]
        public Task<ActionResult> UnarchiveListing(string id, CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _actionService.UnarchiveListingAsync(adminId, id, cancellationToken)));
        }

        [HttpPost("listings/{id}/delete")]
        public Task<ActionResult> DeleteListing(
            string id,
            [FromBody] AdminReasonRequest? request,
            CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _actionService.DeleteListingAsync(adminId, id, request?.Reason, cancellationToken)));
        }

        // ---- Audit log ----

        [HttpGet("audit")]
        public Task<ActionResult> GetAudit([FromQuery] int limit, CancellationToken cancellationToken)
        {
            return ExecuteAsync(async adminId =>
                Ok(await _queryService.GetAuditAsync(limit, cancellationToken)));
        }

        /// <summary>
        /// Checks the administrator token, runs the action and turns the
        /// service's exceptions into the right HTTP status codes.
        /// </summary>
        private async Task<ActionResult> ExecuteAsync(Func<string, Task<ActionResult>> action)
        {
            AdminVerification access = await _accessVerifier.VerifyAsync(GetBearerToken());

            if (access.Access == AdminAccess.InvalidToken)
            {
                return Unauthorized(new { message = "A valid Firebase ID token is required." });
            }

            if (access.Access == AdminAccess.NotAdmin || access.AdminId is null)
            {
                return StatusCode(
                    StatusCodes.Status403Forbidden,
                    new { message = "Administrator access is required." });
            }

            try
            {
                return await action(access.AdminId);
            }
            catch (KeyNotFoundException exception)
            {
                return NotFound(new { message = exception.Message });
            }
            catch (InvalidOperationException exception)
            {
                return Conflict(new { message = exception.Message });
            }
            catch (ArgumentException exception)
            {
                return BadRequest(new { message = exception.Message });
            }
        }

        private string? GetBearerToken()
        {
            string authorization = Request.Headers.Authorization.ToString();

            if (string.IsNullOrWhiteSpace(authorization)
                || !authorization.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
            {
                return null;
            }

            return authorization["Bearer ".Length..].Trim();
        }
    }
}