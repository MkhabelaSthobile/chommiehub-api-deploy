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

class AccommodationDetailViewModel(
    private val repository: AccommodationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AccommodationDetailVM"
    }

    private val _uiState = MutableLiveData<AccommodationDetailUiState>()
    val uiState: LiveData<AccommodationDetailUiState>
        get() = _uiState

    fun loadDetail(accommodationId: String) {
        _uiState.value = AccommodationDetailUiState.Loading

        viewModelScope.launch {
            try {
                val accommodation = repository.getAccommodation(accommodationId)
                val rooms = repository.getRooms(accommodationId)
                _uiState.value = AccommodationDetailUiState.Success(accommodation, rooms)
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load accommodation detail.", exception)
                _uiState.value = AccommodationDetailUiState.Error(
                    exception.message ?: "Unable to load accommodation."
                )
            }
        }
    }

    class Factory(
        private val repository: AccommodationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccommodationDetailViewModel::class.java)) {
                return AccommodationDetailViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface AccommodationDetailUiState {
    data object Loading : AccommodationDetailUiState
    data class Success(
        val accommodation: AccommodationResponse,
        val rooms: List<RoomResponse>
    ) : AccommodationDetailUiState
    data class Error(val message: String) : AccommodationDetailUiState
}