package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.model.student.StudentResponse
import com.chommiehub.app.data.model.student.UpdateStudentRequest
import com.chommiehub.app.data.repository.StudentRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Loads a student's profile and handles creating it for the first time
 * or saving edits, independently from the Fragment.
 */
class StudentProfileViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StudentProfileVM"
        private const val HTTP_NOT_FOUND = 404
    }

    private val _uiState = MutableLiveData<StudentProfileUiState>()

    val uiState: LiveData<StudentProfileUiState>
        get() = _uiState

    private val _saveMessage = MutableLiveData<String?>()

    val saveMessage: LiveData<String?>
        get() = _saveMessage

    private val _saveError = MutableLiveData<String?>()

    val saveError: LiveData<String?>
        get() = _saveError

    /**
     * Loads the profile belonging to [studentId]. A 404 response means no
     * profile has been created yet, so the UI can show the setup form
     * instead of an error.
     */
    fun loadProfile(studentId: String) {
        _uiState.value = StudentProfileUiState.Loading

        viewModelScope.launch {
            try {
                val student = repository.getStudent(studentId)

                _uiState.value = StudentProfileUiState.Loaded(student)
            } catch (exception: HttpException) {
                if (exception.code() == HTTP_NOT_FOUND) {
                    _uiState.value = StudentProfileUiState.NotFound
                } else {
                    Log.e(TAG, "Unable to load student profile.", exception)

                    _uiState.value = StudentProfileUiState.Error(
                        exception.message() ?: "Unable to load your profile."
                    )
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load student profile.", exception)

                _uiState.value = StudentProfileUiState.Error(
                    exception.message ?: "Unable to load your profile."
                )
            }
        }
    }

    fun createProfile(request: CreateStudentRequest) {
        viewModelScope.launch {
            try {
                val student = repository.createStudent(request)

                _uiState.value = StudentProfileUiState.Loaded(student)
                _saveMessage.value = "Profile created."
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to create student profile.", exception)

                _saveError.value = exception.message ?: "Unable to create your profile."
            }
        }
    }

    fun updateProfile(studentId: String, request: UpdateStudentRequest) {
        viewModelScope.launch {
            try {
                val student = repository.updateStudent(studentId, request)

                _uiState.value = StudentProfileUiState.Loaded(student)
                _saveMessage.value = "Profile updated."
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to update student profile.", exception)

                _saveError.value = exception.message ?: "Unable to update your profile."
            }
        }
    }

    fun consumeSaveMessage() {
        _saveMessage.value = null
    }

    fun consumeSaveError() {
        _saveError.value = null
    }

    class Factory(
        private val repository: StudentRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudentProfileViewModel::class.java)) {
                return StudentProfileViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/**
 * Represents all visual states the student profile screen can display.
 */
sealed interface StudentProfileUiState {

    data object Loading : StudentProfileUiState

    /** No profile exists yet for this student - show the setup form. */
    data object NotFound : StudentProfileUiState

    data class Loaded(val student: StudentResponse) : StudentProfileUiState

    data class Error(val message: String) : StudentProfileUiState
}
