package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 * Coordinates available-room retrieval and transactional room assignment
 */
class ManagerRoomAssignmentViewModel(private val repository: ManagerRepository) : ViewModel()
{
    companion object {
        private const val TAG = "ManagerRoomAssignment"
    }

    private val _uiState = MutableLiveData<RoomAssignmentUiState>()

    val uiState: LiveData<RoomAssignmentUiState> get() = _uiState

    fun loadAvailableRooms(accommodationId: String)
    {
        _uiState.value = RoomAssignmentUiState.Loading

        viewModelScope.launch {
            try {
                val rooms = repository.getAvailableRooms(accommodationId)

                _uiState.value = RoomAssignmentUiState.Rooms(rooms)

            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load available rooms.",
                    exception
                )

                _uiState.value = RoomAssignmentUiState.Error(exception.message ?: "Unable to load available rooms.")
            }
        }
    }

    fun assignRoom(applicationId: String, roomId: String)
    {
        _uiState.value = RoomAssignmentUiState.Loading

        viewModelScope.launch {
            try {
                val resident = repository.assignRoom(
                        applicationId,
                        roomId
                    )

                _uiState.value = RoomAssignmentUiState.Assigned(resident)

                Log.i(
                    TAG,
                    "Room $roomId assigned to application $applicationId."
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Room assignment failed.",
                    exception
                )

                _uiState.value = RoomAssignmentUiState.Error(exception.message ?: "Unable to assign room.")
            }
        }
    }

    class Factory(private val repository: ManagerRepository) : ViewModelProvider.Factory
    {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(ManagerRoomAssignmentViewModel::class.java)
            ) {
                return ManagerRoomAssignmentViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class.")
        }
    }
}

sealed interface RoomAssignmentUiState {

    data object Loading : RoomAssignmentUiState

    data class Rooms(val rooms: List<RoomResponse>) : RoomAssignmentUiState

    data class Assigned(val resident: ResidentResponse) : RoomAssignmentUiState

    data class Error(val message: String) : RoomAssignmentUiState
}