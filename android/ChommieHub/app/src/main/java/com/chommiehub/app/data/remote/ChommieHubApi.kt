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
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ChommieHubApi {

    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @GET("api/students/{id}")
    suspend fun getStudent(@Path("id") id: String): StudentResponse

    @POST("api/students")
    suspend fun createStudent(@Body request: CreateStudentRequest): StudentResponse

    @PUT("api/students/{id}")
    suspend fun updateStudent(@Path("id") id: String, @Body request: UpdateStudentRequest): StudentResponse

    @PUT("api/students/{id}/settings")
    suspend fun updateStudentSettings(@Path("id") id: String, @Body request: UpdateStudentSettingsRequest): StudentResponse

    @GET("api/accommodations")
    suspend fun getAllAccommodations(): List<AccommodationResponse>

    @GET("api/accommodations/{id}")
    suspend fun getAccommodation(@Path("id") id: String): AccommodationResponse

    @POST("api/students/{id}/applications")
    suspend fun submitApplication(@Path("id") id: String, @Body request: SubmitApplicationRequest): StudentApplicationResponse

    @GET("api/students/{id}/applications")
    suspend fun getStudentApplications(@Path("id") id: String): List<StudentApplicationResponse>

    @GET("api/manager/accommodations/{accommodationId}/dashboard")
    suspend fun getManagerDashboard(@Path("accommodationId") accommodationId: String): ManagerDashboardResponse

    @GET("api/manager/accommodations/{accommodationId}/applications")
    suspend fun getManagerApplications(@Path("accommodationId") accommodationId: String): List<ManagerApplicationSummaryResponse>

    @GET("api/manager/applications/{applicationId}")
    suspend fun getManagerApplication(@Path("applicationId") applicationId: String): ManagerApplicationDetailResponse

    @PUT("api/manager/applications/{applicationId}/review")
    suspend fun reviewApplication(@Path("applicationId") applicationId: String, @Body request: ReviewApplicationRequest): ManagerApplicationDetailResponse

    @POST("api/manager/applications/{applicationId}/assign-room")
    suspend fun assignRoom(@Path("applicationId") applicationId: String, @Body request: AssignRoomRequest): ResidentResponse

    @GET("api/manager/accommodations/{accommodationId}/residents")
    suspend fun getResidents(@Path("accommodationId") accommodationId: String): List<ResidentResponse>

    @PUT("api/accommodations/{id}")
    suspend fun updateAccommodation(@Path("id") id: String, @Body request: UpdateAccommodationRequest): AccommodationResponse

    @GET("api/accommodations/{accommodationId}/rooms")
    suspend fun getRooms(@Path("accommodationId") accommodationId: String): List<RoomResponse>

    @GET("api/rooms/{roomId}")
    suspend fun getRoom(@Path("roomId") roomId: String): RoomResponse

    @POST("api/accommodations/{accommodationId}/rooms")
    suspend fun createRoom(@Path("accommodationId") accommodationId: String, @Body request: CreateRoomRequest): RoomResponse

    @PUT("api/rooms/{roomId}")
    suspend fun updateRoom(@Path("roomId") roomId: String, @Body request: UpdateRoomRequest): RoomResponse

    @GET("api/students/{studentId}/shortlist")
    suspend fun getShortlist(@Path("studentId") studentId: String): List<AccommodationResponse>

    @POST("api/students/{studentId}/shortlist/{accommodationId}")
    suspend fun addToShortlist(@Path("studentId") studentId: String, @Path("accommodationId") accommodationId: String)

    @DELETE("api/students/{studentId}/shortlist/{accommodationId}")
    suspend fun removeFromShortlist(@Path("studentId") studentId: String, @Path("accommodationId") accommodationId: String)
}