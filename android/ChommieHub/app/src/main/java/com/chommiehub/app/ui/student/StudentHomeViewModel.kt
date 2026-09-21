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

enum class HomeFilter {
    ALL, NEAR_CAMPUS, VERIFIED
}

class StudentHomeViewModel(
    private val repository: AccommodationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StudentHomeVM"
    }

    private val _uiState = MutableLiveData<StudentHomeUiState>()
    val uiState: LiveData<StudentHomeUiState>
        get() = _uiState

    private var allAccommodations: List<AccommodationResponse> = emptyList()
    private var currentQuery: String = ""
    private var currentFilter: HomeFilter = HomeFilter.ALL

    fun loadAccommodations() {
        _uiState.value = StudentHomeUiState.Loading

        viewModelScope.launch {
            try {
                allAccommodations = repository.getAllAccommodations()
                applyFilter(currentQuery, currentFilter)
                Log.i(TAG, "Loaded ${allAccommodations.size} accommodations.")
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load accommodations.", exception)
                _uiState.value = StudentHomeUiState.Error(
                    exception.message ?: "Unable to load accommodations."
                )
            }
        }
    }

    fun applyFilter(query: String, filter: HomeFilter) {
        currentQuery = query
        currentFilter = filter

        val filtered = allAccommodations
            .filter { accommodation ->
                query.isBlank() ||
                        accommodation.name.contains(query, ignoreCase = true) ||
                        accommodation.address.contains(query, ignoreCase = true) ||
                        accommodation.institutionArea.contains(query, ignoreCase = true)
            }
            .filter { accommodation ->
                when (filter) {
                    HomeFilter.ALL -> true
                    HomeFilter.NEAR_CAMPUS -> accommodation.institutionArea.isNotBlank()
                    HomeFilter.VERIFIED -> accommodation.isActive
                }
            }

        _uiState.value = StudentHomeUiState.Success(filtered)
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