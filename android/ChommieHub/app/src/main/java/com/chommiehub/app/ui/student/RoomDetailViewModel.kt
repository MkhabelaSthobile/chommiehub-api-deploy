package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.repository.AccommodationRepository
import kotlinx.coroutines.launch

class RoomDetailViewModel(
    private val repository: AccommodationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "RoomDetailVM"
    }

    private val _uiState = MutableLiveData<RoomDetailUiState>()
    val uiState: LiveData<RoomDetailUiState>
        get() = _uiState

    fun loadRoom(roomId: String) {
        _uiState.value = RoomDetailUiState.Loading

        viewModelScope.launch {
            try {
                val room = repository.getRoom(roomId)
                _uiState.value = RoomDetailUiState.Success(room)
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load room.", exception)
                _uiState.value = RoomDetailUiState.Error(
                    exception.message ?: "Unable to load room."
                )
            }
        }
    }

    class Factory(
        private val repository: AccommodationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoomDetailViewModel::class.java)) {
                return RoomDetailViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface RoomDetailUiState {
    data object Loading : RoomDetailUiState
    data class Success(val room: RoomResponse) : RoomDetailUiState
    data class Error(val message: String) : RoomDetailUiState
}