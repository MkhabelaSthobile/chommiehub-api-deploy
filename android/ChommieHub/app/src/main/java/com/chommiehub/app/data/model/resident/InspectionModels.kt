package com.chommiehub.app.data.model.resident

/**
 * Condition information captured for one inspected room item.
 */
data class InspectionItemRequest(
    val itemName: String,
    val condition: String,
    val comments: String = "",
    val photoReference: String = ""
)

/**
 * Inspection submitted by a resident.
 */
data class CreateInspectionRequest(
    val residentId: String,
    val studentId: String,
    val accommodationId: String,
    val roomId: String,
    val type: String,
    val items: List<InspectionItemRequest>
)

/**
 * Request used when residence management confirms an inspection.
 */
data class ConfirmInspectionRequest(
    val managerNotes: String
)

/**
 * Room-item information returned by the REST API.
 */
data class InspectionItemResponse(
    val itemName: String,
    val condition: String,
    val comments: String,
    val photoReference: String
)

/**
 * Complete inspection returned from the ChommieHub API.
 */
data class InspectionResponse(
    val id: String,
    val residentId: String,
    val studentId: String,
    val accommodationId: String,
    val roomId: String,
    val type: String,
    val items: List<InspectionItemResponse>,
    val status: String,
    val managerConfirmed: Boolean,
    val managerNotes: String,
    val createdAtUtc: String,
    val confirmedAtUtc: String?
)