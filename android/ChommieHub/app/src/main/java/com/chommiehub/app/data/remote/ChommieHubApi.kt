package com.chommiehub.app.data.remote

import com.chommiehub.app.data.model.HealthResponse
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
import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.model.student.StudentApplicationResponse
import com.chommiehub.app.data.model.student.StudentResponse
import com.chommiehub.app.data.model.student.SubmitApplicationRequest
import com.chommiehub.app.data.model.student.UpdateStudentRequest
import com.chommiehub.app.data.model.student.UpdateStudentSettingsRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit API interface used by the ChommieHub Android application
 * to communicate with the ASP.NET Core backend.
 */
interface ChommieHubApi {

    // =========================================================
    // HEALTH CHECK
    // =========================================================

    @GET("api/health")
    suspend fun getHealth(): HealthResponse


    // =========================================================
    // STUDENT PROFILE
    // =========================================================

    @GET("api/students/{id}")
    suspend fun getStudent(
        @Path("id") id: String
    ): StudentResponse

    @POST("api/students")
    suspend fun createStudent(
        @Body request: CreateStudentRequest
    ): StudentResponse

    @PUT("api/students/{id}")
    suspend fun updateStudent(
        @Path("id") id: String,
        @Body request: UpdateStudentRequest
    ): StudentResponse

    @PUT("api/students/{id}/settings")
    suspend fun updateStudentSettings(
        @Path("id") id: String,
        @Body request: UpdateStudentSettingsRequest
    ): StudentResponse


    // =========================================================
    // ACCOMMODATION DISCOVERY
    // =========================================================

    @GET("api/accommodations")
    suspend fun getAllAccommodations(): List<AccommodationResponse>

    @GET("api/accommodations/{id}")
    suspend fun getAccommodation(
        @Path("id") id: String
    ): AccommodationResponse


    // =========================================================
    // STUDENT APPLICATIONS
    // =========================================================

    @POST("api/applications/{id}")
    suspend fun submitApplication(
        @Path("id") id: String,
        @Body request: SubmitApplicationRequest
    ): StudentApplicationResponse

    @GET("api/applications/student/{id}")
    suspend fun getStudentApplications(
        @Path("id") id: String
    ): List<StudentApplicationResponse>


    // =========================================================
    // MANAGER DASHBOARD
    // =========================================================

    @GET("api/manager/{accommodationId}/dashboard")
    suspend fun getManagerDashboard(
        @Path("accommodationId") accommodationId: String
    ): ManagerDashboardResponse


    // =========================================================
    // MANAGER APPLICATIONS
    // =========================================================

    @GET("api/manager/{accommodationId}/applications")
    suspend fun getManagerApplications(
        @Path("accommodationId") accommodationId: String
    ): List<ManagerApplicationSummaryResponse>

    @GET("api/manager/applications/{applicationId}")
    suspend fun getManagerApplication(
        @Path("applicationId") applicationId: String
    ): ManagerApplicationDetailResponse

    @PUT("api/manager/applications/{applicationId}/review")
    suspend fun reviewApplication(
        @Path("applicationId") applicationId: String,
        @Body request: ReviewApplicationRequest
    ): ManagerApplicationDetailResponse


    // =========================================================
    // MANAGER ROOM ASSIGNMENT
    // =========================================================

    @POST("api/manager/applications/{applicationId}/assign-room")
    suspend fun assignRoom(
        @Path("applicationId") applicationId: String,
        @Body request: AssignRoomRequest
    ): ResidentResponse


    // =========================================================
    // MANAGER RESIDENTS
    // =========================================================

    @GET("api/manager/{accommodationId}/residents")
    suspend fun getResidents(
        @Path("accommodationId") accommodationId: String
    ): List<ResidentResponse>


    // =========================================================
    // ACCOMMODATION MANAGEMENT
    // =========================================================

    @PUT("api/accommodations/{id}")
    suspend fun updateAccommodation(
        @Path("id") id: String,
        @Body request: UpdateAccommodationRequest
    ): AccommodationResponse


    // =========================================================
    // ROOM MANAGEMENT
    // =========================================================

    @GET("api/accommodations/{accommodationId}/rooms")
    suspend fun getRooms(
        @Path("accommodationId") accommodationId: String
    ): List<RoomResponse>

    @GET("api/rooms/{roomId}")
    suspend fun getRoom(
        @Path("roomId") roomId: String
    ): RoomResponse

    @POST("api/accommodations/{accommodationId}/rooms")
    suspend fun createRoom(
        @Path("accommodationId") accommodationId: String,
        @Body request: CreateRoomRequest
    ): RoomResponse

    @PUT("api/rooms/{roomId}")
    suspend fun updateRoom(
        @Path("roomId") roomId: String,
        @Body request: UpdateRoomRequest
    ): RoomResponse
}