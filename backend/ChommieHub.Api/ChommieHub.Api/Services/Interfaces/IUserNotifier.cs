namespace ChommieHub.Api.Services.Interfaces
{
    // Sends a push notification to a user. Delivery problems never throw,
    // so a failed notification cannot undo the action that triggered it.
    public interface IUserNotifier
    {
        Task NotifyAsync(string userId, string title, string body);
    }
}
