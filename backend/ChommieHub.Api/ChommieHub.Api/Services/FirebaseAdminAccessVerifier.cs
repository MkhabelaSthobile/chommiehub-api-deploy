using ChommieHub.Api.Services.Interfaces;
using FirebaseAdmin;
using FirebaseAdmin.Auth;

namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Checks a Firebase ID token and whether it carries the administrator claim.
    /// </summary>
    public sealed class FirebaseAdminAccessVerifier : IAdminAccessVerifier
    {
        private readonly FirebaseApp _firebaseApp;
        private readonly ILogger<FirebaseAdminAccessVerifier> _logger;

        public FirebaseAdminAccessVerifier(
            FirebaseApp firebaseApp,
            ILogger<FirebaseAdminAccessVerifier> logger)
        {
            _firebaseApp = firebaseApp;
            _logger = logger;
        }

        public async Task<AdminVerification> VerifyAsync(string? idToken)
        {
            if (string.IsNullOrWhiteSpace(idToken))
            {
                return new AdminVerification(AdminAccess.InvalidToken, null);
            }

            try
            {
                FirebaseAuth firebaseAuth = FirebaseAuth.GetAuth(_firebaseApp);

                FirebaseToken decodedToken = await firebaseAuth.VerifyIdTokenAsync(
                    idToken,
                    checkRevoked: true);

                if (string.IsNullOrWhiteSpace(decodedToken.Uid))
                {
                    return new AdminVerification(AdminAccess.InvalidToken, null);
                }

                bool isAdmin =
                    decodedToken.Claims.TryGetValue("role", out object? role)
                    && string.Equals(role as string, "admin", StringComparison.Ordinal);

                return isAdmin
                    ? new AdminVerification(AdminAccess.Granted, decodedToken.Uid)
                    : new AdminVerification(AdminAccess.NotAdmin, decodedToken.Uid);
            }
            catch (FirebaseAuthException exception)
            {
                _logger.LogWarning(
                    "Administrator token verification failed: {ErrorCode}",
                    exception.AuthErrorCode);

                return new AdminVerification(AdminAccess.InvalidToken, null);
            }
        }
    }
}