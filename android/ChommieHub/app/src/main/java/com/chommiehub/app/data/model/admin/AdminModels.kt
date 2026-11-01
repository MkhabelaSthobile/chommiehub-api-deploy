package com.chommiehub.app.data.model.admin

import com.chommiehub.app.data.model.manager.ManagerAccommodationResponse

/**
 * One administrator action in the audit log.
 */
data class AuditRecordResponse(
    val id: String,
    val adminId: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val targetName: String,
    val reason: String,
    val createdAtUtc: String
)

/**
 * A student, resident or manager in the administrator's account list.
 * role is Student, Resident or Manager.
 */
data class AdminUserSummary(
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val accommodationId: String,
    val accommodationName: String,
    val isDisabled: Boolean,
    val createdAtUtc: String
)

/**
 * Accommodation in the administrator's listings list.
 */
data class AdminListingSummary(
    val id: String,
    val name: String,
    val address: String,
    val institutionArea: String,
    val reviewStatus: String,
    val isArchived: Boolean,
    val isActive: Boolean,
    val managerId: String,
    val managerName: String,
    val submittedAtUtc: String?,
    val reviewedAtUtc: String?
)

data class AdminApplicationSummary(
    val id: String,
    val accommodationId: String,
    val accommodationName: String,
    val status: String,
    val dateSubmittedUtc: String
)

data class AdminRoomSummary(
    val id: String,
    val roomType: String,
    val price: Double,
    val capacity: Int,
    val availability: Boolean
)

/**
 * Full account details: the account, its accommodation and any applications.
 */
data class AdminUserDetailResponse(
    val user: AdminUserSummary,
    val accommodation: AdminListingSummary?,
    val applications: List<AdminApplicationSummary>
)

/**
 * Full listing details: the accommodation, its manager, residents and rooms.
 */
data class AdminListingDetailResponse(
    val accommodation: ManagerAccommodationResponse,
    val manager: AdminUserSummary?,
    val residents: List<AdminUserSummary>,
    val rooms: List<AdminRoomSummary>,
    val applicationCount: Int
)

/**
 * Counts and recent activity shown on the administrator dashboard.
 */
data class AdminDashboardResponse(
    val students: Int,
    val residents: Int,
    val managers: Int,
    val listingsTotal: Int,
    val listingsDraft: Int,
    val listingsPending: Int,
    val listingsApproved: Int,
    val listingsRejected: Int,
    val listingsArchived: Int,
    val applicationsTotal: Int,
    val applicationsOpen: Int,
    val recentActivity: List<AuditRecordResponse>
)

/**
 * The reason an administrator gives for reject, archive, disable or delete.
 */
data class AdminReasonRequest(
    val reason: String
)

/**
 * The outcome of an administrator action.
 */
data class AdminActionResponse(
    val action: String,
    val targetType: String,
    val targetId: String,
    val message: String
)