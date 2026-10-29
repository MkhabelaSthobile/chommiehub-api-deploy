package com.chommiehub.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chommiehub.app.data.local.AppDatabase
import com.chommiehub.app.data.local.dao.ShortlistDao
import com.chommiehub.app.data.local.entity.ShortlistEntryEntity
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.remote.ChommieHubApi
import retrofit2.HttpException
import java.io.IOException

/**
 * Background job that sends shortlist changes saved on the phone to the API.
 */
class ShortlistSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getInstance(applicationContext).shortlistDao()
        val api = ApiClient.api

        while (true) {
            val pending = dao.getPendingEntries()
            if (pending.isEmpty()) return Result.success()

            for (entry in pending) {
                when (send(api, entry)) {
                    SendResult.SENT -> dao.markSyncedIfUnchanged(
                        entry.studentId,
                        entry.accommodationId,
                        entry.updatedAtMillis
                    )

                    SendResult.REJECTED -> dao.markFailedIfUnchanged(
                        entry.studentId,
                        entry.accommodationId,
                        entry.updatedAtMillis
                    )

                    SendResult.RETRY_LATER -> {
                        recordTemporaryFailure(dao, entry)
                        return Result.retry()
                    }
                }
            }
        }
    }

    private suspend fun send(api: ChommieHubApi, entry: ShortlistEntryEntity): SendResult {
        return try {
            if (entry.isShortlisted) {
                api.addToShortlist(entry.studentId, entry.accommodationId)
            } else {
                api.removeFromShortlist(entry.studentId, entry.accommodationId)
            }
            SendResult.SENT
        } catch (_: IOException) {
            // No connection or the server could not be reached.
            SendResult.RETRY_LATER
        } catch (exception: HttpException) {
            if (exception.isTemporary()) SendResult.RETRY_LATER else SendResult.REJECTED
        }
    }

    // Counts one failed attempt, and gives up after MAX_ATTEMPTS.
    private suspend fun recordTemporaryFailure(dao: ShortlistDao, entry: ShortlistEntryEntity) {
        if (entry.attemptCount + 1 >= MAX_ATTEMPTS) {
            dao.markFailedIfUnchanged(
                entry.studentId,
                entry.accommodationId,
                entry.updatedAtMillis
            )
        } else {
            dao.addAttemptIfUnchanged(
                entry.studentId,
                entry.accommodationId,
                entry.updatedAtMillis
            )
        }
    }

    // Server trouble, not signed in yet (401), timeout or too many requests.
    private fun HttpException.isTemporary(): Boolean {
        val statusCode = code()
        return statusCode >= 500 || statusCode == 401 || statusCode == 408 || statusCode == 429
    }

    private enum class SendResult {
        SENT,
        REJECTED,
        RETRY_LATER
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}