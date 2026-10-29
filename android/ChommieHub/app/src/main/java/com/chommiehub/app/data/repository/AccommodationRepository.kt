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
import com.chommiehub.app.data.sync.ShortlistSyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        try {
            val serverShortlist = api.getShortlist(studentId)
            saveServerShortlist(studentId, serverShortlist)
        } catch (_: IOException) {
            // Offline: show what is saved on the phone.
        } catch (exception: HttpException) {
            if (!exception.isServerError()) throw exception
        }

        return dao.getShortlistedAccommodations(studentId).map { it.toResponse() }
    }

    /** Sync status (PENDING / SYNCED / FAILED) of each shortlisted residence, kept up to date. */
    fun observeShortlistStatuses(studentId: String): Flow<Map<String, SyncState>> {
        return dao.observeEntries(studentId).map { entries ->
            entries
                .filter { it.isShortlisted }
                .associate { it.accommodationId to it.syncState }
        }
    }

    /** Merges the server's shortlist into the phone's, keeping unsent changes. */
    private suspend fun saveServerShortlist(
        studentId: String,
        serverShortlist: List<AccommodationResponse>
    ) {
        dao.upsertAccommodations(serverShortlist.map { it.toEntity() })

        val serverIds = serverShortlist.map { it.id }.toSet()
        val localEntries = dao.getEntries(studentId).associateBy { it.accommodationId }
        val now = System.currentTimeMillis()

        // Remove synced rows the server no longer has (for example removed on another device).
        localEntries.values
            .filter { it.syncState == SyncState.SYNCED && it.accommodationId !in serverIds }
            .forEach { dao.deleteEntry(studentId, it.accommodationId) }

        // Add or refresh what the server has, unless a local change is still waiting.
        val entriesToSave = serverIds
            .filter { id ->
                val state = localEntries[id]?.syncState
                state == null || state == SyncState.SYNCED
            }
            .map { id ->
                ShortlistEntryEntity(
                    studentId = studentId,
                    accommodationId = id,
                    isShortlisted = true,
                    syncState = SyncState.SYNCED,
                    updatedAtMillis = localEntries[id]?.updatedAtMillis ?: now
                )
            }

        dao.upsertEntries(entriesToSave)
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
            ShortlistSyncScheduler.schedule()
        } catch (exception: HttpException) {
            if (exception.isServerError()) {
                // Server trouble: the change stays PENDING so it can be retried.
                ShortlistSyncScheduler.schedule()
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