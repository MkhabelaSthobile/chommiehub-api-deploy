
using FirebaseAdmin;
using FirebaseAdmin.Auth;

namespace ChommieHub.Api.Services;


public sealed class FirebaseTokenVerificationService
{
    private readonly FirebaseApp _firebaseApp;
    private readonly ILogger<FirebaseTokenVerificationService> _logger;

    public FirebaseTokenVerificationService(
        FirebaseApp firebaseApp,
        ILogger<FirebaseTokenVerificationService> logger)
    {
        _firebaseApp = firebaseApp;
        _logger = logger;
    }


    /// Verifies a Firebase ID token and returns its authenticated UID and returns null if the token is invalid or revoked.

    public async Task<string?> VerifyUserIdAsync(string? idToken)
    {
        if (string.IsNullOrWhiteSpace(idToken))
        {
            return null;
        }

        try
        {
            FirebaseAuth firebaseAuth =
                FirebaseAuth.GetAuth(_firebaseApp);

            FirebaseToken decodedToken =
                await firebaseAuth.VerifyIdTokenAsync(
                    idToken,
                    checkRevoked: true
                );

            if (string.IsNullOrWhiteSpace(decodedToken.Uid))
            {
                return null;
            }

            return decodedToken.Uid;
        }
        catch (FirebaseAuthException exception)
        {
            _logger.LogWarning(
                "Firebase ID token verification failed: {ErrorCode}",
                exception.AuthErrorCode
            );

            return null;
        }
    }

        /// <summary>
        /// Verifies that the Firebase ID token belongs to an administrator.
        /// The role claim must be assigned through a trusted server-side process.
        /// </summary>
public async Task<bool> VerifyAdminAsync(string? idToken)
    {
        if (string.IsNullOrWhiteSpace(idToken))
        {
            return false;
        }

        try
        {
            FirebaseAuth firebaseAuth =
                FirebaseAuth.GetAuth(_firebaseApp);

            FirebaseToken decodedToken =
                await firebaseAuth.VerifyIdTokenAsync(
                    idToken,
                    checkRevoked: true);

            return !string.IsNullOrWhiteSpace(decodedToken.Uid)
                && decodedToken.Claims.TryGetValue("role", out object? role)
                && string.Equals(
                    role as string,
                    "admin",
                    StringComparison.Ordinal);
        }
        catch (FirebaseAuthException exception)
        {
            _logger.LogWarning(
                "Firebase administrator token verification failed: {ErrorCode}",
                exception.AuthErrorCode);

            return false;
        }
    }
}

