
package com.chommiehub.app

import android.app.Application
import android.app.NotificationManager
import com.chommiehub.app.firebase.ChommieHubMessagingService

/**
 * Initializes app-wide services when ChommieHub starts.
 */
class ChommieHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Create the FCM notification channel before
        // Firebase displays background notifications.
        val notificationManager =
            getSystemService(NotificationManager::class.java)

        ChommieHubMessagingService.createNotificationChannel(
            notificationManager
        )
    }
}
