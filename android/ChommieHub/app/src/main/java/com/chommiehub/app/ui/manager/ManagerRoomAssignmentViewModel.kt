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
 * Coordinates available-room retrieval
 * and room assignment.
 */
class ManagerRoomAssignmentViewModel(
    private val repository: ManagerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ManagerRoomAssignment"
    }

    private val _uiState =
        MutableLiveData<RoomAssignmentUiState>()

    val uiState: LiveData<RoomAssignmentUiState>
        get() = _uiState

    /**
     * Loads all currently available rooms
     * for the selected accommodation.
     */
    fun loadAvailableRooms(
        accommodationId: String
    ) {
        if (accommodationId.isBlank()) {
            _uiState.value =
                RoomAssignmentUiState.Error(
                    "Accommodation ID is required."
                )

            Log.e(
                TAG,
                "Unable to load available rooms because the accommodation ID is empty."
            )

            return
        }

        _uiState.value =
            RoomAssignmentUiState.Loading

        viewModelScope.launch {
            try {
                val rooms =
                    repository.getAvailableRooms(
                        accommodationId
                    )

                _uiState.value =
                    RoomAssignmentUiState.Rooms(
                        rooms
                    )

                Log.i(
                    TAG,
                    "Loaded ${rooms.size} available rooms for accommodation $accommodationId."
                )

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to load available rooms.",
                    exception
                )

                val errorMessage =
                    exception.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unable to load available rooms."

                _uiState.value =
                    RoomAssignmentUiState.Error(
                        errorMessage
                    )
            }
        }
    }

    /**
     * Assigns the selected room to an
     * approved accommodation application.
     */
    fun assignRoom(
        applicationId: String,
        roomId: String
    ) {
        if (applicationId.isBlank()) {
            _uiState.value =
                RoomAssignmentUiState.Error(
                    "Application ID is required."
                )

            Log.e(
                TAG,
                "Unable to assign room because the application ID is empty."
            )

            return
        }

        if (roomId.isBlank()) {
            _uiState.value =
                RoomAssignmentUiState.Error(
                    "Room ID is required."
                )

            Log.e(
                TAG,
                "Unable to assign room because the room ID is empty."
            )

            return
        }

        _uiState.value =
            RoomAssignmentUiState.Loading

        viewModelScope.launch {
            try {
                val resident =
                    repository.assignRoom(
                        applicationId,
                        roomId
                    )

                _uiState.value =
                    RoomAssignmentUiState.Assigned(
                        resident
                    )

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

                val errorMessage =
                    exception.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unable to assign room."

                _uiState.value =
                    RoomAssignmentUiState.Error(
                        errorMessage
                    )
            }
        }
    }

    /**
     * Factory used to provide ManagerRepository
     * to ManagerRoomAssignmentViewModel.
     */
    class Factory(
        private val repository: ManagerRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {

            if (
                modelClass.isAssignableFrom(
                    ManagerRoomAssignmentViewModel::class.java
                )
            ) {
                return ManagerRoomAssignmentViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}

/**
 * Represents the possible states of
 * the room-assignment screen.
 */
sealed interface RoomAssignmentUiState {

    data object Loading :
        RoomAssignmentUiState

    data class Rooms(
        val rooms: List<RoomResponse>
    ) : RoomAssignmentUiState

    data class Assigned(
        val resident: ResidentResponse
    ) : RoomAssignmentUiState

    data class Error(
        val message: String
    ) : RoomAssignmentUiState
}