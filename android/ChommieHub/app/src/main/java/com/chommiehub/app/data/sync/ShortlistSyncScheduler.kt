package com.chommiehub.app.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Queues the background job that sends offline shortlist changes to the API.
 */
object ShortlistSyncScheduler {

    private const val UNIQUE_WORK_NAME = "shortlist_sync"

    @Volatile
    private var appContext: Context? = null

    /** Called once from ChommieHubApplication. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** Queues a sync that runs as soon as the phone has internet. */
    fun schedule() {
        val context = appContext ?: return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<ShortlistSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}