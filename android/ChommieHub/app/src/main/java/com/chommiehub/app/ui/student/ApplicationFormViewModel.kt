package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.student.StudentApplicationResponse
import com.chommiehub.app.data.model.student.SubmitApplicationRequest
import com.chommiehub.app.data.repository.StudentRepository
import kotlinx.coroutines.launch

/**
 * Loads the accommodations available to apply to and submits the student's application.
 */
class ApplicationFormViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ApplicationFormVM"
    }

    private val _accommodations = MutableLiveData<List<AccommodationResponse>>(emptyList())
    val accommodations: LiveData<List<AccommodationResponse>>
        get() = _accommodations

    private val _loadError = MutableLiveData<String?>()
    val loadError: LiveData<String?>
        get() = _loadError

    private val _submitResult = MutableLiveData<StudentApplicationResponse?>()
    val submitResult: LiveData<StudentApplicationResponse?>
        get() = _submitResult

    private val _submitError = MutableLiveData<String?>()
    val submitError: LiveData<String?>
        get() = _submitError

    private val _isSubmitting = MutableLiveData(false)
    val isSubmitting: LiveData<Boolean>
        get() = _isSubmitting

    fun loadAccommodations() {
        viewModelScope.launch {
            try {
                _accommodations.value = repository.getAccommodations()
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load accommodations.", exception)
                _loadError.value = exception.message ?: "Unable to load residences."
            }
        }
    }

    fun submit(studentId: String, request: SubmitApplicationRequest) {
        _isSubmitting.value = true

        viewModelScope.launch {
            try {
                val application = repository.submitApplication(studentId, request)
                _submitResult.value = application
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to submit application.", exception)
                _submitError.value = exception.message ?: "Unable to submit your application."
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun consumeSubmitResult() {
        _submitResult.value = null
    }

    fun consumeSubmitError() {
        _submitError.value = null
    }

    class Factory(private val repository: StudentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ApplicationFormViewModel::class.java)) {
                return ApplicationFormViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
