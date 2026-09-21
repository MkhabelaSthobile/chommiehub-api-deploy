package com.chommiehub.app.ui.resident

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.resident.CreateVisitorRequest
import com.chommiehub.app.data.model.resident.VisitorRequestResponse
import com.chommiehub.app.data.repository.ResidentServicesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class VisitorRequestViewModel(
    private val repository: ResidentServicesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "VisitorRequestVM"
    }

    private val _resident =
        MutableLiveData<ResidentResponse?>()

    val resident: LiveData<ResidentResponse?>
        get() = _resident

    private val _requests =
        MutableLiveData<List<VisitorRequestResponse>>(emptyList())

    val requests: LiveData<List<VisitorRequestResponse>>
        get() = _requests

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
                _error.value = "Unable to connect to the ChommieHub API."

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
                _error.value = exception.message ?: "Unable to load residence information."

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
                Log.d(TAG, "Loading visitor requests.")

                _requests.value =
                    repository.getVisitorRequests(residentId)

                Log.i(TAG, "Visitor requests loaded successfully.")

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                Log.e(TAG, "Network error while loading visitor requests.", exception)
                _error.value = "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {
                Log.e(TAG, "HTTP error while loading visitor requests.", exception)
                _error.value = "Unable to load visitor requests. Server returned ${exception.code()}."

            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load visitor requests.", exception)
                _error.value = exception.message ?: "Unable to load visitor requests."

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitRequest(
        visitorName: String,
        visitorIdentification: String,
        arrivalDateUtc: String,
        departureDateUtc: String,
        isSleepover: Boolean
    ) {
        val resident = _resident.value

        if (resident == null) {
            _error.value = "Resident information is unavailable."
            return
        }

        if (visitorName.isBlank()) {
            _error.value = "Enter the visitor's name."
            return
        }

        if (visitorIdentification.isBlank()) {
            _error.value = "Enter the visitor's identification."
            return
        }

        if (arrivalDateUtc.isBlank()) {
            _error.value = "Enter the arrival date."
            return
        }

        if (departureDateUtc.isBlank()) {
            _error.value = "Enter the departure date."
            return
        }

        val request = CreateVisitorRequest(
            residentId = resident.id,
            accommodationId = resident.accommodationId,
            visitorName = visitorName,
            visitorIdentification = visitorIdentification,
            arrivalDateUtc = arrivalDateUtc,
            departureDateUtc = departureDateUtc,
            isSleepover = isSleepover
        )

        _isLoading.value = true

        viewModelScope.launch {
            try {
                Log.i(TAG, "Submitting visitor request.")

                repository.createVisitorRequest(request)

                Log.i(TAG, "Visitor request submitted successfully.")

                _message.value = "Visitor request submitted successfully."

                loadRequests(resident.id)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                Log.e(TAG, "Network error while submitting visitor request.", exception)
                _error.value = "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {
                Log.e(TAG, "HTTP error while submitting visitor request.", exception)
                _error.value = "Unable to submit visitor request. Server returned ${exception.code()}."

            } catch (exception: Exception) {
                Log.e(TAG, "Unable to submit visitor request.", exception)
                _error.value = exception.message ?: "Unable to submit visitor request."

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
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VisitorRequestViewModel::class.java)) {
                return VisitorRequestViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}