package com.chommiehub.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.chommiehub.app.data.local.entity.CachedAccommodationEntity
import com.chommiehub.app.data.local.entity.ShortlistEntryEntity

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
}