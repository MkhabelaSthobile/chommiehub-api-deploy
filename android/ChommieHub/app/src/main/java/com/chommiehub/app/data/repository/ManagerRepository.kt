package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.AssignRoomRequest
import com.chommiehub.app.data.model.manager.CreateRoomRequest
import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.manager.ReviewApplicationRequest
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.model.manager.UpdateAccommodationRequest
import com.chommiehub.app.data.model.manager.UpdateRoomRequest
import com.chommiehub.app.data.remote.ChommieHubApi

/**
 * Repository used by manager-facing Android features.
 *
 * Keeping API access here prevents Activities and Fragments
 * from being coupled directly to Retrofit.
 */
class ManagerRepository(
    private val api: ChommieHubApi
) {

    /**
     * Retrieves dashboard statistics for the manager's accommodation.
     */
    suspend fun getDashboard(
        accommodationId: String
    ): ManagerDashboardResponse {
        return api.getManagerDashboard(accommodationId)
    }

    /**
     * Retrieves applications submitted to the manager's accommodation.
     */
    suspend fun getApplications(
        accommodationId: String
    ): List<ManagerApplicationSummaryResponse> {
        return api.getManagerApplications(accommodationId)
    }

    /**
     * Retrieves the full details of a specific application.
     */
    suspend fun getApplication(
        applicationId: String
    ): ManagerApplicationDetailResponse {
        return api.getManagerApplication(applicationId)
    }

    /**
     * Approves or declines an accommodation application.
     */
    suspend fun reviewApplication(
        applicationId: String,
        decision: String,
        reviewNotes: String
    ): ManagerApplicationDetailResponse {

        val request = ReviewApplicationRequest(
            decision = decision,
            reviewNotes = reviewNotes
        )

        return api.reviewApplication(
            applicationId,
            request
        )
    }

    /**
     * Assigns a room to an approved student application.
     */
    suspend fun assignRoom(
        applicationId: String,
        roomId: String
    ): ResidentResponse {

        val request = AssignRoomRequest(
            roomId = roomId
        )

        return api.assignRoom(
            applicationId,
            request
        )
    }

    /**
     * Retrieves residents belonging to the accommodation.
     */
    suspend fun getResidents(
        accommodationId: String
    ): List<ResidentResponse> {
        return api.getResidents(accommodationId)
    }

    /**
     * Retrieves only rooms that are currently available
     * for assignment.
     */
    suspend fun getAvailableRooms(
        accommodationId: String
    ): List<RoomResponse> {

        return api
            .getRooms(accommodationId)
            .filter { room ->
                room.availability
            }
    }

    /**
     * Retrieves the manager's accommodation information.
     */
    suspend fun getAccommodation(
        accommodationId: String
    ): AccommodationResponse {
        return api.getAccommodation(accommodationId)
    }

    /**
     * Updates the manager's accommodation information.
     */
    suspend fun updateAccommodation(
        accommodationId: String,
        request: UpdateAccommodationRequest
    ): AccommodationResponse {

        return api.updateAccommodation(
            accommodationId,
            request
        )
    }

    /**
     * Retrieves all rooms belonging to an accommodation.
     */
    suspend fun getRooms(
        accommodationId: String
    ): List<RoomResponse> {
        return api.getRooms(accommodationId)
    }

    /**
     * Retrieves a specific room.
     */
    suspend fun getRoom(
        roomId: String
    ): RoomResponse {
        return api.getRoom(roomId)
    }

    /**
     * Creates a new room for an accommodation.
     */
    suspend fun createRoom(
        accommodationId: String,
        request: CreateRoomRequest
    ): RoomResponse {

        return api.createRoom(
            accommodationId,
            request
        )
    }

    /**
     * Updates an existing room.
     */
    suspend fun updateRoom(
        roomId: String,
        request: UpdateRoomRequest
    ): RoomResponse {

        return api.updateRoom(
            roomId,
            request
        )
    }
}