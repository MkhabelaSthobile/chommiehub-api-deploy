package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 * Loads and retains the residence manager's application queue
 */
class ManagerApplicationsViewModel(private val repository: ManagerRepository) : ViewModel() {

    companion object {
        private const val TAG = "ManagerApplicationsVM"
    }

    private val _uiState = MutableLiveData<ManagerApplicationsUiState>()

    val uiState:
            LiveData<ManagerApplicationsUiState>
        get() = _uiState

    fun loadApplications(accommodationId: String
    ) {
        _uiState.value = ManagerApplicationsUiState.Loading

        viewModelScope.launch {
            try {
                val applications = repository.getApplications(accommodationId)

                _uiState.value = ManagerApplicationsUiState.Success(applications)

                Log.i(
                    TAG,
                    "Loaded ${applications.size} manager applications."
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load manager applications.",
                    exception
                )

                _uiState.value = ManagerApplicationsUiState.Error(exception.message ?: "Unable to load applications.")
            }
        }
    }

    class Factory(private val repository: ManagerRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(ManagerApplicationsViewModel::class.java)
            ) {
                return ManagerApplicationsViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class.")
        }
    }
}

sealed interface ManagerApplicationsUiState {

    data object Loading : ManagerApplicationsUiState

    data class Success(val applications: List<ManagerApplicationSummaryResponse>) : ManagerApplicationsUiState

    data class Error(val message: String) : ManagerApplicationsUiState
}