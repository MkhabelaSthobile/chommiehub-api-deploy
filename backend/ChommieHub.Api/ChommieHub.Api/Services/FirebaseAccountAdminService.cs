using ChommieHub.Api.Services.Interfaces;
using FirebaseAdmin;
using FirebaseAdmin.Auth;

namespace ChommieHub.Api.Services
{
    public sealed class FirebaseAccountAdminService : IFirebaseAccountAdmin
    {
        private readonly FirebaseApp _firebaseApp;

        public FirebaseAccountAdminService(FirebaseApp firebaseApp)
        {
            _firebaseApp = firebaseApp;
        }

        public async Task SetDisabledAsync(
            string uid,
            bool disabled,
            CancellationToken cancellationToken = default)
        {
            FirebaseAuth firebaseAuth = FirebaseAuth.GetAuth(_firebaseApp);

            try
            {
                await firebaseAuth.UpdateUserAsync(
                    new UserRecordArgs { Uid = uid, Disabled = disabled },
                    cancellationToken);

                if (disabled)
                {
                    // End the user's current sessions straight away.
                    await firebaseAuth.RevokeRefreshTokensAsync(uid, cancellationToken);
                }
            }
            catch (FirebaseAuthException exception)
                when (exception.AuthErrorCode == AuthErrorCode.UserNotFound)
            {
                throw new KeyNotFoundException("The Firebase account was not found.");
            }
        }

        public async Task DeleteAsync(
            string uid,
            CancellationToken cancellationToken = default)
        {
            FirebaseAuth firebaseAuth = FirebaseAuth.GetAuth(_firebaseApp);

            try
            {
                await firebaseAuth.DeleteUserAsync(uid, cancellationToken);
            }
            catch (FirebaseAuthException exception)
                when (exception.AuthErrorCode == AuthErrorCode.UserNotFound)
            {
                // Already gone, so there is nothing left to delete.
            }
        }
    }
}