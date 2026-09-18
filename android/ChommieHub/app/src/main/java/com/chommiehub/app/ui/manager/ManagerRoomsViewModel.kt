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
 * Loads the complete room inventory for manager maintenance
 */
class ManagerRoomsViewModel(private val repository: ManagerRepository) : ViewModel()
{
    companion object {
        private const val TAG = "ManagerRoomsVM"
    }

    private val _uiState = MutableLiveData<ManagerRoomsUiState>()

    val uiState: LiveData<ManagerRoomsUiState> get() = _uiState

    fun loadRooms(accommodationId: String)
    {
        _uiState.value = ManagerRoomsUiState.Loading

        viewModelScope.launch {
            try {
                val rooms = repository.getRooms(accommodationId)

                _uiState.value = ManagerRoomsUiState.Success(rooms)
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load room inventory.",
                    exception
                )

                _uiState.value = ManagerRoomsUiState.Error(exception.message ?: "Unable to load rooms.")
            }
        }
    }

    class Factory(private val repository: ManagerRepository) : ViewModelProvider.Factory
    {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return ManagerRoomsViewModel(
                repository
            ) as T
        }
    }
}

sealed interface ManagerRoomsUiState {

    data object Loading : ManagerRoomsUiState

    data class Success(val rooms: List<RoomResponse>) : ManagerRoomsUiState

    data class Error(val message: String) : ManagerRoomsUiState
}