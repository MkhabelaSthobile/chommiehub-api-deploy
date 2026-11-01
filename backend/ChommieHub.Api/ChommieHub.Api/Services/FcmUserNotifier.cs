using ChommieHub.Api.Services.Interfaces;

namespace ChommieHub.Api.Services
{
    // Sends notifications through Firebase Cloud Messaging.
    public sealed class FcmUserNotifier : IUserNotifier
    {
        private readonly FirebaseNotificationService _notificationService;
        private readonly ILogger<FcmUserNotifier> _logger;

        public FcmUserNotifier(
            FirebaseNotificationService notificationService,
            ILogger<FcmUserNotifier> logger)
        {
            _notificationService = notificationService;
            _logger = logger;
        }

        public async Task NotifyAsync(string userId, string title, string body)
        {
            try
            {
                await _notificationService.SendToUserAsync(userId, title, body);
            }
            catch (Exception exception)
            {
                // A notification failure must not undo the administrator's action.
                _logger.LogWarning(exception, "A notification could not be sent.");
            }
        }
    }
}