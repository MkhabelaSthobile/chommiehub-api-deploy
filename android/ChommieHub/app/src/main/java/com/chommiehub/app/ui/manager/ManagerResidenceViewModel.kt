package com.chommiehub.app.ui.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.UpdateAccommodationRequest
import com.chommiehub.app.data.repository.ManagerRepository
import kotlinx.coroutines.launch

/**
 *Loads and updates the accommodation managed by the current residence manager
 */
class ManagerResidenceViewModel(private val repository: ManagerRepository) : ViewModel()
{
    companion object {
        private const val TAG = "ManagerResidenceVM"
    }

    private val _uiState = MutableLiveData<ManagerResidenceUiState>()

    val uiState: LiveData<ManagerResidenceUiState> get() = _uiState

    private val _message = MutableLiveData<String?>()

    val message: LiveData<String?> get() = _message

    fun loadAccommodation(accommodationId: String)
    {
        _uiState.value = ManagerResidenceUiState.Loading

        viewModelScope.launch {
            try {
                val accommodation = repository.getAccommodation(accommodationId)

                _uiState.value = ManagerResidenceUiState.Success(accommodation)
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to load accommodation.",
                    exception
                )

                _uiState.value = ManagerResidenceUiState.Error(exception.message ?: "Unable to load residence.")
            }
        }
    }

    /**
     * Persists manager-edited accommodation information
     */
    fun updateAccommodation(
        accommodationId: String,
        request: UpdateAccommodationRequest
    ) {
        _uiState.value = ManagerResidenceUiState.Loading

        viewModelScope.launch {
            try {
                val accommodation = repository.updateAccommodation(
                        accommodationId,
                        request
                    )

                _uiState.value = ManagerResidenceUiState.Success(accommodation)

                _message.value = "Residence updated successfully."

                Log.i(
                    TAG,
                    "Accommodation $accommodationId updated."
                )
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Unable to update accommodation.",
                    exception
                )

                _uiState.value = ManagerResidenceUiState.Error(exception.message ?: "Unable to update residence."
                    )
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
                modelClass.isAssignableFrom(ManagerResidenceViewModel::class.java)
            ) {
                return ManagerResidenceViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class.")
        }
    }
}

sealed interface ManagerResidenceUiState {

    data object Loading : ManagerResidenceUiState

    data class Success(val accommodation: AccommodationResponse) : ManagerResidenceUiState

    data class Error(val message: String) : ManagerResidenceUiState
}