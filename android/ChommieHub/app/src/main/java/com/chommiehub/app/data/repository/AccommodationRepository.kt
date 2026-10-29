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
        api.addToShortlist(studentId, accommodationId)
    }

    suspend fun removeFromShortlist(studentId: String, accommodationId: String) {
        api.removeFromShortlist(studentId, accommodationId)
    }

    suspend fun getWeather(accommodationId: String): WeatherResponse {
        return api.getAccommodationWeather(accommodationId)
    }
}