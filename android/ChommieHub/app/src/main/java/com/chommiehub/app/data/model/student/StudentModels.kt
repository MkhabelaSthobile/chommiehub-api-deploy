package com.chommiehub.app.data.model.student

/**
 * Complete student profile information returned by the ChommieHub API.
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

/**
 * Request sent to create a student profile linked to a Firebase UID.
 */
data class CreateStudentRequest(
    val firebaseUid: String,
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val preferredLanguage: String
)

/**
 * Request sent when a student updates their own profile.
 *
 * Identity fields such as Firebase UID, accommodation,
 * room and user type are not editable here.
 */
data class UpdateStudentRequest(
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val preferredLanguage: String
)

/**
 * Request sent when a student updates their settings.
 */
data class UpdateStudentSettingsRequest(
    val preferredLanguage: String,
    val notificationsEnabled: Boolean
)

/**
 * Request sent when a student submits a new
 * accommodation application.
 *
 * StudentId is taken from the URL path using the
 * signed-in Firebase UID and is not sent in this body.
 */
data class SubmitApplicationRequest(
    val accommodationId: String,
    val roomTypePreference: String,
    val rentalPeriod: String,
    val supportingInfo: String
)

/**
 * Represents one of the signed-in student's
 * accommodation applications.
 */
data class StudentApplicationResponse(
    val id: String,
    val accommodationId: String,
    val accommodationName: String,
    val roomTypePreference: String,
    val rentalPeriod: String,
    val supportingInfo: String,
    val status: String,
    val assignedRoomId: String,
    val reviewNotes: String,
    val dateSubmittedUtc: String,
    val dateReviewedUtc: String?
)