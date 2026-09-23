package com.chommiehub.app.data.model.student

/**
 * Complete student profile returned by the ChommieHub API.
 */
data class StudentResponse(
    val id: String,
    val firebaseUid: String,
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val preferredLanguage: String,
    val notificationsEnabled: Boolean = true,
    val userType: String,
    val accommodationId: String,
    val roomId: String,
    val createdAtUtc: String,
    val updatedAtUtc: String
)

data class CreateStudentRequest(
    val firebaseUid: String,
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val preferredLanguage: String
)

data class UpdateStudentRequest(
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val preferredLanguage: String
)

data class UpdateStudentSettingsRequest(
    val preferredLanguage: String,
    val notificationsEnabled: Boolean
)

/**
 * Submits an application for an actual room belonging to an accommodation.
 */
data class SubmitApplicationRequest(
    val accommodationId: String,
    val roomId: String,
    val rentalPeriod: String,
    val supportingInfo: String
)

data class StudentApplicationResponse(
    val id: String,
    val accommodationId: String,
    val accommodationName: String,
    val requestedRoomId: String,
    val roomTypePreference: String,
    val rentalPeriod: String,
    val supportingInfo: String,
    val status: String,
    val assignedRoomId: String,
    val reviewNotes: String,
    val dateSubmittedUtc: String,
    val dateReviewedUtc: String?
)