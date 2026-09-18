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
 * Request sent when a student updates their own profile. Identity fields
 * (Firebase UID, accommodation, room, user type) are not editable here.
 */
data class UpdateStudentRequest(
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val preferredLanguage: String
)
