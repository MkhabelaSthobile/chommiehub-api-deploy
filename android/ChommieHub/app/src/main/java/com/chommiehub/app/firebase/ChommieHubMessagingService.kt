package com.chommiehub.app.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.chommiehub.app.R

/**
 * Handles Firebase Cloud Messaging notifications
 * and device token updates for ChommieHub.
 */
class ChommieHubMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ChommieHubFCM"
        private const val CHANNEL_ID = "chommiehub_updates"
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

        showNotification(title, body)
    }

    // Displays the received notification on the device.
    private fun showNotification(title: String, body: String) {

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            // Create a notification channel on Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "ChommieHub Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Residence applications, approvals and important updates"
                }

                notificationManager.createNotificationChannel(channel)
            }

            // Android 13+ requires notification permission.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Notification permission not granted")
                return
            }

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.chommiehub_app_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(this).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
        }


        // TODO: Route notification actions to the correct screen.
    }
