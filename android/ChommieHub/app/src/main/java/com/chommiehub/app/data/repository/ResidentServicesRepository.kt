package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.resident.ConfirmInspectionRequest
import com.chommiehub.app.data.model.resident.CreateInspectionRequest
import com.chommiehub.app.data.model.resident.CreateMaintenanceRequest
import com.chommiehub.app.data.model.resident.CreateVisitorRequest
import com.chommiehub.app.data.model.resident.InspectionResponse
import com.chommiehub.app.data.model.resident.MaintenanceRequestResponse
import com.chommiehub.app.data.model.resident.UpdateMaintenanceStatusRequest
import com.chommiehub.app.data.model.resident.UpdateVisitorRequestStatusRequest
import com.chommiehub.app.data.model.resident.VisitorRequestResponse
import com.chommiehub.app.data.remote.ChommieHubApi

/**
 * Repository for resident-facing and manager-facing operational services.
 * Android UI classes use this repository instead of calling Retrofit directly.
 */
class ResidentServicesRepository(private val api: ChommieHubApi) {

    suspend fun getResidentByStudentId(studentId: String): ResidentResponse {
        return api.getResidentByStudentId(studentId)
    }

    suspend fun createMaintenanceRequest(request: CreateMaintenanceRequest): MaintenanceRequestResponse {
        return api.createMaintenanceRequest(request)
    }

    suspend fun getMaintenanceRequests(residentId: String): List<MaintenanceRequestResponse> {
        return api.getResidentMaintenanceRequests(residentId)
    }

    suspend fun getManagerMaintenanceRequests(accommodationId: String): List<MaintenanceRequestResponse> {
        return api.getManagerMaintenanceRequests(accommodationId)
    }

    suspend fun updateMaintenanceStatus(requestId: String, status: String): MaintenanceRequestResponse {
        return api.updateMaintenanceStatus(requestId, UpdateMaintenanceStatusRequest(status))
    }

    suspend fun createVisitorRequest(request: CreateVisitorRequest): VisitorRequestResponse {
        return api.createVisitorRequest(request)
    }

    suspend fun getVisitorRequests(residentId: String): List<VisitorRequestResponse> {
        return api.getResidentVisitorRequests(residentId)
    }

    suspend fun getManagerVisitorRequests(accommodationId: String): List<VisitorRequestResponse> {
        return api.getManagerVisitorRequests(accommodationId)
    }

    suspend fun updateVisitorRequestStatus(requestId: String, status: String): VisitorRequestResponse {
        return api.updateVisitorRequestStatus(requestId, UpdateVisitorRequestStatusRequest(status))
    }

    suspend fun createInspection(request: CreateInspectionRequest): InspectionResponse {
        return api.createInspection(request)
    }

    suspend fun getResidentInspections(residentId: String): List<InspectionResponse> {
        return api.getResidentInspections(residentId)
    }

    suspend fun getManagerInspections(accommodationId: String): List<InspectionResponse> {
        return api.getManagerInspections(accommodationId)
    }

    suspend fun confirmInspection(inspectionId: String, managerNotes: String): InspectionResponse {
        return api.confirmInspection(inspectionId, ConfirmInspectionRequest(managerNotes))
    }
}