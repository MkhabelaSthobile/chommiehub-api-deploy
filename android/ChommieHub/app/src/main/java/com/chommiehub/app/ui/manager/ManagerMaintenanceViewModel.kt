package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.resident.MaintenanceRequestResponse
import com.chommiehub.app.data.repository.ResidentServicesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ManagerMaintenanceViewModel(
    private val repository: ResidentServicesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ManagerMaintenanceVM"
    }

    private val _requests =
        MutableLiveData<List<MaintenanceRequestResponse>>(emptyList())

    val requests: LiveData<List<MaintenanceRequestResponse>>
        get() = _requests

    private val _isLoading = MutableLiveData(false)

    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _message = MutableLiveData<String?>()

    val message: LiveData<String?>
        get() = _message

    private val _error = MutableLiveData<String?>()

    val error: LiveData<String?>
        get() = _error

    fun loadRequests(accommodationId: String) {
        if (accommodationId.isBlank()) {
            _error.value = "Accommodation ID is unavailable."
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading manager maintenance requests.")

                _requests.value =
                    repository.getManagerMaintenanceRequests(
                        accommodationId
                    )

                Log.i(TAG, "Manager maintenance requests loaded successfully.")

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                Log.e(TAG, "Network error while loading maintenance requests.", exception)
                _error.value = "Unable to connect to the ChommieHub API."
            } catch (exception: HttpException) {
                Log.e(TAG, "HTTP error while loading maintenance requests.", exception)
                _error.value =
                    "Unable to load maintenance requests. Server returned ${exception.code()}."
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load maintenance requests.", exception)
                _error.value =
                    exception.message ?: "Unable to load maintenance requests."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStatus(
        requestId: String,
        status: String,
        accommodationId: String
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                Log.i(TAG, "Updating maintenance request status to $status.")

                repository.updateMaintenanceStatus(
                    requestId,
                    status
                )

                _message.value =
                    "Maintenance request updated to $status."

                loadRequests(accommodationId)

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                Log.e(TAG, "Network error while updating maintenance request.", exception)
                _error.value = "Unable to connect to the ChommieHub API."
            } catch (exception: HttpException) {
                Log.e(TAG, "HTTP error while updating maintenance request.", exception)
                _error.value =
                    "Unable to update maintenance request. Server returned ${exception.code()}."
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to update maintenance request.", exception)
                _error.value =
                    exception.message ?: "Unable to update maintenance request."
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
            if (modelClass.isAssignableFrom(ManagerMaintenanceViewModel::class.java)) {
                return ManagerMaintenanceViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}