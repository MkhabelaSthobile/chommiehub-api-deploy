package com.chommiehub.app.data.remote

import com.chommiehub.app.data.model.HealthResponse
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.AssignRoomRequest
import com.chommiehub.app.data.model.manager.CreateManagerRequest
import com.chommiehub.app.data.model.manager.CreateRoomRequest
import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.model.manager.ManagerResponse
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.manager.ReviewApplicationRequest
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.model.manager.UpdateAccommodationRequest
import com.chommiehub.app.data.model.manager.UpdateRoomRequest
import com.chommiehub.app.data.model.resident.ConfirmInspectionRequest
import com.chommiehub.app.data.model.resident.CreateInspectionRequest
import com.chommiehub.app.data.model.resident.CreateMaintenanceRequest
import com.chommiehub.app.data.model.resident.CreateVisitorRequest
import com.chommiehub.app.data.model.resident.InspectionResponse
import com.chommiehub.app.data.model.resident.MaintenanceRequestResponse
import com.chommiehub.app.data.model.resident.UpdateMaintenanceStatusRequest
import com.chommiehub.app.data.model.resident.UpdateVisitorRequestStatusRequest
import com.chommiehub.app.data.model.resident.VisitorRequestResponse
import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.model.student.StudentApplicationResponse
import com.chommiehub.app.data.model.student.StudentResponse
import com.chommiehub.app.data.model.student.SubmitApplicationRequest
import com.chommiehub.app.data.model.student.UpdateStudentRequest
import com.chommiehub.app.data.model.student.UpdateStudentSettingsRequest
import com.chommiehub.app.data.model.weather.WeatherResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Header
import com.chommiehub.app.data.model.admin.AdminActionResponse
import com.chommiehub.app.data.model.admin.AdminDashboardResponse
import com.chommiehub.app.data.model.admin.AdminListingDetailResponse
import com.chommiehub.app.data.model.admin.AdminListingSummary
import com.chommiehub.app.data.model.admin.AdminReasonRequest
import com.chommiehub.app.data.model.admin.AdminUserDetailResponse
import com.chommiehub.app.data.model.admin.AdminUserSummary
import com.chommiehub.app.data.model.admin.AuditRecordResponse
import com.chommiehub.app.data.model.manager.ManagerAccommodationResponse
import com.chommiehub.app.data.model.manager.RegisterManagerRequest
import com.chommiehub.app.data.model.manager.SaveAccommodationRequest
import retrofit2.http.Query

interface ChommieHubApi {

    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @GET("api/students/{id}")
    suspend fun getStudent(@Path("id") id: String): StudentResponse

    @POST("api/students")
    suspend fun createStudent(@Body request: CreateStudentRequest): StudentResponse

    @GET("api/students/by-firebase-uid/{firebaseUid}")
    suspend fun getStudentByFirebaseUid(@Path("firebaseUid") firebaseUid: String): StudentResponse

    @PUT("api/students/{id}")
    suspend fun updateStudent(@Path("id") id: String, @Body request: UpdateStudentRequest): StudentResponse

    @PUT("api/students/{id}/settings")
    suspend fun updateStudentSettings(@Path("id") id: String, @Body request: UpdateStudentSettingsRequest): StudentResponse

    @GET("api/accommodations")
    suspend fun getAllAccommodations(): List<AccommodationResponse>

    @GET("api/accommodations/{id}")
    suspend fun getAccommodation(@Path("id") id: String): AccommodationResponse

    @GET("api/accommodations/{accommodationId}/rooms")
    suspend fun getRooms(@Path("accommodationId") accommodationId: String): List<RoomResponse>

    @GET("api/rooms/{roomId}")
    suspend fun getRoom(@Path("roomId") roomId: String): RoomResponse

    @GET("api/weather/accommodation/{accommodationId}")
    suspend fun getAccommodationWeather(@Path("accommodationId") accommodationId: String): WeatherResponse

    @POST("api/students/{id}/applications")
    suspend fun submitApplication(@Path("id") id: String, @Body request: SubmitApplicationRequest): StudentApplicationResponse

    @GET("api/students/{id}/applications")
    suspend fun getStudentApplications(@Path("id") id: String): List<StudentApplicationResponse>

    @GET("api/students/{studentId}/shortlist")
    suspend fun getShortlist(@Path("studentId") studentId: String): List<AccommodationResponse>

    @POST("api/students/{studentId}/shortlist/{accommodationId}")
    suspend fun addToShortlist(@Path("studentId") studentId: String, @Path("accommodationId") accommodationId: String)

    @DELETE("api/students/{studentId}/shortlist/{accommodationId}")
    suspend fun removeFromShortlist(@Path("studentId") studentId: String, @Path("accommodationId") accommodationId: String)

    @POST("api/managers")
    suspend fun createManager(@Body request: CreateManagerRequest): ManagerResponse

    @GET("api/managers/by-firebase-uid/{firebaseUid}")
    suspend fun getManagerByFirebaseUid(@Path("firebaseUid") firebaseUid: String): ManagerResponse

    @GET("api/manager/accommodations/{accommodationId}/dashboard")
    suspend fun getManagerDashboard(@Path("accommodationId") accommodationId: String): ManagerDashboardResponse

    @GET("api/manager/accommodations/{accommodationId}/applications")
    suspend fun getManagerApplications(@Path("accommodationId") accommodationId: String): List<ManagerApplicationSummaryResponse>

    @GET("api/manager/applications/{applicationId}")
    suspend fun getManagerApplication(@Path("applicationId") applicationId: String): ManagerApplicationDetailResponse

    @PATCH("api/manager/applications/{applicationId}/review")
    suspend fun reviewApplication(@Path("applicationId") applicationId: String, @Body request: ReviewApplicationRequest): ManagerApplicationDetailResponse

    @POST("api/manager/applications/{applicationId}/assign-room")
    suspend fun assignRoom(@Path("applicationId") applicationId: String, @Body request: AssignRoomRequest): ResidentResponse

    @GET("api/manager/accommodations/{accommodationId}/residents")
    suspend fun getResidents(@Path("accommodationId") accommodationId: String): List<ResidentResponse>

    @PUT("api/accommodations/{id}")
    suspend fun updateAccommodation(@Path("id") id: String, @Body request: UpdateAccommodationRequest): AccommodationResponse

    @POST("api/accommodations/{accommodationId}/rooms")
    suspend fun createRoom(@Path("accommodationId") accommodationId: String, @Body request: CreateRoomRequest): RoomResponse

    @PUT("api/rooms/{roomId}")
    suspend fun updateRoom(@Path("roomId") roomId: String, @Body request: UpdateRoomRequest): RoomResponse

    @GET("api/residents/student/{studentId}")
    suspend fun getResidentByStudentId(@Path("studentId") studentId: String): ResidentResponse

    @POST("api/maintenance")
    suspend fun createMaintenanceRequest(@Body request: CreateMaintenanceRequest): MaintenanceRequestResponse

    @GET("api/maintenance/resident/{residentId}")
    suspend fun getResidentMaintenanceRequests(@Path("residentId") residentId: String): List<MaintenanceRequestResponse>

    @GET("api/maintenance/accommodation/{accommodationId}")
    suspend fun getManagerMaintenanceRequests(@Path("accommodationId") accommodationId: String): List<MaintenanceRequestResponse>

    @PATCH("api/maintenance/{id}/status")
    suspend fun updateMaintenanceStatus(@Path("id") id: String, @Body request: UpdateMaintenanceStatusRequest): MaintenanceRequestResponse

    @POST("api/visitors")
    suspend fun createVisitorRequest(@Body request: CreateVisitorRequest): VisitorRequestResponse

    @GET("api/visitors/resident/{residentId}")
    suspend fun getResidentVisitorRequests(@Path("residentId") residentId: String): List<VisitorRequestResponse>

    @GET("api/visitors/accommodation/{accommodationId}")
    suspend fun getManagerVisitorRequests(@Path("accommodationId") accommodationId: String): List<VisitorRequestResponse>

    @PATCH("api/visitors/{id}/status")
    suspend fun updateVisitorRequestStatus(@Path("id") id: String, @Body request: UpdateVisitorRequestStatusRequest): VisitorRequestResponse

    @POST("api/inspections")
    suspend fun createInspection(@Body request: CreateInspectionRequest): InspectionResponse

    @GET("api/inspections/resident/{residentId}")
    suspend fun getResidentInspections(@Path("residentId") residentId: String): List<InspectionResponse>

    @GET("api/inspections/accommodation/{accommodationId}")
    suspend fun getManagerInspections(@Path("accommodationId") accommodationId: String): List<InspectionResponse>

    @PATCH("api/inspections/{id}/confirm")
    suspend fun confirmInspection(@Path("id") id: String, @Body request: ConfirmInspectionRequest): InspectionResponse

    @POST("api/notifications/devices")
    suspend fun registerFcmDevice(
        @Header("Authorization") authorization: String,
        @Body request: FcmDeviceRequest
    ): retrofit2.Response<Unit>

    @DELETE("api/notifications/devices")
    suspend fun removeFcmDevice(
        @Header("Authorization") authorization: String,
        @Body request: FcmDeviceRequest
    ): retrofit2.Response<Unit>

    // ---- Manager sign-up and accommodation review ----

    @POST("api/managers/register")
    suspend fun registerManager(@Body request: RegisterManagerRequest): ManagerResponse

    // Throws HttpException with code 404 when the manager has not saved an accommodation yet.
    @GET("api/manager/my-accommodation")
    suspend fun getMyAccommodation(): ManagerAccommodationResponse

    @PUT("api/manager/my-accommodation")
    suspend fun saveMyAccommodation(@Body request: SaveAccommodationRequest): ManagerAccommodationResponse

    @POST("api/manager/my-accommodation/submit")
    suspend fun submitMyAccommodation(): ManagerAccommodationResponse

    // ---- Administrator ----

    @GET("api/admin/dashboard")
    suspend fun getAdminDashboard(): AdminDashboardResponse

    // role: Student, Resident or Manager (or null for all). search matches name or email.
    @GET("api/admin/users")
    suspend fun getAdminUsers(
        @Query("role") role: String?,
        @Query("search") search: String?
    ): List<AdminUserSummary>

    @GET("api/admin/users/{id}")
    suspend fun getAdminUser(@Path("id") id: String): AdminUserDetailResponse

    @POST("api/admin/users/{id}/disable")
    suspend fun disableAdminUser(
        @Path("id") id: String,
        @Body request: AdminReasonRequest
    ): AdminActionResponse

    @POST("api/admin/users/{id}/enable")
    suspend fun enableAdminUser(@Path("id") id: String): AdminActionResponse

    @POST("api/admin/users/{id}/delete")
    suspend fun deleteAdminUser(
        @Path("id") id: String,
        @Body request: AdminReasonRequest
    ): AdminActionResponse

    // status: Draft, Pending, Approved, Rejected or Archived (or null for all).
    @GET("api/admin/listings")
    suspend fun getAdminListings(@Query("status") status: String?): List<AdminListingSummary>

    @GET("api/admin/listings/{id}")
    suspend fun getAdminListing(@Path("id") id: String): AdminListingDetailResponse

    @POST("api/admin/listings/{id}/approve")
    suspend fun approveAdminListing(@Path("id") id: String): AdminActionResponse

    @POST("api/admin/listings/{id}/reject")
    suspend fun rejectAdminListing(
        @Path("id") id: String,
        @Body request: AdminReasonRequest
    ): AdminActionResponse

    @POST("api/admin/listings/{id}/archive")
    suspend fun archiveAdminListing(
        @Path("id") id: String,
        @Body request: AdminReasonRequest
    ): AdminActionResponse

    @POST("api/admin/listings/{id}/unarchive")
    suspend fun unarchiveAdminListing(@Path("id") id: String): AdminActionResponse

    @POST("api/admin/listings/{id}/delete")
    suspend fun deleteAdminListing(
        @Path("id") id: String,
        @Body request: AdminReasonRequest
    ): AdminActionResponse

    // limit: 1 to 200 (the server uses 50 if it is 0 or less).
    @GET("api/admin/audit")
    suspend fun getAdminAudit(@Query("limit") limit: Int): List<AuditRecordResponse>
}