package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 * Holds and loads manager dashboard data independently from the Fragment.
 */
class ManagerDashboardViewModel(
    private val repository: ManagerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ManagerDashboardVM"
    }

    private val _uiState =
        MutableLiveData<ManagerDashboardUiState>()

    val uiState: LiveData<ManagerDashboardUiState>
        get() = _uiState

    /**
     * Loads current residence statistics without blocking the UI thread.
     */
    fun loadDashboard(accommodationId: String) {
        _uiState.value = ManagerDashboardUiState.Loading

        viewModelScope.launch {
            try {
                val dashboard =
                    repository.getDashboard(accommodationId)

                _uiState.value =
                    ManagerDashboardUiState.Success(dashboard)

                Log.i(
                    TAG,
                    "Manager dashboard loaded for accommodation $accommodationId."
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load manager dashboard.",
                    exception
                )

                _uiState.value =
                    ManagerDashboardUiState.Error(
                        exception.message
                            ?: "Unable to load residence information."
                    )
            }
        }
    }

    /**
     * Factory supplying the repository dependency to the ViewModel.
     */
    class Factory(
        private val repository: ManagerRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(
                    ManagerDashboardViewModel::class.java
                )
            ) {
                return ManagerDashboardViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}

/**
 * Represents all visual states the manager dashboard can display.
 */
sealed interface ManagerDashboardUiState {

    data object Loading : ManagerDashboardUiState

    data class Success(
        val dashboard: ManagerDashboardResponse
    ) : ManagerDashboardUiState

    data class Error(
        val message: String
    ) : ManagerDashboardUiState
}