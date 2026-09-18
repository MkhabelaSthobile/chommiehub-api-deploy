package com.chommiehub.app.data.remote

import com.chommiehub.app.data.model.HealthResponse
import com.chommiehub.app.data.model.manager.AssignRoomRequest
import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.manager.ReviewApplicationRequest
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.model.manager.CreateRoomRequest
import com.chommiehub.app.data.model.manager.UpdateRoomRequest
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.UpdateAccommodationRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

/**
 * Defines HTTP operations exposed by the ChommieHub REST API
 * Android UI code communicates with this contract through repositories rather than performing network requests directly
 */
interface ChommieHubApi {

    /**
     * Confirms that the custom ASP.NET Core backend is reachable
     */
    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    /**
     * Returns live summary statistics for a manager's accommodation
     */
    @GET("api/manager/accommodations/{accommodationId}/dashboard")
    suspend fun getManagerDashboard(
        @Path("accommodationId") accommodationId: String): ManagerDashboardResponse

    /**
     * Returns applications submitted to the selected accommodation.
     */
    @GET("api/manager/accommodations/{accommodationId}/applications")
    suspend fun getManagerApplications(
        @Path("accommodationId") accommodationId: String): List<ManagerApplicationSummaryResponse>

    /**
     * Returns the complete information for one application.
     */
    @GET("api/manager/applications/{applicationId}")
    suspend fun getManagerApplication(
        @Path("applicationId") applicationId: String): ManagerApplicationDetailResponse

    /**
     * Approves or declines an accommodation application.
     */
    @PATCH("api/manager/applications/{applicationId}/review")
    suspend fun reviewApplication(
        @Path("applicationId") applicationId: String,
        @Body request: ReviewApplicationRequest): ManagerApplicationDetailResponse

    /**
     * Assigns a room to an approved applicant.
     */
    @POST("api/manager/applications/{applicationId}/assign-room")
    suspend fun assignRoom(
        @Path("applicationId") applicationId: String,
        @Body request: AssignRoomRequest): ResidentResponse

    /**
     * Returns active residents belonging to an accommodation
     */
    @GET("api/manager/accommodations/{accommodationId}/residents")
    suspend fun getResidents(
        @Path("accommodationId") accommodationId: String): List<ResidentResponse>

    /**
     * Returns all rooms belonging to the selected accommodation.
     */
    @GET("api/accommodations/{accommodationId}/rooms")
    suspend fun getRooms(
        @Path("accommodationId")
        accommodationId: String
    ): List<RoomResponse>

    /**
     * Returns the accommodation managed by the current residence manager
     */
    @GET("api/accommodations/{accommodationId}")
    suspend fun getAccommodation(
        @Path("accommodationId")
        accommodationId: String
    ): AccommodationResponse

    /**
     * updates the manager's accommodation information.
     */
    @PUT("api/accommodations/{accommodationId}")
    suspend fun updateAccommodation(
        @Path("accommodationId")
        accommodationId: String,
        @Body request: UpdateAccommodationRequest
    ): AccommodationResponse

    /**
     * Creates a new room under an accommodation.
     */
    @POST("api/accommodations/{accommodationId}/rooms")
    suspend fun createRoom(
        @Path("accommodationId")
        accommodationId: String,
        @Body request: CreateRoomRequest
    ): RoomResponse

    /**
     * Retrieves an individual room.
     */
    @GET("api/rooms/{roomId}")
    suspend fun getRoom(
        @Path("roomId")
        roomId: String
    ): RoomResponse

    /**
     * Updates room configuration and availability.
     */
    @PUT("api/rooms/{roomId}")
    suspend fun updateRoom(
        @Path("roomId")
        roomId: String,
        @Body request: UpdateRoomRequest
    ): RoomResponse
}