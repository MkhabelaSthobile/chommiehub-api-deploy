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
 * Coordinates loading and review actions
 * for a selected accommodation application.
 */
class ManagerApplicationDetailViewModel(
    private val repository: ManagerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ManagerApplicationVM"

        private const val DECISION_APPROVED = "Approved"
        private const val DECISION_DECLINED = "Declined"
    }

    private val _uiState =
        MutableLiveData<ManagerApplicationDetailUiState>()

    val uiState: LiveData<ManagerApplicationDetailUiState>
        get() = _uiState

    private val _message =
        MutableLiveData<String?>()

    val message: LiveData<String?>
        get() = _message

    /**
     * Loads the selected application.
     */
    fun loadApplication(
        applicationId: String
    ) {
        if (applicationId.isBlank()) {
            _uiState.value =
                ManagerApplicationDetailUiState.Error(
                    "Application ID is required."
                )

            Log.e(
                TAG,
                "Unable to load application because the application ID is empty."
            )

            return
        }

        _uiState.value =
            ManagerApplicationDetailUiState.Loading

        viewModelScope.launch {
            try {
                val application =
                    repository.getApplication(
                        applicationId
                    )

                _uiState.value =
                    ManagerApplicationDetailUiState.Success(
                        application
                    )

                Log.i(
                    TAG,
                    "Application $applicationId loaded successfully."
                )

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to load application.",
                    exception
                )

                val errorMessage =
                    exception.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unable to load application."

                _uiState.value =
                    ManagerApplicationDetailUiState.Error(
                        errorMessage
                    )
            }
        }
    }

    /**
     * Sends the manager's approval or decline decision
     * to the custom REST API.
     */
    fun reviewApplication(
        applicationId: String,
        decision: String,
        reviewNotes: String
    ) {
        if (applicationId.isBlank()) {
            _uiState.value =
                ManagerApplicationDetailUiState.Error(
                    "Application ID is required."
                )
            return
        }

        val normalisedDecision =
            when {
                decision.equals(
                    DECISION_APPROVED,
                    ignoreCase = true
                ) -> DECISION_APPROVED

                decision.equals(
                    DECISION_DECLINED,
                    ignoreCase = true
                ) -> DECISION_DECLINED

                else -> null
            }

        if (normalisedDecision == null) {
            _uiState.value =
                ManagerApplicationDetailUiState.Error(
                    "Invalid application decision."
                )

            Log.e(
                TAG,
                "Invalid application decision supplied: $decision"
            )

            return
        }

        _uiState.value =
            ManagerApplicationDetailUiState.Loading

        viewModelScope.launch {
            try {
                val updatedApplication =
                    repository.reviewApplication(
                        applicationId = applicationId,
                        decision = normalisedDecision,
                        reviewNotes = reviewNotes.trim()
                    )

                _uiState.value =
                    ManagerApplicationDetailUiState.Success(
                        updatedApplication
                    )

                _message.value =
                    when (normalisedDecision) {
                        DECISION_APPROVED ->
                            "Application approved."

                        DECISION_DECLINED ->
                            "Application declined."

                        else ->
                            "Application updated."
                    }

                Log.i(
                    TAG,
                    "Application $applicationId reviewed as $normalisedDecision."
                )

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Application review failed.",
                    exception
                )

                val errorMessage =
                    exception.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unable to review application."

                _uiState.value =
                    ManagerApplicationDetailUiState.Error(
                        errorMessage
                    )
            }
        }
    }

    /**
     * Clears the message after the Fragment
     * has displayed it.
     */
    fun consumeMessage() {
        _message.value = null
    }

    /**
     * Factory used to provide ManagerRepository
     * to ManagerApplicationDetailViewModel.
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
                    ManagerApplicationDetailViewModel::class.java
                )
            ) {
                return ManagerApplicationDetailViewModel(
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
 * Represents the possible states of the
 * manager application-detail screen.
 */
sealed interface ManagerApplicationDetailUiState {

    data object Loading :
        ManagerApplicationDetailUiState

    data class Success(
        val application: ManagerApplicationDetailResponse
    ) : ManagerApplicationDetailUiState

    data class Error(
        val message: String
    ) : ManagerApplicationDetailUiState
}