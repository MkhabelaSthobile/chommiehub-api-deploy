package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.model.student.StudentResponse
import com.chommiehub.app.data.model.student.UpdateStudentRequest
import com.chommiehub.app.data.model.student.UpdateStudentSettingsRequest
import com.chommiehub.app.data.model.student.SubmitApplicationRequest
import com.chommiehub.app.data.model.student.StudentApplicationResponse
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.remote.ChommieHubApi

/**
 * Repository used by student-facing Android features
 * Keeping API access here prevents Activities and Fragments from being coupled directly to Retrofit
 */
class StudentRepository(private val api: ChommieHubApi) {

    suspend fun getStudent(id: String): StudentResponse {
        return api.getStudent(id)
    }

    suspend fun createStudent(request: CreateStudentRequest): StudentResponse {
        return api.createStudent(request)
    }

    suspend fun updateStudent(id: String, request: UpdateStudentRequest): StudentResponse {
        return api.updateStudent(id, request)
    }

    suspend fun updateSettings(id: String, request: UpdateStudentSettingsRequest): StudentResponse {
        return api.updateStudentSettings(id, request)
    }

    suspend fun submitApplication(id: String, request: SubmitApplicationRequest): StudentApplicationResponse {
        return api.submitApplication(id, request)
    }

    suspend fun applications(id: String): List<StudentApplicationResponse> {
        return api.getStudentApplications(id)
    }

    suspend fun getAccommodations(): List<AccommodationResponse> {
        return api.getAllAccommodations()
    }
}
