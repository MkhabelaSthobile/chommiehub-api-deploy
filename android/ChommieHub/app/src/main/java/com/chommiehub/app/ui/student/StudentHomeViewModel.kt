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

class StudentHomeViewModel(private val repository: AccommodationRepository) : ViewModel() {

    companion object {
        private const val TAG = "StudentHomeVM"
    }

    private val _uiState = MutableLiveData<StudentHomeUiState>()
    val uiState: LiveData<StudentHomeUiState> get() = _uiState

    private val _availableAreas = MutableLiveData<List<String>>(listOf("All areas"))
    val availableAreas: LiveData<List<String>> get() = _availableAreas

    private var allAccommodations: List<AccommodationResponse> = emptyList()
    private var currentQuery = ""
    private var currentArea = "All areas"
    private var currentFilter = HomeFilter.ALL
    private var currentSort = HomeSort.NAME_ASCENDING

    fun loadAccommodations() {
        _uiState.value = StudentHomeUiState.Loading

        viewModelScope.launch {
            try {
                allAccommodations = repository.getAllAccommodations()

                val areas = allAccommodations
                    .map { it.institutionArea.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sortedBy { it.lowercase() }

                _availableAreas.value = listOf("All areas") + areas

                applySearch(currentQuery, currentArea, currentFilter, currentSort)

                Log.i(TAG, "Loaded ${allAccommodations.size} accommodations.")
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load accommodations.", exception)
                _uiState.value = StudentHomeUiState.Error(exception.message ?: "Unable to load accommodations.")
            }
        }
    }

    fun applySearch(query: String, area: String, filter: HomeFilter, sort: HomeSort) {
        currentQuery = query
        currentArea = area
        currentFilter = filter
        currentSort = sort

        val filtered = AccommodationSearchEngine.apply(
            accommodations = allAccommodations,
            query = query,
            selectedArea = area,
            filter = filter,
            sort = sort
        )

        _uiState.value = StudentHomeUiState.Success(filtered)
    }

    suspend fun toggleShortlist(accommodationId: String) {
        repository.addToShortlist(CurrentStudent.ID, accommodationId)
    }

    class Factory(private val repository: AccommodationRepository) : ViewModelProvider.Factory {

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