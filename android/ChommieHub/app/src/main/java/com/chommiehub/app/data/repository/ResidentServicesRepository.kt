package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.resident.CreateMaintenanceRequest
import com.chommiehub.app.data.model.resident.CreateVisitorRequest
import com.chommiehub.app.data.model.resident.MaintenanceRequestResponse
import com.chommiehub.app.data.model.resident.UpdateMaintenanceStatusRequest
import com.chommiehub.app.data.model.resident.UpdateVisitorRequestStatusRequest
import com.chommiehub.app.data.model.resident.VisitorRequestResponse
import com.chommiehub.app.data.remote.ChommieHubApi
import com.chommiehub.app.data.model.manager.ResidentResponse

/**
 * Repository used by resident-service Android features.
 *
 * Keeps maintenance and visitor API access separate from
 * Activities, Fragments and ViewModels.
 */
class ResidentServicesRepository(
    private val api: ChommieHubApi
) {

    suspend fun createMaintenanceRequest(
        request: CreateMaintenanceRequest
    ): MaintenanceRequestResponse {
        return api.createMaintenanceRequest(request)
    }

    suspend fun getMaintenanceRequests(
        residentId: String
    ): List<MaintenanceRequestResponse> {
        return api.getResidentMaintenanceRequests(residentId)
    }

    suspend fun getManagerMaintenanceRequests(
        accommodationId: String
    ): List<MaintenanceRequestResponse> {
        return api.getManagerMaintenanceRequests(accommodationId)
    }

    suspend fun updateMaintenanceStatus(
        requestId: String,
        status: String
    ): MaintenanceRequestResponse {

        val request = UpdateMaintenanceStatusRequest(
            status = status
        )

        return api.updateMaintenanceStatus(
            requestId,
            request
        )
    }

    suspend fun createVisitorRequest(
        request: CreateVisitorRequest
    ): VisitorRequestResponse {
        return api.createVisitorRequest(request)
    }

    suspend fun getVisitorRequests(
        residentId: String
    ): List<VisitorRequestResponse> {
        return api.getResidentVisitorRequests(residentId)
    }

    suspend fun getManagerVisitorRequests(
        accommodationId: String
    ): List<VisitorRequestResponse> {
        return api.getManagerVisitorRequests(accommodationId)
    }

    suspend fun getResidentByStudentId(studentId: String): ResidentResponse {
        return api.getResidentByStudentId(studentId)
    }

    suspend fun updateVisitorRequestStatus(
        requestId: String,
        status: String
    ): VisitorRequestResponse {

        val request = UpdateVisitorRequestStatusRequest(
            status = status
        )

        return api.updateVisitorRequestStatus(
            requestId,
            request
        )
    }
}