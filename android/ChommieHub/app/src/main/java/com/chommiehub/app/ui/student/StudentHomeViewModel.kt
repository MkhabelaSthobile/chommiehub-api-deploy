package com.chommiehub.app.ui.student

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.repository.AccommodationRepository
import kotlinx.coroutines.launch
import com.chommiehub.app.ui.student.CurrentStudent

class StudentHomeViewModel(
    private val repository: AccommodationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StudentHomeVM"
    }

    private val _uiState = MutableLiveData<StudentHomeUiState>()
    val uiState: LiveData<StudentHomeUiState>
        get() = _uiState

    fun loadAccommodations() {
        _uiState.value = StudentHomeUiState.Loading

        viewModelScope.launch {
            try {
                val accommodations = repository.getAllAccommodations()
                _uiState.value = StudentHomeUiState.Success(accommodations)
                Log.i(TAG, "Loaded ${accommodations.size} accommodations.")
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load accommodations.", exception)
                _uiState.value = StudentHomeUiState.Error(
                    exception.message ?: "Unable to load accommodations."
                )
            }
        }
    }

    suspend fun toggleShortlist(accommodationId: String) {
        repository.addToShortlist(CurrentStudent.ID, accommodationId)
    }

    class Factory(
        private val repository: AccommodationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudentHomeViewModel::class.java)) {
                return StudentHomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

sealed interface StudentHomeUiState {
    data object Loading : StudentHomeUiState
    data class Success(val accommodations: List<AccommodationResponse>) : StudentHomeUiState
    data class Error(val message: String) : StudentHomeUiState
}