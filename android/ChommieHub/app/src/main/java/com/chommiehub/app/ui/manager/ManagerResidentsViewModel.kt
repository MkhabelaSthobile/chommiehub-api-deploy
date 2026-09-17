package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 * Loads the residents currently belonging to the manager's accommodation.
 */
class ManagerResidentsViewModel(private val repository: ManagerRepository) : ViewModel()
{
    companion object {
        private const val TAG = "ManagerResidentsVM"
    }

    private val _uiState = MutableLiveData<ManagerResidentsUiState>()

    val uiState: LiveData<ManagerResidentsUiState> get() = _uiState

    fun loadResidents(accommodationId: String)
    {
        _uiState.value = ManagerResidentsUiState.Loading

        viewModelScope.launch {
            try {
                val residents = repository.getResidents(accommodationId)

                _uiState.value = ManagerResidentsUiState.Success(residents)

                Log.i(
                    TAG,
                    "Loaded ${residents.size} residents."
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load residents.",
                    exception
                )

                _uiState.value = ManagerResidentsUiState.Error(exception.message ?: "Unable to load residents.")
            }
        }
    }

    class Factory(private val repository: ManagerRepository) : ViewModelProvider.Factory
    {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(ManagerResidentsViewModel::class.java)
            ) {
                return ManagerResidentsViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class.")
        }
    }
}

sealed interface ManagerResidentsUiState {

    data object Loading : ManagerResidentsUiState

    data class Success(val residents: List<ResidentResponse>) : ManagerResidentsUiState

    data class Error(val message: String) : ManagerResidentsUiState
}