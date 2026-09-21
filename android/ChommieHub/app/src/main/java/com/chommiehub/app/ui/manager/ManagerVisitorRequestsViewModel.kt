package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.resident.VisitorRequestResponse
import com.chommiehub.app.data.repository.ResidentServicesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ManagerVisitorRequestsViewModel(
    private val repository: ResidentServicesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ManagerVisitorVM"
    }

    private val _requests =
        MutableLiveData<List<VisitorRequestResponse>>(emptyList())

    val requests: LiveData<List<VisitorRequestResponse>>
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
                Log.d(
                    TAG,
                    "Loading manager visitor requests."
                )

                _requests.value =
                    repository.getManagerVisitorRequests(
                        accommodationId
                    )

                Log.i(
                    TAG,
                    "Manager visitor requests loaded successfully."
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                Log.e(
                    TAG,
                    "Network error while loading visitor requests.",
                    exception
                )

                _error.value =
                    "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {
                Log.e(
                    TAG,
                    "HTTP error while loading visitor requests.",
                    exception
                )

                _error.value =
                    "Unable to load visitor requests. Server returned ${exception.code()}."

            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load visitor requests.",
                    exception
                )

                _error.value =
                    exception.message
                        ?: "Unable to load visitor requests."

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
                Log.i(
                    TAG,
                    "Updating visitor request status to $status."
                )

                repository.updateVisitorRequestStatus(
                    requestId,
                    status
                )

                _message.value =
                    "Visitor request updated to $status."

                loadRequests(accommodationId)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: IOException) {
                Log.e(
                    TAG,
                    "Network error while updating visitor request.",
                    exception
                )

                _error.value =
                    "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {
                Log.e(
                    TAG,
                    "HTTP error while updating visitor request.",
                    exception
                )

                _error.value =
                    "Unable to update visitor request. Server returned ${exception.code()}."

            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to update visitor request.",
                    exception
                )

                _error.value =
                    exception.message
                        ?: "Unable to update visitor request."

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

            if (
                modelClass.isAssignableFrom(
                    ManagerVisitorRequestsViewModel::class.java
                )
            ) {
                return ManagerVisitorRequestsViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}