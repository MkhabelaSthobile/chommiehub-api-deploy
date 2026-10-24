
package com.chommiehub.app.firebase

import android.util.Log
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.remote.FcmDeviceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Retrieves and registers FCM device tokens
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

            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to register FCM device",
                    exception
                )
            }
        }
    }
}
