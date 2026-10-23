package com.chommiehub.app.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles Firebase Cloud Messaging notifications
 * and device token updates for ChommieHub.
 */
class ChommieHubMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ChommieHubFCM"
    }

    /**
     * Called when Firebase issues or refreshes
     * this application's FCM registration token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Never log the full token in production.
        Log.d(TAG, "FCM registration token refreshed")

        // TODO: Register the token with the authenticated backend.
    }

    /**
     * Handles messages delivered to the application
     * while this service is active.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "ChommieHub"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new notification."

        Log.d(TAG, "Notification received: $title")

        // TODO: Display a system notification using NotificationManager.
        // TODO: Route notification actions to the correct screen.
    }
}