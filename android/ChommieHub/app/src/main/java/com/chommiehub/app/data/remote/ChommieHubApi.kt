package com.chommiehub.app.data.remote

import com.chommiehub.app.data.model.HealthResponse
import com.chommiehub.app.data.model.manager.AssignRoomRequest
import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.manager.ReviewApplicationRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

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
}