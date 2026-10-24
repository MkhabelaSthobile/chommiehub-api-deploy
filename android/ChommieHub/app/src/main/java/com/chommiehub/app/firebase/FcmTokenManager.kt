
package com.chommiehub.app.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Manages Firebase Cloud Messaging device tokens.
 */
object FcmTokenManager {

    private const val TAG = "ChommieHubFCM"

    /**
     * Retrieves the current FCM token after a user signs in.
     */
    fun refreshTokenForCurrentUser() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.d(
                TAG,
                "No signed-in user. Token registration skipped."
            )
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->

                val currentUser =
                    FirebaseAuth.getInstance().currentUser

                if (currentUser?.uid != user.uid) {
                    Log.d(
                        TAG,
                        "User session changed. Registration skipped."
                    )
                    return@addOnSuccessListener
                }

                Log.d(TAG, "FCM token retrieved successfully")

                // TODO: Register token with authenticated backend.
                // Never log the full FCM token.
            }
            .addOnFailureListener { exception ->

                Log.e(
                    TAG,
                    "Failed to retrieve FCM token",
                    exception
                )
            }
    }

    /**
     * Handles a newly generated or refreshed FCM token.
     */
    fun handleRefreshedToken(token: String) {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.d(
                TAG,
                "No signed-in user. Refreshed token registration deferred."
            )
            return
        }

        if (token.isBlank()) {
            Log.w(TAG, "Received an empty FCM token")
            return
        }

        Log.d(
            TAG,
            "FCM token refresh detected for an authenticated user"
        )

        // TODO: Register refreshed token with authenticated backend.
        // Backend must verify Firebase ID token and user identity.
    }
}
