package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.model.weather.WeatherResponse
import com.chommiehub.app.data.remote.ChommieHubApi
import com.chommiehub.app.data.local.AppDatabase
import com.chommiehub.app.data.local.dao.ShortlistDao
import com.chommiehub.app.data.local.entity.toEntity
import com.chommiehub.app.data.local.entity.toResponse
import java.io.IOException
import com.chommiehub.app.data.local.entity.ShortlistEntryEntity
import com.chommiehub.app.data.local.entity.SyncState
import retrofit2.HttpException

/**
 * Repository used by student-facing accommodation discovery features.
 *
 * Keeping Retrofit calls inside the repository prevents Android UI classes
 * from depending directly on REST implementation details.
 */
class AccommodationRepository(private val api: ChommieHubApi) {

    private val dao: ShortlistDao
        get() = AppDatabase.get().shortlistDao()

    suspend fun getAllAccommodations(): List<AccommodationResponse> {
        return try {
            val accommodations = api.getAllAccommodations()
            dao.upsertAccommodations(accommodations.map { it.toEntity() })
            accommodations
        } catch (exception: IOException) {
            // Offline: fall back to the copies saved on the phone.
            val cached = dao.getAllCachedAccommodations()
            if (cached.isEmpty()) throw exception
            cached.map { it.toResponse() }
        }
    }

    suspend fun getAccommodation(accommodationId: String): AccommodationResponse {
        return try {
            val accommodation = api.getAccommodation(accommodationId)
            dao.upsertAccommodations(listOf(accommodation.toEntity()))
            accommodation
        } catch (exception: IOException) {
            dao.getCachedAccommodation(accommodationId)?.toResponse() ?: throw exception
        }
    }

    suspend fun getRooms(accommodationId: String): List<RoomResponse> {
        return api.getRooms(accommodationId)
    }

    suspend fun getRoom(roomId: String): RoomResponse {
        return api.getRoom(roomId)
    }

    suspend fun getShortlist(studentId: String): List<AccommodationResponse> {
        return api.getShortlist(studentId)
    }

    suspend fun addToShortlist(studentId: String, accommodationId: String) {
        val entry = saveShortlistChange(studentId, accommodationId, isShortlisted = true)
        sendOrQueue(entry) { api.addToShortlist(studentId, accommodationId) }
    }

    suspend fun removeFromShortlist(studentId: String, accommodationId: String) {
        val entry = saveShortlistChange(studentId, accommodationId, isShortlisted = false)
        sendOrQueue(entry) { api.removeFromShortlist(studentId, accommodationId) }
    }

    /** Saves the student's choice on the phone first, marked as waiting to be sent. */
    private suspend fun saveShortlistChange(
        studentId: String,
        accommodationId: String,
        isShortlisted: Boolean
    ): ShortlistEntryEntity {
        val entry = ShortlistEntryEntity(
            studentId = studentId,
            accommodationId = accommodationId,
            isShortlisted = isShortlisted,
            syncState = SyncState.PENDING,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertEntry(entry)
        return entry
    }

    /** Tries the server now. If that is not possible, the change stays PENDING. */
    private suspend fun sendOrQueue(entry: ShortlistEntryEntity, send: suspend () -> Unit) {
        try {
            send()
            markSyncedIfUnchanged(entry)
        } catch (_: IOException) {
            // Offline: the change stays PENDING and is sent later.
        } catch (exception: HttpException) {
            if (exception.isServerError()) {
                // Server trouble: the change stays PENDING so it can be retried.
            } else {
                markFailedIfUnchanged(entry)
                throw exception
            }
        }
    }

    // Only update the row if the student has not changed it again in the meantime.
    private suspend fun markSyncedIfUnchanged(entry: ShortlistEntryEntity) {
        val current = dao.getEntry(entry.studentId, entry.accommodationId) ?: return
        if (current.updatedAtMillis == entry.updatedAtMillis) {
            dao.upsertEntry(current.copy(syncState = SyncState.SYNCED, attemptCount = 0))
        }
    }

    private suspend fun markFailedIfUnchanged(entry: ShortlistEntryEntity) {
        val current = dao.getEntry(entry.studentId, entry.accommodationId) ?: return
        if (current.updatedAtMillis == entry.updatedAtMillis) {
            dao.upsertEntry(
                current.copy(
                    syncState = SyncState.FAILED,
                    attemptCount = current.attemptCount + 1
                )
            )
        }
    }

    private fun HttpException.isServerError(): Boolean = code() >= 500

    suspend fun getWeather(accommodationId: String): WeatherResponse {
        return api.getAccommodationWeather(accommodationId)
    }
}