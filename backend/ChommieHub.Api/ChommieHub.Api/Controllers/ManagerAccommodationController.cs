using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers
{
    // Lets a signed-in residence manager save and submit their own accommodation.
    [ApiController]
    [Route("api/manager/my-accommodation")]
    public sealed class ManagerAccommodationController : ControllerBase
    {
        private readonly IManagerListingService _listingService;
        private readonly FirebaseTokenVerificationService _tokenVerifier;

        public ManagerAccommodationController(
            IManagerListingService listingService,
            FirebaseTokenVerificationService tokenVerifier)
        {
            _listingService = listingService;
            _tokenVerifier = tokenVerifier;
        }

        [HttpGet]
        public Task<ActionResult> Get(CancellationToken cancellationToken)
        {
            return ExecuteAsync(async managerId =>
            {
                ManagerAccommodationResponse? result =
                    await _listingService.GetMyAccommodationAsync(managerId, cancellationToken);

                if (result is null)
                {
                    return NotFound(new { message = "You have not saved an accommodation yet." });
                }

                return Ok(result);
            });
        }

        [HttpPut]
        public Task<ActionResult> Save(
            [FromBody] CreateAccommodationRequest request,
            CancellationToken cancellationToken)
        {
            return ExecuteAsync(async managerId =>
            {
                ManagerAccommodationResponse result =
                    await _listingService.SaveDraftAsync(managerId, request, cancellationToken);

                return Ok(result);
            });
        }

        [HttpPost("submit")]
        public Task<ActionResult> Submit(CancellationToken cancellationToken)
        {
            return ExecuteAsync(async managerId =>
            {
                ManagerAccommodationResponse result =
                    await _listingService.SubmitForReviewAsync(managerId, cancellationToken);

                return Ok(result);
            });
        }

        private async Task<ActionResult> ExecuteAsync(Func<string, Task<ActionResult>> action)
        {
            string? managerId = await GetManagerIdAsync();

            if (managerId is null)
            {
                return Unauthorized(new { message = "A valid Firebase ID token is required." });
            }

            try
            {
                return await action(managerId);
            }
            catch (KeyNotFoundException exception)
            {
                return StatusCode(StatusCodes.Status403Forbidden, new { message = exception.Message });
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

        private async Task<string?> GetManagerIdAsync()
        {
            string authorization = Request.Headers.Authorization.ToString();

            if (string.IsNullOrWhiteSpace(authorization)
                || !authorization.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
            {
                return null;
            }

            string idToken = authorization["Bearer ".Length..].Trim();

            return await _tokenVerifier.VerifyUserIdAsync(idToken);
        }
    }
}