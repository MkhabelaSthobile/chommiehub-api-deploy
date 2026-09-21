package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 * Loads the complete room inventory
 * for manager maintenance.
 */
class ManagerRoomsViewModel(
    private val repository: ManagerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ManagerRoomsVM"
    }

    private val _uiState =
        MutableLiveData<ManagerRoomsUiState>()

    val uiState: LiveData<ManagerRoomsUiState>
        get() = _uiState

    /**
     * Loads all rooms belonging to the
     * manager's accommodation.
     */
    fun loadRooms(
        accommodationId: String
    ) {
        if (accommodationId.isBlank()) {
            _uiState.value =
                ManagerRoomsUiState.Error(
                    "Accommodation ID is required."
                )

            Log.e(
                TAG,
                "Unable to load rooms because the accommodation ID is empty."
            )

            return
        }

        _uiState.value =
            ManagerRoomsUiState.Loading

        viewModelScope.launch {
            try {
                val rooms =
                    repository.getRooms(
                        accommodationId
                    )

                _uiState.value =
                    ManagerRoomsUiState.Success(
                        rooms
                    )

                Log.i(
                    TAG,
                    "Loaded ${rooms.size} rooms for accommodation $accommodationId."
                )
            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to load room inventory.",
                    exception
                )

                val errorMessage =
                    exception.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unable to load rooms."

                _uiState.value =
                    ManagerRoomsUiState.Error(
                        errorMessage
                    )
            }
        }
    }

    /**
     * Factory used to provide ManagerRepository
     * to ManagerRoomsViewModel.
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
                    ManagerRoomsViewModel::class.java
                )
            ) {
                return ManagerRoomsViewModel(
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
 * Represents the possible states of the
 * manager rooms screen.
 */
sealed interface ManagerRoomsUiState {

    data object Loading :
        ManagerRoomsUiState

    data class Success(
        val rooms: List<RoomResponse>
    ) : ManagerRoomsUiState

    data class Error(
        val message: String
    ) : ManagerRoomsUiState
}