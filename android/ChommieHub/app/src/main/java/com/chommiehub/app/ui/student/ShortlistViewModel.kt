package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.repository.AccommodationRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.asLiveData
import com.chommiehub.app.data.local.entity.SyncState

class ShortlistViewModel(
    private val repository: AccommodationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ShortlistVM"
    }

    private val _uiState = MutableLiveData<ShortlistUiState>()
    val uiState: LiveData<ShortlistUiState>
        get() = _uiState

    // Live sync status for each shortlisted residence.
    val statuses: LiveData<Map<String, SyncState>> =
        repository.observeShortlistStatuses(CurrentStudent.ID).asLiveData()

    fun loadShortlist() {
        _uiState.value = ShortlistUiState.Loading

        viewModelScope.launch {
            try {
                val accommodations = repository.getShortlist(CurrentStudent.ID)
                _uiState.value = ShortlistUiState.Success(accommodations)
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load shortlist.", exception)
                _uiState.value = ShortlistUiState.Error(
                    exception.message ?: "Unable to load shortlist."
                )
            }
        }
    }

    fun removeFromShortlist(accommodationId: String) {
        viewModelScope.launch {
            try {
                repository.removeFromShortlist(CurrentStudent.ID, accommodationId)
                loadShortlist()
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to remove from shortlist.", exception)
            }
        }
    }

    class Factory(
        private val repository: AccommodationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShortlistViewModel::class.java)) {
                return ShortlistViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface ShortlistUiState {
    data object Loading : ShortlistUiState
    data class Success(val accommodations: List<AccommodationResponse>) : ShortlistUiState
    data class Error(val message: String) : ShortlistUiState
}