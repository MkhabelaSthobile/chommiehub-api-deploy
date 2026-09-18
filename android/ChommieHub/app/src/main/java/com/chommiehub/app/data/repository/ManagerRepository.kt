package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.manager.AssignRoomRequest
import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.manager.ReviewApplicationRequest
import com.chommiehub.app.data.remote.ChommieHubApi
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.model.manager.CreateRoomRequest
import com.chommiehub.app.data.model.manager.UpdateRoomRequest
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.UpdateAccommodationRequest

/**
 * Repository used by manager-facing Android features
 * Keeping API access here prevents Activities and Fragments from being coupled directly to Retrofit
 */
class ManagerRepository(private val api: ChommieHubApi)
{
    suspend fun getDashboard(accommodationId: String): ManagerDashboardResponse
    {
        return api.getManagerDashboard(accommodationId)
    }

    suspend fun getApplications(accommodationId: String): List<ManagerApplicationSummaryResponse>
    {
        return api.getManagerApplications(accommodationId)
    }

    suspend fun getApplication(applicationId: String): ManagerApplicationDetailResponse
    {
        return api.getManagerApplication(applicationId)
    }

    suspend fun reviewApplication(applicationId: String, decision: String, reviewNotes: String): ManagerApplicationDetailResponse
    {
        return api.reviewApplication(applicationId, ReviewApplicationRequest
            (
                decision = decision,
                reviewNotes = reviewNotes
            )
        )
    }

    suspend fun assignRoom(applicationId: String, roomId: String): ResidentResponse
    {
        return api.assignRoom(applicationId, AssignRoomRequest(roomId))
    }

    suspend fun getResidents(accommodationId: String): List<ResidentResponse>
    {
        return api.getResidents(accommodationId)
    }

    /**
     *returns only rooms that are currently available for assignment
     */
    suspend fun getAvailableRooms(accommodationId: String): List<RoomResponse>
    {
        return api.getRooms(accommodationId).filter {
            room -> room.availability
        }
    }

    /**
     *Retrieves the manager's current accommodation information
     */
    suspend fun getAccommodation(accommodationId: String): AccommodationResponse
    {
        return api.getAccommodation(accommodationId)
    }

    /**
     * Updates accommodation information through the custom REST API
     */
    suspend fun updateAccommodation(accommodationId: String, request: UpdateAccommodationRequest): AccommodationResponse
    {
        return api.updateAccommodation(
            accommodationId,
            request
        )
    }

    suspend fun getRooms(accommodationId: String): List<RoomResponse>
    {
        return api.getRooms(accommodationId)
    }

    suspend fun getRoom(roomId: String): RoomResponse
    {
        return api.getRoom(roomId)
    }

    suspend fun createRoom(accommodationId: String, request: CreateRoomRequest): RoomResponse
    {
        return api.createRoom(
            accommodationId,
            request
        )
    }

    suspend fun updateRoom(roomId: String, request: UpdateRoomRequest): RoomResponse
    {
        return api.updateRoom(
            roomId,
            request
        )
    }
}