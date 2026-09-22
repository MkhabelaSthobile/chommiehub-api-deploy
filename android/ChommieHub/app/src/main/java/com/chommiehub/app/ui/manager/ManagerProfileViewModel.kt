package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.ManagerResponse
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 * Loads the signed-in manager's profile information and
 * their assigned accommodation.
 */
class ManagerProfileViewModel(
    private val repository: ManagerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ManagerProfileVM"
    }

    private val _manager = MutableLiveData<ManagerResponse?>()
    val manager: LiveData<ManagerResponse?>
        get() = _manager

    private val _accommodation = MutableLiveData<AccommodationResponse?>()
    val accommodation: LiveData<AccommodationResponse?>
        get() = _accommodation

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?>
        get() = _error

    fun loadManager(firebaseUid: String) {
        if (firebaseUid.isBlank()) {
            _error.value = "Firebase user ID is unavailable."
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Note: your ChommieHubApi currently has getManagerByFirebaseUid,
                // so we reuse that for the profile lookup.
                val response = com.chommiehub.app.data.remote.ApiClient.api
                    .getManagerByFirebaseUid(firebaseUid)

                _manager.value = response

                // Cache the accommodation ID for other screens.
                if (response.accommodationId.isNotBlank()) {
                    CurrentManager.ID = response.id
                    CurrentManager.ACCOMMODATION_ID = response.accommodationId
                }

                Log.i(TAG, "Manager profile loaded.")
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load manager profile.", exception)
                _error.value = exception.message ?: "Unable to load profile."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAccommodation(accommodationId: String) {
        if (accommodationId.isBlank()) {
            return
        }

        viewModelScope.launch {
            try {
                val response = repository.getAccommodation(accommodationId)
                _accommodation.value = response
                Log.i(TAG, "Accommodation loaded: ${response.name}")
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load accommodation.", exception)
                // Non-fatal: profile can still display without it.
            }
        }
    }

    fun consumeError() {
        _error.value = null
    }

    class Factory(
        private val repository: ManagerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManagerProfileViewModel::class.java)) {
                return ManagerProfileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}