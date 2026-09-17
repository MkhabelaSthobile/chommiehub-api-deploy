package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.manager.AssignRoomRequest
import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.manager.ReviewApplicationRequest
import com.chommiehub.app.data.remote.ChommieHubApi
import com.chommiehub.app.data.model.manager.RoomResponse

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
     * Returns only rooms that are currently available for assignment
     */
    suspend fun getAvailableRooms(
        accommodationId: String
    ): List<RoomResponse> {
        return api.getRooms(
            accommodationId
        ).filter { room ->
            room.availability
        }
    }
}