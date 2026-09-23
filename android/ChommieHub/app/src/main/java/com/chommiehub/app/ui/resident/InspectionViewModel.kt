package com.chommiehub.app.ui.resident

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.resident.CreateInspectionRequest
import com.chommiehub.app.data.model.resident.InspectionItemRequest
import com.chommiehub.app.data.model.resident.InspectionResponse
import com.chommiehub.app.data.repository.ResidentServicesRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Coordinates the resident digital-inspection workflow.
 *
 * The ViewModel resolves the signed-in student's active resident record,
 * loads previous inspections and submits new move-in or move-out inspections
 * through the ChommieHub repository layer.
 */
class InspectionViewModel(private val repository: ResidentServicesRepository) : ViewModel() {

    companion object {
        private const val TAG = "InspectionViewModel"
    }

    private val _resident = MutableLiveData<ResidentResponse?>()
    val resident: LiveData<ResidentResponse?> get() = _resident

    private val _inspections = MutableLiveData<List<InspectionResponse>>(emptyList())
    val inspections: LiveData<List<InspectionResponse>> get() = _inspections

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    /**
     * Resolves the resident associated with the signed-in Student account and
     * loads that resident's previous move-in and move-out inspections.
     */
    fun loadResident(studentId: String) {
        if (studentId.isBlank()) {
            _error.value = "Student ID is required."
            return
        }

        _loading.value = true

        viewModelScope.launch {
            try {
                val resident = repository.getResidentByStudentId(studentId)

                _resident.value = resident
                _inspections.value = repository.getResidentInspections(resident.id)

                Log.i(TAG, "Resident inspection information loaded for resident ${resident.id}.")
            } catch (exception: HttpException) {
                Log.e(TAG, "Server error while loading resident inspection information.", exception)

                _error.value = if (exception.code() == 404) {
                    "Inspections become available after your application is approved and a room is assigned."
                } else {
                    "Unable to load inspection information. Server returned ${exception.code()}."
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load inspection information.", exception)
                _error.value = exception.message ?: "Unable to load inspection information."
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Submits a completed resident inspection to the ChommieHub REST API.
     */
    fun submit(type: String, items: List<InspectionItemRequest>) {
        val currentResident = _resident.value

        if (currentResident == null) {
            _error.value = "A room assignment is required before an inspection can be submitted."
            return
        }

        if (items.isEmpty()) {
            _error.value = "Add at least one inspection item."
            return
        }

        _loading.value = true

        viewModelScope.launch {
            try {
                val request = CreateInspectionRequest(
                    residentId = currentResident.id,
                    studentId = currentResident.studentId,
                    accommodationId = currentResident.accommodationId,
                    roomId = currentResident.roomId,
                    type = type,
                    items = items
                )

                repository.createInspection(request)

                _inspections.value = repository.getResidentInspections(currentResident.id)
                _message.value = "$type inspection submitted successfully."

                Log.i(TAG, "$type inspection submitted for resident ${currentResident.id}.")
            } catch (exception: HttpException) {
                Log.e(TAG, "Server rejected the inspection submission.", exception)
                _error.value = "Unable to submit inspection. Server returned ${exception.code()}."
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to submit inspection.", exception)
                _error.value = exception.message ?: "Unable to submit inspection."
            } finally {
                _loading.value = false
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeError() {
        _error.value = null
    }

    /**
     * Factory used to provide the repository dependency to the ViewModel.
     */
    class Factory(private val repository: ResidentServicesRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InspectionViewModel::class.java)) {
                return InspectionViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}