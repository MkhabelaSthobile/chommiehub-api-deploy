
using ChommieHub.Api.Repositories;
using FirebaseAdmin;
using FirebaseAdmin.Messaging;

namespace ChommieHub.Api.Services;

/// <summary>
/// Sends Firebase Cloud Messaging notifications to
/// devices registered for a specific user.
/// </summary>
public sealed class FirebaseNotificationService
{
    private readonly FirebaseApp _firebaseApp;
    private readonly FirestoreFcmDeviceRepository _deviceRepository;
    private readonly ILogger<FirebaseNotificationService> _logger;

    public FirebaseNotificationService(
        FirebaseApp firebaseApp,
        FirestoreFcmDeviceRepository deviceRepository,
        ILogger<FirebaseNotificationService> logger)
    {
        _firebaseApp = firebaseApp;
        _deviceRepository = deviceRepository;
        _logger = logger;
    }

    /// <summary>
    /// Sends a notification to all registered devices
    /// belonging to the specified Firebase user.
    ///
    /// This method must only be called by trusted
    /// backend application code.
    /// </summary>
    public async Task<int> SendToUserAsync(
        string userId,
        string title,
        string body)
    {
        if (string.IsNullOrWhiteSpace(userId))
        {
            throw new ArgumentException(
                "A Firebase user ID is required.",
                nameof(userId));
        }

        if (string.IsNullOrWhiteSpace(title))
        {
            throw new ArgumentException(
                "A notification title is required.",
                nameof(title));
        }

        if (string.IsNullOrWhiteSpace(body))
        {
            throw new ArgumentException(
                "A notification body is required.",
                nameof(body));
        }

        // Retrieve this user's registered devices.
        var devices =
            await _deviceRepository.GetDevicesForUserAsync(userId);

        if (devices.Count == 0)
        {
            _logger.LogInformation(
                "No registered FCM devices found for notification.");
            return 0;
        }

        FirebaseMessaging messaging =
            FirebaseMessaging.GetMessaging(_firebaseApp);

        int successCount = 0;

        foreach (var device in devices)
        {
            if (string.IsNullOrWhiteSpace(device.FcmToken))
            {
                continue;
            }

            try
            {
                var message = new Message
                {
                    Token = device.FcmToken,
                    Notification = new Notification
                    {
                        Title = title,
                        Body = body
                    },
                    Android = new AndroidConfig
                    {
                        Priority = Priority.High
                    }
                };

                await messaging.SendAsync(message);

                successCount++;
            }
            catch (FirebaseMessagingException exception)
            {
                // Never log raw FCM tokens.
                _logger.LogWarning(
                    "FCM delivery failed. Error code: {ErrorCode}",
                    exception.MessagingErrorCode);
            }
        }

        _logger.LogInformation(
            "FCM notification sent to {SuccessfulDevices} of {TotalDevices} devices.",
            successCount,
            devices.Count);

        return successCount;
    }
}
