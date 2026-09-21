package com.chommiehub.app.data.model.resident

/**
 * Data required when a resident submits a maintenance request.
 */
data class CreateMaintenanceRequest(
    val residentId: String,
    val accommodationId: String,
    val roomId: String,
    val category: String,
    val description: String,
    val photoUrl: String? = null
)

/**
 * Maintenance request returned by the ChommieHub API.
 */
data class MaintenanceRequestResponse(
    val id: String,
    val residentId: String,
    val accommodationId: String,
    val roomId: String,
    val category: String,
    val description: String,
    val photoUrl: String?,
    val status: String,
    val createdAtUtc: String,
    val updatedAtUtc: String?
)

/**
 * Data sent when a manager changes a maintenance request status.
 */
data class UpdateMaintenanceStatusRequest(
    val status: String
)

/**
 * Data required when a resident submits a visitor request.
 */
data class CreateVisitorRequest(
    val residentId: String,
    val accommodationId: String,
    val visitorName: String,
    val visitorIdentification: String,
    val arrivalDateUtc: String,
    val departureDateUtc: String,
    val isSleepover: Boolean
)

/**
 * Visitor request returned by the ChommieHub API.
 */
data class VisitorRequestResponse(
    val id: String,
    val residentId: String,
    val accommodationId: String,
    val visitorName: String,
    val visitorIdentification: String,
    val arrivalDateUtc: String,
    val departureDateUtc: String,
    val isSleepover: Boolean,
    val status: String,
    val createdAtUtc: String,
    val reviewedAtUtc: String?
)

/**
 * Data sent when a manager approves or rejects a visitor request.
 */
data class UpdateVisitorRequestStatusRequest(
    val status: String
)