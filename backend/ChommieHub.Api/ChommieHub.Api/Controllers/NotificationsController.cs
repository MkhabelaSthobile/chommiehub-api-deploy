
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Controllers;


/// Manages FCM device registrations for authenticated users.
[ApiController]
[Route("api/notifications")]
public sealed class NotificationsController : ControllerBase
{
    private readonly FirebaseTokenVerificationService _tokenVerifier;
    private readonly FirestoreFcmDeviceRepository _deviceRepository;
    private readonly ILogger<NotificationsController> _logger;

    public NotificationsController(
        FirebaseTokenVerificationService tokenVerifier,
        FirestoreFcmDeviceRepository deviceRepository,
        ILogger<NotificationsController> logger)
    {
        _tokenVerifier = tokenVerifier;
        _deviceRepository = deviceRepository;
        _logger = logger;
    }

   
    /// Registers an FCM token for the authenticated user.
    [HttpPost("devices")]
    public async Task<IActionResult> RegisterDevice(
        [FromBody] DeviceTokenRequest request)
    {
        string? userId = await VerifyCurrentUserAsync();

        if (userId == null)
        {
            return Unauthorized(new
            {
                message = "A valid Firebase ID token is required."
            });
        }

        if (string.IsNullOrWhiteSpace(request.FcmToken) ||
            request.FcmToken.Length > 4096)
        {
            return BadRequest(new
            {
                message = "A valid FCM token is required."
            });
        }

        if (!string.Equals(
                request.Platform,
                "android",
                StringComparison.OrdinalIgnoreCase))
        {
            return BadRequest(new
            {
                message = "Only Android devices are supported."
            });
        }

        await _deviceRepository.RegisterDeviceAsync(
            userId,
            request.FcmToken);

        _logger.LogInformation(
            "FCM device registered for authenticated user.");

        return Ok(new
        {
            message = "Device registered successfully."
        });
    }


    /// Removes an FCM token belonging to the authenticated user.
    [HttpDelete("devices")]
    public async Task<IActionResult> RemoveDevice(
        [FromBody] DeviceTokenRequest request)
    {
        string? userId = await VerifyCurrentUserAsync();

        if (userId == null)
        {
            return Unauthorized(new
            {
                message = "A valid Firebase ID token is required."
            });
        }

        if (string.IsNullOrWhiteSpace(request.FcmToken))
        {
            return BadRequest(new
            {
                message = "An FCM token is required."
            });
        }

        bool removed = await _deviceRepository.RemoveDeviceAsync(
            userId,
            request.FcmToken);

        if (!removed)
        {
            return NotFound(new
            {
                message = "No matching device registration was found."
            });
        }

        _logger.LogInformation(
            "FCM device registration removed.");

        return NoContent();
    }

    
    /// Extracts and verifies the Firebase ID token from the Authorization: Bearer header.
    private async Task<string?> VerifyCurrentUserAsync()
    {
        string authorization =
            Request.Headers.Authorization.ToString();

        const string bearerPrefix = "Bearer ";

        if (!authorization.StartsWith(
                bearerPrefix,
                StringComparison.OrdinalIgnoreCase))
        {
            return null;
        }

        string idToken = authorization[bearerPrefix.Length..].Trim();

        if (string.IsNullOrWhiteSpace(idToken))
        {
            return null;
        }

        return await _tokenVerifier.VerifyUserIdAsync(idToken);
    }


    /// Request body for FCM device registration and removal.

    public sealed class DeviceTokenRequest
    {
        public string FcmToken { get; set; } = string.Empty;

        public string Platform { get; set; } = "android";
    }
}
