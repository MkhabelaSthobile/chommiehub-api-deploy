namespace ChommieHub.Api.Services.Interfaces
{
    public enum AdminAccess
    {
        Granted,
        InvalidToken,
        NotAdmin
    }

    /// <summary>
    /// The outcome of checking an ID token for administrator access.
    /// </summary>
    public sealed record AdminVerification(AdminAccess Access, string? AdminId);

    public interface IAdminAccessVerifier
    {
        Task<AdminVerification> VerifyAsync(string? idToken);
    }
}