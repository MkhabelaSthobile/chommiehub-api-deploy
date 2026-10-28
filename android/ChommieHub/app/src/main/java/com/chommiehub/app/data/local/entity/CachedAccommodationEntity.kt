package com.chommiehub.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A copy of a residence saved on the phone so it can be shown offline.
 * The amenities list is stored as JSON text.
 */
@Entity(tableName = "cached_accommodations")
data class CachedAccommodationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val phoneNumber: String,
    val amenitiesJson: String,
    val institutionArea: String,
    val isActive: Boolean,
    val cachedAtMillis: Long
)