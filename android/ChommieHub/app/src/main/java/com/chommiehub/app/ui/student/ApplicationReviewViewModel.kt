package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.student.StudentApplicationResponse
import com.chommiehub.app.data.model.student.SubmitApplicationRequest
import com.chommiehub.app.data.repository.StudentRepository
import kotlinx.coroutines.launch

/**
 * Submits an application only after the student has reviewed all application information.
 */
class ApplicationReviewViewModel(private val repository: StudentRepository) : ViewModel() {

    companion object {
        private const val TAG = "ApplicationReviewVM"
    }

    private val _submitting = MutableLiveData(false)
    val submitting: LiveData<Boolean> get() = _submitting

    private val _result = MutableLiveData<StudentApplicationResponse?>()
    val result: LiveData<StudentApplicationResponse?> get() = _result

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun submit(studentId: String, request: SubmitApplicationRequest) {
        _submitting.value = true

        viewModelScope.launch {
            try {
                _result.value = repository.submitApplication(studentId, request)
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to submit application.", exception)
                _error.value = exception.message ?: "Unable to submit your application."
            } finally {
                _submitting.value = false
            }
        }
    }

    class Factory(private val repository: StudentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ApplicationReviewViewModel::class.java)) {
                return ApplicationReviewViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}