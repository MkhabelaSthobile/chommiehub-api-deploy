/**
 * LOCAL MAINTAINER UTILITY — never expose this operation as an HTTP endpoint.
 *
 * Usage (from repo root, after npm installing firebase-admin locally):
 *   node scripts/grant-admin.cjs <firebase-uid> <expected-email>
 *
 * Requires GOOGLE_APPLICATION_CREDENTIALS to point to an uncommitted Firebase
 * service-account file for the same Firebase project as the ChommieHub API.
 * Review the target account's email in Firebase Console before running.
 */
const { initializeApp, applicationDefault } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');

const uid = process.argv[2]?.trim();
const expectedEmail = process.argv[3]?.trim().toLowerCase();

if (!uid || !expectedEmail || !process.env.GOOGLE_APPLICATION_CREDENTIALS) {
  console.error('Usage: GOOGLE_APPLICATION_CREDENTIALS=<private-file> node scripts/grant-admin.cjs <firebase-uid> <expected-email>');
  process.exit(2);
}

(async () => {
  initializeApp({ credential: applicationDefault() });
  const auth = getAuth();
  const user = await auth.getUser(uid);
  if (!user.email || user.email.toLowerCase() !== expectedEmail || user.disabled) {
    throw new Error('Account is disabled or its email does not match; role was NOT changed.');
  }
  const claims = { ...(user.customClaims || {}), role: 'admin' };
  await auth.setCustomUserClaims(uid, claims);
  // Existing sessions must refresh their ID token before the claim appears.
  await auth.revokeRefreshTokens(uid);
  console.log(`Administrator role granted to verified account ${user.email}. Sign in again to refresh the token.`);
})().catch(error => {
  console.error(`Failed: ${error.message}`);
  process.exitCode = 1;
});
