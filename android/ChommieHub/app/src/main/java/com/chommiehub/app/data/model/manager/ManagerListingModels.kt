package com.chommiehub.app.data.model.manager

/**
 * The manager's own accommodation, including its review status.
 * reviewStatus is one of Draft, Pending, Approved or Rejected.
 */
data class ManagerAccommodationResponse(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val phoneNumber: String,
    val amenities: List<String>,
    val institutionArea: String,
    val isActive: Boolean,
    val reviewStatus: String,
    val reviewReason: String,
    val submittedAtUtc: String?,
    val reviewedAtUtc: String?,
    val isArchived: Boolean,
    val imageUrls: List<String>
)

/**
 * Details a manager saves as a draft before submitting for review.
 */
data class SaveAccommodationRequest(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val phoneNumber: String,
    val amenities: List<String>,
    val institutionArea: String
)