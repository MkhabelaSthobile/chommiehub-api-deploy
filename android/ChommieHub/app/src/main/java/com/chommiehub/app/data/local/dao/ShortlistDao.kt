package com.chommiehub.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.chommiehub.app.data.local.entity.CachedAccommodationEntity
import com.chommiehub.app.data.local.entity.ShortlistEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortlistDao {

    // ---- Cached residences ----

    @Upsert
    suspend fun upsertAccommodations(accommodations: List<CachedAccommodationEntity>)

    @Query("SELECT * FROM cached_accommodations")
    suspend fun getAllCachedAccommodations(): List<CachedAccommodationEntity>

    @Query("SELECT * FROM cached_accommodations WHERE id = :accommodationId")
    suspend fun getCachedAccommodation(accommodationId: String): CachedAccommodationEntity?

    // ---- Shortlist entries ----

    @Upsert
    suspend fun upsertEntry(entry: ShortlistEntryEntity)

    @Upsert
    suspend fun upsertEntries(entries: List<ShortlistEntryEntity>)

    @Query(
        "SELECT * FROM shortlist_entries " +
                "WHERE studentId = :studentId AND accommodationId = :accommodationId"
    )
    suspend fun getEntry(studentId: String, accommodationId: String): ShortlistEntryEntity?

    @Query("SELECT * FROM shortlist_entries WHERE studentId = :studentId")
    suspend fun getEntries(studentId: String): List<ShortlistEntryEntity>

    // Emits a new list every time a shortlist row changes (used for live status labels).
    @Query("SELECT * FROM shortlist_entries WHERE studentId = :studentId")
    fun observeEntries(studentId: String): Flow<List<ShortlistEntryEntity>>

    @Query(
        "DELETE FROM shortlist_entries " +
                "WHERE studentId = :studentId AND accommodationId = :accommodationId"
    )
    suspend fun deleteEntry(studentId: String, accommodationId: String)

    // ---- Shortlist screen ----

    // Residences the student currently has shortlisted (removed ones are hidden).
    @Query(
        "SELECT a.* FROM cached_accommodations a " +
                "INNER JOIN shortlist_entries s ON s.accommodationId = a.id " +
                "WHERE s.studentId = :studentId AND s.isShortlisted = 1 " +
                "ORDER BY s.updatedAtMillis DESC"
    )
    suspend fun getShortlistedAccommodations(studentId: String): List<CachedAccommodationEntity>

    // ---- Background sync ----

    // Changes still waiting to be sent, oldest first.
    @Query(
        "SELECT * FROM shortlist_entries " +
                "WHERE syncState = 'PENDING' " +
                "ORDER BY updatedAtMillis ASC"
    )
    suspend fun getPendingEntries(): List<ShortlistEntryEntity>

    // The three updates below only apply if the row has not been changed again.
    @Query(
        "UPDATE shortlist_entries SET syncState = 'SYNCED', attemptCount = 0 " +
                "WHERE studentId = :studentId AND accommodationId = :accommodationId " +
                "AND updatedAtMillis = :updatedAtMillis"
    )
    suspend fun markSyncedIfUnchanged(
        studentId: String,
        accommodationId: String,
        updatedAtMillis: Long
    ): Int

    @Query(
        "UPDATE shortlist_entries SET syncState = 'FAILED', attemptCount = attemptCount + 1 " +
                "WHERE studentId = :studentId AND accommodationId = :accommodationId " +
                "AND updatedAtMillis = :updatedAtMillis"
    )
    suspend fun markFailedIfUnchanged(
        studentId: String,
        accommodationId: String,
        updatedAtMillis: Long
    ): Int

    @Query(
        "UPDATE shortlist_entries SET attemptCount = attemptCount + 1 " +
                "WHERE studentId = :studentId AND accommodationId = :accommodationId " +
                "AND updatedAtMillis = :updatedAtMillis"
    )
    suspend fun addAttemptIfUnchanged(
        studentId: String,
        accommodationId: String,
        updatedAtMillis: Long
    ): Int

}