
package com.chommiehub.app

import android.app.Application
import android.app.NotificationManager
import com.chommiehub.app.firebase.ChommieHubMessagingService
import com.chommiehub.app.data.local.AppDatabase

//Initializes app-wide services when ChommieHub starts.
class ChommieHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Prepare the on-phone database used for offline shortlists.
        AppDatabase.getInstance(this)

        // Create the FCM notification channel before
        // Firebase displays background notifications.
        val notificationManager =
            getSystemService(NotificationManager::class.java)

        ChommieHubMessagingService.createNotificationChannel(
            notificationManager
        )
    }
}
