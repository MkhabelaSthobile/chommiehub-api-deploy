package com.chommiehub.app.data.model.manager

/**
 * Aggregated statistics displayed on the residence manager dashboard.
 */
data class ManagerDashboardResponse(
    val accommodationId: String,
    val totalApplications: Int,
    val pendingApplications: Int,
    val approvedApplications: Int,
    val declinedApplications: Int,
    val activeResidents: Int,
    val totalRooms: Int,
    val availableRooms: Int,
    val unavailableRooms: Int
)

/**
 * Summary information displayed in the manager's application list.
 */
data class ManagerApplicationSummaryResponse(
    val id: String,
    val studentId: String,
    val accommodationId: String,
    val roomTypePreference: String,
    val status: String,
    val assignedRoomId: String,
    val dateSubmittedUtc: String
)

/**
 * Complete application information used during manager review.
 */
data class ManagerApplicationDetailResponse(
    val id: String,
    val studentId: String,
    val accommodationId: String,
    val roomTypePreference: String,
    val rentalPeriod: String,
    val supportingInfo: String,
    val status: String,
    val assignedRoomId: String,
    val reviewNotes: String,
    val dateSubmittedUtc: String,
    val dateReviewedUtc: String?
)

/**
 * Request sent when a manager approves or declines an application.
 */
data class ReviewApplicationRequest(
    val decision: String,
    val reviewNotes: String
)

/**
 * Request sent when assigning an available room to an approved applicant.
 */
data class AssignRoomRequest(
    val roomId: String
)

/**
 * Resident information returned after room assignment or when viewing the residence's current occupants.
 */
data class ResidentResponse(
    val id: String,
    val studentId: String,
    val accommodationId: String,
    val roomId: String,
    val applicationId: String,
    val isActive: Boolean,
    val createdAtUtc: String
)