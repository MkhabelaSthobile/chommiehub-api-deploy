package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.CreateRoomRequest
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.model.manager.UpdateRoomRequest
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 * Handles creation and editing of residence rooms.
 */
class ManagerRoomFormViewModel(
    private val repository: ManagerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ManagerRoomFormVM"
    }

    private val _room =
        MutableLiveData<RoomResponse?>()

    val room: LiveData<RoomResponse?>
        get() = _room

    private val _message =
        MutableLiveData<String?>()

    val message: LiveData<String?>
        get() = _message

    private val _error =
        MutableLiveData<String?>()

    val error: LiveData<String?>
        get() = _error

    /**
     * Loads an existing room for editing.
     */
    fun loadRoom(
        roomId: String
    ) {
        if (roomId.isBlank()) {
            _error.value =
                "Room ID is required."
            return
        }

        clearError()

        viewModelScope.launch {
            try {
                val roomResponse =
                    repository.getRoom(
                        roomId
                    )

                _room.value =
                    roomResponse

                Log.i(
                    TAG,
                    "Room $roomId loaded successfully."
                )
            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to load room.",
                    exception
                )

                _error.value =
                    getErrorMessage(
                        exception,
                        "Unable to load room."
                    )
            }
        }
    }

    /**
     * Creates a new room for an accommodation.
     */
    fun createRoom(
        accommodationId: String,
        request: CreateRoomRequest
    ) {
        if (accommodationId.isBlank()) {
            _error.value =
                "Accommodation ID is required."
            return
        }

        clearError()

        viewModelScope.launch {
            try {
                val createdRoom =
                    repository.createRoom(
                        accommodationId,
                        request
                    )

                _room.value =
                    createdRoom

                _message.value =
                    "Room created successfully."

                Log.i(
                    TAG,
                    "Room created successfully for accommodation $accommodationId."
                )
            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to create room.",
                    exception
                )

                _error.value =
                    getErrorMessage(
                        exception,
                        "Unable to create room."
                    )
            }
        }
    }

    /**
     * Updates an existing room.
     */
    fun updateRoom(
        roomId: String,
        request: UpdateRoomRequest
    ) {
        if (roomId.isBlank()) {
            _error.value =
                "Room ID is required."
            return
        }

        clearError()

        viewModelScope.launch {
            try {
                val updatedRoom =
                    repository.updateRoom(
                        roomId,
                        request
                    )

                _room.value =
                    updatedRoom

                _message.value =
                    "Room updated successfully."

                Log.i(
                    TAG,
                    "Room $roomId updated successfully."
                )
            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to update room.",
                    exception
                )

                _error.value =
                    getErrorMessage(
                        exception,
                        "Unable to update room."
                    )
            }
        }
    }

    /**
     * Clears a success message after it has been displayed.
     */
    fun consumeMessage() {
        _message.value = null
    }

    /**
     * Clears the current error.
     */
    private fun clearError() {
        _error.value = null
    }

    /**
     * Returns a useful error message while providing
     * a fallback when the exception has no message.
     */
    private fun getErrorMessage(
        exception: Exception,
        fallbackMessage: String
    ): String {
        return exception.message
            ?.takeIf { it.isNotBlank() }
            ?: fallbackMessage
    }

    /**
     * Factory used to provide ManagerRepository
     * to ManagerRoomFormViewModel.
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
                    ManagerRoomFormViewModel::class.java
                )
            ) {
                return ManagerRoomFormViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}