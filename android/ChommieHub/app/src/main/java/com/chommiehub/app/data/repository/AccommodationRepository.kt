package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.model.weather.WeatherResponse
import com.chommiehub.app.data.remote.ChommieHubApi

/**
 * Repository used by student-facing accommodation discovery features.
 *
 * Keeping Retrofit calls inside the repository prevents Android UI classes
 * from depending directly on REST implementation details.
 */
class AccommodationRepository(private val api: ChommieHubApi) {

    suspend fun getAllAccommodations(): List<AccommodationResponse> {
        return api.getAllAccommodations()
    }

    suspend fun getAccommodation(accommodationId: String): AccommodationResponse {
        return api.getAccommodation(accommodationId)
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