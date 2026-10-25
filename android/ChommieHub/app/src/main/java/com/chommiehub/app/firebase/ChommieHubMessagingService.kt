
package com.chommiehub.app.firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.chommiehub.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles Firebase Cloud Messaging notifications
 * and device token updates for ChommieHub.
 */
class ChommieHubMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ChommieHubFCM"

        const val CHANNEL_ID = "chommiehub_updates"

        /**
         * Creates the notification channel if it does not exist.
         *
         * Safe to call more than once.
         */
        fun createNotificationChannel(
            notificationManager: NotificationManager
        ) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ChommieHub Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    "Residence applications, approvals and important updates"
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()

        val notificationManager =
            getSystemService(NotificationManager::class.java)

        createNotificationChannel(notificationManager)
    }

    /**
     * Called when Firebase issues or refreshes
     * this application's FCM registration token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Never log the actual token.
        Log.d(TAG, "FCM registration token refreshed")

        FcmTokenManager.handleRefreshedToken(token)
    }

    /**
     * Handles messages delivered to this service.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "ChommieHub"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new notification."

        Log.d(TAG, "FCM notification received")

        showNotification(title, body)
    }

    /**
     * Displays a notification on the device.
     */
    private fun showNotification(
        title: String,
        body: String
    ) {
        val notificationManager =
            getSystemService(NotificationManager::class.java)

        // Ensure the channel exists.
        createNotificationChannel(notificationManager)

        // Android 13+ requires runtime notification permission.
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(
                TAG,
                "Notification permission not granted"
            )
            return
        }

        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.chommiehub_app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(body)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
        } catch (exception: SecurityException) {
            Log.e(
                TAG,
                "Notification could not be displayed",
                exception
            )
        }
    }

    // TODO: Add safe notification tap navigation.
}
