package com.chommiehub.app.ui.resident

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.resident.CreateMaintenanceRequest
import com.chommiehub.app.data.model.resident.MaintenanceRequestResponse
import com.chommiehub.app.data.repository.ResidentServicesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.chommiehub.app.data.model.manager.ResidentResponse

class MaintenanceViewModel(
    private val repository: ResidentServicesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MaintenanceVM"
    }

    private val _requests =
        MutableLiveData<List<MaintenanceRequestResponse>>(emptyList())

    val requests: LiveData<List<MaintenanceRequestResponse>>
        get() = _requests

    private val _resident =
        MutableLiveData<ResidentResponse?>()

    val resident: LiveData<ResidentResponse?>
        get() = _resident

    private val _isLoading =
        MutableLiveData(false)

    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _message =
        MutableLiveData<String?>()

    val message: LiveData<String?>
        get() = _message

    private val _error =
        MutableLiveData<String?>()

    val error: LiveData<String?>
        get() = _error

    fun loadResident(studentId: String) {

        if (studentId.isBlank()) {
            _error.value = "Student ID is unavailable."
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading resident information.")

                val resident =
                    repository.getResidentByStudentId(studentId)

                _resident.value = resident

                Log.i(TAG, "Resident information loaded successfully.")

                loadRequests(resident.id)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                Log.e(TAG, "Network error while loading resident information.", exception)

                _error.value =
                    "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {
                Log.e(TAG, "HTTP error while loading resident information.", exception)

                _error.value =
                    if (exception.code() == 404) {
                        "No active residence was found for this student."
                    } else {
                        "Unable to load residence information. Server returned ${exception.code()}."
                    }

            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load resident information.", exception)

                _error.value =
                    exception.message
                        ?: "Unable to load residence information."

            } finally {
                _isLoading.value = false
            }
        }
    }
    fun loadRequests(residentId: String) {

        if (residentId.isBlank()) {
            _error.value = "Resident ID is unavailable."
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading maintenance requests.")

                _requests.value =
                    repository.getMaintenanceRequests(residentId)

                Log.i(TAG, "Maintenance requests loaded successfully.")

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                Log.e(TAG, "Network error while loading maintenance requests.", exception)

                _error.value =
                    "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {
                Log.e(TAG, "HTTP error while loading maintenance requests.", exception)

                _error.value =
                    "Unable to load maintenance requests. Server returned ${exception.code()}."

            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load maintenance requests.", exception)

                _error.value =
                    exception.message
                        ?: "Unable to load maintenance requests."

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitRequest(
        category: String,
        description: String
    ) {

        val resident = _resident.value

        if (resident == null) {
            _error.value = "Resident information is unavailable."
            return
        }

        if (category.isBlank()) {
            _error.value = "Select a maintenance category."
            return
        }

        if (description.isBlank()) {
            _error.value = "Enter a description of the problem."
            return
        }

        val request = CreateMaintenanceRequest(
            residentId = resident.id,
            accommodationId = resident.accommodationId,
            roomId = resident.roomId,
            category = category,
            description = description
        )

        _isLoading.value = true

        viewModelScope.launch {
            try {
                Log.i(TAG, "Submitting maintenance request.")

                repository.createMaintenanceRequest(request)

                Log.i(TAG, "Maintenance request submitted successfully.")

                _message.value =
                    "Maintenance request submitted successfully."

                loadRequests(resident.id)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                Log.e(TAG, "Network error while submitting maintenance request.", exception)

                _error.value =
                    "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {
                Log.e(TAG, "HTTP error while submitting maintenance request.", exception)

                _error.value =
                    "Unable to submit request. Server returned ${exception.code()}."

            } catch (exception: Exception) {
                Log.e(TAG, "Unable to submit maintenance request.", exception)

                _error.value =
                    exception.message
                        ?: "Unable to submit maintenance request."

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun consumeError() {
        _error.value = null
    }

    class Factory(
        private val repository: ResidentServicesRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {

            if (modelClass.isAssignableFrom(MaintenanceViewModel::class.java)) {
                return MaintenanceViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}