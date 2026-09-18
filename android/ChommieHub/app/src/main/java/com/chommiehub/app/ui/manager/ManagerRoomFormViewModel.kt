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
class ManagerRoomFormViewModel(private val repository: ManagerRepository) : ViewModel()
{
    companion object {
        private const val TAG = "ManagerRoomFormVM"
    }

    private val _room = MutableLiveData<RoomResponse?>()

    val room: LiveData<RoomResponse?> get() = _room

    private val _message = MutableLiveData<String?>()

    val message: LiveData<String?> get() = _message

    private val _error = MutableLiveData<String?>()

    val error: LiveData<String?> get() = _error

    fun loadRoom(
        roomId: String
    ) {
        viewModelScope.launch {
            try {
                _room.value = repository.getRoom(roomId)
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load room.",
                    exception
                )

                _error.value = exception.message ?: "Unable to load room."
            }
        }
    }

    fun createRoom(
        accommodationId: String,
        request: CreateRoomRequest
    ) {
        viewModelScope.launch {
            try {
                _room.value = repository.createRoom(
                        accommodationId,
                        request
                    )

                _message.value = "Room created successfully."
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to create room.",
                    exception
                )

                _error.value = exception.message
                    ?: "Unable to create room."
            }
        }
    }

    fun updateRoom(
        roomId: String,
        request: UpdateRoomRequest
    ) {
        viewModelScope.launch {
            try {
                _room.value = repository.updateRoom(
                        roomId,
                        request
                    )

                _message.value = "Room updated successfully."
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to update room.",
                    exception
                )

                _error.value = exception.message ?: "Unable to update room."
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    class Factory(private val repository: ManagerRepository) : ViewModelProvider.Factory
    {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return ManagerRoomFormViewModel(
                repository
            ) as T
        }
    }
}