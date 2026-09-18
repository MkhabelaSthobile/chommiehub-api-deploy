package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.model.student.StudentResponse
import com.chommiehub.app.data.model.student.UpdateStudentRequest
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
}
