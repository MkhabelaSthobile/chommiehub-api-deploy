
package com.chommiehub.app.firebase

import android.util.Log
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.remote.FcmDeviceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Retrieves, registers and unregisters FCM device tokens
 * for authenticated ChommieHub users.
 */
object FcmTokenManager {

    private const val TAG = "ChommieHubFCM"

    /**
     * Called after a user successfully signs in.
     */
    fun refreshTokenForCurrentUser() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.d(TAG, "No signed-in user. FCM registration skipped.")
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (FirebaseAuth.getInstance().currentUser?.uid != user.uid) {
                    Log.d(TAG, "User changed before token retrieval completed.")
                    return@addOnSuccessListener
                }

                registerToken(user, token)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to retrieve FCM token", exception)
            }
    }

    /**
     * Called when Firebase generates a new FCM token.
     */
    fun handleRefreshedToken(token: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.d(TAG, "No signed-in user. Token registration deferred.")
            return
        }

        registerToken(user, token)
    }

    /**
     * Sends the FCM token to the authenticated backend.
     */
    private fun registerToken(user: FirebaseUser, fcmToken: String) {
        if (fcmToken.isBlank()) {
            Log.w(TAG, "Empty FCM token. Registration skipped.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Obtain a valid Firebase Authentication ID token.
                val idToken = user.getIdToken(false).await().token

                if (idToken.isNullOrBlank()) {
                    Log.w(TAG, "Firebase ID token unavailable.")
                    return@launch
                }

                // Check whether the same user is still signed in.
                if (FirebaseAuth.getInstance().currentUser?.uid != user.uid) {
                    Log.d(TAG, "User session changed. Registration cancelled.")
                    return@launch
                }

                val response = ApiClient.api.registerFcmDevice(
                    authorization = "Bearer $idToken",
                    request = FcmDeviceRequest(fcmToken = fcmToken)
                )

                if (response.isSuccessful) {
                    Log.d(TAG, "FCM device registered successfully.")
                } else {
                    Log.w(
                        TAG,
                        "FCM registration failed. HTTP ${response.code()}"
                    )
                }

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to register FCM device",
                    exception
                )
            }
        }
    }

    /**
     * Attempts to remove this device's FCM registration
     * before the user signs out.
     *
     * Returns true when removal is confirmed or no
     * signed-in user exists.
     *
     * Returns false when cleanup cannot be confirmed.
     */
    suspend fun unregisterCurrentDevice(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser ?: return true

        return try {
            val result = withTimeoutOrNull(10_000L) {
                // Retrieve this device's current FCM token.
                val fcmToken = FirebaseMessaging.getInstance()
                    .token.await()

                if (fcmToken.isBlank()) {
                    return@withTimeoutOrNull true
                }

                // Retrieve the Firebase Authentication ID token.
                val idToken = user.getIdToken(false)
                    .await()
                    .token

                if (idToken.isNullOrBlank()) {
                    Log.w(TAG, "Firebase ID token unavailable for removal.")
                    return@withTimeoutOrNull false
                }

                // Ensure the same user is still signed in.
                if (FirebaseAuth.getInstance().currentUser?.uid != user.uid) {
                    Log.d(TAG, "User changed. FCM removal cancelled.")
                    return@withTimeoutOrNull false
                }

                // Request removal from the ASP.NET Core backend.
                val response = ApiClient.api.removeFcmDevice(
                    authorization = "Bearer $idToken",
                    request = FcmDeviceRequest(fcmToken = fcmToken)
                )

                when (response.code()) {
                    200, 204, 404 -> {
                        Log.d(TAG, "FCM device removal completed.")
                        true
                    }

                    else -> {
                        Log.w(
                            TAG,
                            "FCM device removal failed. HTTP ${response.code()}"
                        )
                        false
                    }
                }
            }

            result ?: run {
                Log.w(TAG, "FCM device removal timed out.")
                false
            }

        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.w(
                TAG,
                "FCM device removal could not be completed."
            )
            false
        }
    }
}
