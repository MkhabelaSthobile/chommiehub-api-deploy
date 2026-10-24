
package com.chommiehub.app.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

object FcmTokenManager {

    private const val TAG = "ChommieHubFCM"

    fun refreshTokenForCurrentUser() {

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.d(TAG, "No signed-in user. Token registration skipped.")
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->

                // The user may have signed out while
                // Firebase was retrieving the token.
                val currentUser =
                    FirebaseAuth.getInstance().currentUser

                if (currentUser?.uid != user.uid) {
                    Log.d(TAG, "User session changed. Registration skipped.")
                    return@addOnSuccessListener
                }

                Log.d(TAG, "FCM token retrieved successfully")

                // TODO: Register this token through an authenticated
                // backend endpoint once it is available.
                // Never log the full token.
            }
            .addOnFailureListener { exception ->

                Log.e(
                    TAG,
                    "Failed to retrieve FCM token",
                    exception
                )
            }
    }
}
