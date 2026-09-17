package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 * Coordinates loading and review actions for a selected accommodation application
 */
class ManagerApplicationDetailViewModel(private val repository: ManagerRepository) : ViewModel() {

    companion object {
        private const val TAG = "ManagerApplicationVM"
    }

    private val _uiState = MutableLiveData<ManagerApplicationDetailUiState>()

    val uiState: LiveData<ManagerApplicationDetailUiState> get() = _uiState

    private val _message = MutableLiveData<String?>()

    val message: LiveData<String?> get() = _message

    fun loadApplication(applicationId: String)
    {
        _uiState.value = ManagerApplicationDetailUiState.Loading

        viewModelScope.launch {
            try {
                val application = repository.getApplication(applicationId)

                _uiState.value = ManagerApplicationDetailUiState.Success(application)

            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load application.",
                    exception
                )

                _uiState.value = ManagerApplicationDetailUiState.Error(exception.message ?: "Unable to load application.")
            }
        }
    }

    /**
     * Sends the manager's final approval or decline decision to the custom REST API
     */
    fun reviewApplication(
        applicationId: String,
        decision: String,
        reviewNotes: String
    ) {
        _uiState.value = ManagerApplicationDetailUiState.Loading

        viewModelScope.launch {
            try {
                val updatedApplication = repository.reviewApplication(
                        applicationId = applicationId,
                        decision = decision,
                        reviewNotes = reviewNotes
                    )

                _uiState.value = ManagerApplicationDetailUiState.Success(updatedApplication)

                _message.value =
                    if (decision == "Approved") {
                        "Application approved."
                    } else {
                        "Application declined."
                    }

                Log.i(
                    TAG,
                    "Application $applicationId reviewed as $decision."
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Application review failed.",
                    exception
                )

                _uiState.value = ManagerApplicationDetailUiState.Error(exception.message ?: "Unable to review application.")
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    class Factory(private val repository: ManagerRepository) : ViewModelProvider.Factory
    {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (
                modelClass.isAssignableFrom(ManagerApplicationDetailViewModel::class.java)
            ) {
                return ManagerApplicationDetailViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class.")
        }
    }
}

sealed interface ManagerApplicationDetailUiState {
    data object Loading : ManagerApplicationDetailUiState
    data class Success(val application: ManagerApplicationDetailResponse) : ManagerApplicationDetailUiState
    data class Error(val message: String) : ManagerApplicationDetailUiState
}