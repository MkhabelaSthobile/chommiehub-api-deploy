package com.chommiehub.app.ui.resident

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.repository.AccommodationRepository
import com.chommiehub.app.data.repository.ResidentServicesRepository
import kotlinx.coroutines.launch

/**
 * Loads the active resident record together with the resident's assigned
 * accommodation and room.
 */
class ResidentHomeViewModel(
    private val residentRepository: ResidentServicesRepository,
    private val accommodationRepository: AccommodationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ResidentHomeVM"
    }

    private val _uiState = MutableLiveData<ResidentHomeUiState>()
    val uiState: LiveData<ResidentHomeUiState> get() = _uiState

    fun load(studentId: String) {
        if (studentId.isBlank()) {
            _uiState.value = ResidentHomeUiState.Error("No signed-in student was found.")
            return
        }

        _uiState.value = ResidentHomeUiState.Loading

        viewModelScope.launch {
            try {
                val resident = residentRepository.getResidentByStudentId(studentId)
                val accommodation = accommodationRepository.getAccommodation(resident.accommodationId)
                val room = accommodationRepository.getRoom(resident.roomId)

                _uiState.value = ResidentHomeUiState.Success(resident, accommodation, room)

                Log.i(TAG, "Resident dashboard loaded for resident ${resident.id}.")
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load resident dashboard.", exception)
                _uiState.value = ResidentHomeUiState.Error(exception.message ?: "Unable to load your residence information.")
            }
        }
    }

    class Factory(
        private val residentRepository: ResidentServicesRepository,
        private val accommodationRepository: AccommodationRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResidentHomeViewModel::class.java)) {
                return ResidentHomeViewModel(residentRepository, accommodationRepository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface ResidentHomeUiState {
    data object Loading : ResidentHomeUiState

    data class Success(
        val resident: ResidentResponse,
        val accommodation: AccommodationResponse,
        val room: RoomResponse
    ) : ResidentHomeUiState

    data class Error(val message: String) : ResidentHomeUiState
}