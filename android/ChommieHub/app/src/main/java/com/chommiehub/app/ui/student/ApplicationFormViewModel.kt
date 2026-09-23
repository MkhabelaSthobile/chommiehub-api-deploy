package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.repository.AccommodationRepository
import kotlinx.coroutines.launch

/**
 * Loads residences and their currently available rooms for the student application form.
 */
class ApplicationFormViewModel(private val repository: AccommodationRepository) : ViewModel() {

    companion object {
        private const val TAG = "ApplicationFormVM"
    }

    private val _accommodations = MutableLiveData<List<AccommodationResponse>>(emptyList())
    val accommodations: LiveData<List<AccommodationResponse>> get() = _accommodations

    private val _rooms = MutableLiveData<List<RoomResponse>>(emptyList())
    val rooms: LiveData<List<RoomResponse>> get() = _rooms

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadAccommodations() {
        viewModelScope.launch {
            try {
                _accommodations.value = repository.getAllAccommodations()
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load accommodations.", exception)
                _error.value = exception.message ?: "Unable to load residences."
            }
        }
    }

    fun loadRooms(accommodationId: String) {
        viewModelScope.launch {
            try {
                _rooms.value = repository.getRooms(accommodationId).filter { it.availability }
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load rooms.", exception)
                _rooms.value = emptyList()
                _error.value = exception.message ?: "Unable to load available rooms."
            }
        }
    }

    class Factory(private val repository: AccommodationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ApplicationFormViewModel::class.java)) {
                return ApplicationFormViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}