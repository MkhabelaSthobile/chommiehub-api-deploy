package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.remote.ApiErrorMapper
import com.chommiehub.app.data.repository.AccommodationRepository
import kotlinx.coroutines.launch

/**
 * Loads accommodation locations for the Student map-based discovery feature.
 */
class AccommodationMapViewModel(private val repository: AccommodationRepository) : ViewModel() {

    companion object {
        private const val TAG = "AccommodationMapVM"
    }

    private val _uiState = MutableLiveData<AccommodationMapUiState>()
    val uiState: LiveData<AccommodationMapUiState> get() = _uiState

    /**
     * Retrieves all approved accommodations that can be displayed on the discovery map.
     */
    fun loadAccommodations() {
        _uiState.value = AccommodationMapUiState.Loading

        viewModelScope.launch {
            try {
                val accommodations = repository.getAllAccommodations()
                _uiState.value = AccommodationMapUiState.Success(accommodations)

                Log.i(TAG, "Loaded ${accommodations.size} accommodations for map discovery.")
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load accommodation map data.", exception)
                _uiState.value = AccommodationMapUiState.Error(ApiErrorMapper.message(exception, "Unable to load the accommodation map."))
            }
        }
    }

    class Factory(private val repository: AccommodationRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccommodationMapViewModel::class.java)) {
                return AccommodationMapViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface AccommodationMapUiState {
    data object Loading : AccommodationMapUiState
    data class Success(val accommodations: List<AccommodationResponse>) : AccommodationMapUiState
    data class Error(val message: String) : AccommodationMapUiState
}