package com.chommiehub.app.data.local.entity

import androidx.room.Entity

/** One row per student and residence. Holds the student's latest choice
 * (shortlisted or removed) and whether the server has it yet.
 */
@Entity(
    tableName = "shortlist_entries",
    primaryKeys = ["studentId", "accommodationId"]
)
data class ShortlistEntryEntity(
    val studentId: String,
    val accommodationId: String,
    val isShortlisted: Boolean,
    val syncState: SyncState,
    val updatedAtMillis: Long,
    val attemptCount: Int = 0
)