
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class StudentHomeViewModel(
    private val repository: AccommodationRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StudentHomeVM"
    }

    private val _uiState = MutableLiveData<StudentHomeUiState>()
    val uiState: LiveData<StudentHomeUiState> get() = _uiState

    private val _availableAreas =
        MutableLiveData<List<String>>(listOf("All areas"))

    val availableAreas: LiveData<List<String>>
        get() = _availableAreas

    private var allAccommodations: List<AccommodationResponse> = emptyList()

    private var roomsByAccommodation:
            Map<String, List<RoomResponse>> = emptyMap()

    private var pricesLoaded = false
    private var pricesUnavailable = false

    private var currentQuery = ""
    private var currentArea = "All areas"
    private var currentFilter = HomeFilter.ALL
    private var currentSort = HomeSort.NAME_ASCENDING
    private var currentMaxPrice: Double? = null

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

                // Reset prices when the residence list is refreshed.
                roomsByAccommodation = emptyMap()
                pricesLoaded = false
                pricesUnavailable = false

                // Show residences immediately when no price limit is selected.
                applyCurrentSearch()

                Log.i(
                    TAG,
                    "Loaded ${allAccommodations.size} accommodations."
                )

                // Retrieve prices without changing the repository or API.
                val semaphore = Semaphore(4)

                val results = coroutineScope {
                    allAccommodations.map { accommodation ->
                        async {
                            semaphore.withPermit {
                                try {
                                    val rooms =
                                        repository.getRooms(accommodation.id)

                                    Triple(
                                        accommodation.id,
                                        rooms,
                                        false
                                    )
                                } catch (exception: CancellationException) {
                                    throw exception
                                } catch (exception: Exception) {
                                    Log.w(
                                        TAG,
                                        "Unable to load rooms for ${accommodation.id}",
                                        exception
                                    )

                                    Triple(
                                        accommodation.id,
                                        emptyList<RoomResponse>(),
                                        true
                                    )
                                }
                            }
                        }
                    }.awaitAll()
                }

                roomsByAccommodation = results.associate {
                    it.first to it.second
                }

                pricesUnavailable = results.any { it.third }
                pricesLoaded = true

                applyCurrentSearch()

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load accommodations.",
                    exception
                )

                _uiState.value = StudentHomeUiState.Error(
                    exception.message ?: "Unable to load accommodations."
                )
            }
        }
    }

    fun applySearch(
        query: String,
        area: String,
        filter: HomeFilter,
        sort: HomeSort,
        maxPrice: Double? = null
    ) {
        currentQuery = query
        currentArea = area
        currentFilter = filter
        currentSort = sort
        currentMaxPrice = maxPrice

        applyCurrentSearch()
    }

    private fun applyCurrentSearch() {
        if (currentMaxPrice != null && !pricesLoaded) {
            _uiState.value = StudentHomeUiState.Loading
            return
        }

        if (currentMaxPrice != null && pricesUnavailable) {
            _uiState.value = StudentHomeUiState.PriceUnavailable
            return
        }

        val filtered = AccommodationSearchEngine.apply(
            accommodations = allAccommodations,
            query = currentQuery,
            selectedArea = currentArea,
            filter = currentFilter,
            sort = currentSort,
            roomsByAccommodation = roomsByAccommodation,
            maxPrice = currentMaxPrice
        )

        _uiState.value = StudentHomeUiState.Success(filtered)
    }

    suspend fun toggleShortlist(accommodationId: String) {
        repository.addToShortlist(
            CurrentStudent.ID,
            accommodationId
        )
    }

    class Factory(
        private val repository: AccommodationRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(
                    StudentHomeViewModel::class.java
                )
            ) {
                return StudentHomeViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}

sealed interface StudentHomeUiState {
    data object Loading : StudentHomeUiState

    data class Success(
        val accommodations: List<AccommodationResponse>
    ) : StudentHomeUiState

    data class Error(
        val message: String
    ) : StudentHomeUiState

    data object PriceUnavailable : StudentHomeUiState
}
