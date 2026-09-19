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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class StudentProfileViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StudentProfileVM"
        private const val HTTP_NOT_FOUND = 404
    }

    private val _uiState =
        MutableLiveData<StudentProfileUiState>()

    val uiState: LiveData<StudentProfileUiState>
        get() = _uiState

    private val _saveMessage =
        MutableLiveData<String?>()

    val saveMessage: LiveData<String?>
        get() = _saveMessage

    private val _saveError =
        MutableLiveData<String?>()

    val saveError: LiveData<String?>
        get() = _saveError

    fun loadProfile(studentId: String) {

        if (studentId.isBlank()) {
            _uiState.value =
                StudentProfileUiState.Error(
                    "Student ID is unavailable."
                )
            return
        }

        _uiState.value =
            StudentProfileUiState.Loading

        viewModelScope.launch {

            try {

                val student =
                    repository.getStudent(studentId)

                _uiState.value =
                    StudentProfileUiState.Loaded(
                        student
                    )

            } catch (exception: CancellationException) {

                // Cancellation is part of normal coroutine lifecycle behaviour.
                // Do not display it to the user as an API error.
                throw exception

            } catch (exception: HttpException) {

                when (exception.code()) {

                    HTTP_NOT_FOUND -> {
                        _uiState.value =
                            StudentProfileUiState.NotFound
                    }

                    else -> {
                        Log.e(
                            TAG,
                            "HTTP error while loading profile.",
                            exception
                        )

                        _uiState.value =
                            StudentProfileUiState.Error(
                                "Unable to load your profile. " +
                                        "Server returned ${exception.code()}."
                            )
                    }
                }

            } catch (exception: IOException) {

                Log.e(
                    TAG,
                    "Network error while loading profile.",
                    exception
                )

                _uiState.value =
                    StudentProfileUiState.Error(
                        "Unable to connect to the ChommieHub API. " +
                                "Make sure the API is running."
                    )

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unexpected error while loading profile.",
                    exception
                )

                _uiState.value =
                    StudentProfileUiState.Error(
                        exception.message
                            ?: "Unable to load your profile."
                    )
            }
        }
    }

    fun createProfile(
        request: CreateStudentRequest
    ) {

        viewModelScope.launch {

            try {

                val student =
                    repository.createStudent(request)

                _uiState.value =
                    StudentProfileUiState.Loaded(
                        student
                    )

                _saveMessage.value =
                    "Profile created."

            } catch (exception: CancellationException) {

                throw exception

            } catch (exception: IOException) {

                Log.e(
                    TAG,
                    "Network error while creating profile.",
                    exception
                )

                _saveError.value =
                    "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {

                Log.e(
                    TAG,
                    "HTTP error while creating profile.",
                    exception
                )

                _saveError.value =
                    "Unable to create profile. " +
                            "Server returned ${exception.code()}."

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to create student profile.",
                    exception
                )

                _saveError.value =
                    exception.message
                        ?: "Unable to create your profile."
            }
        }
    }

    fun updateProfile(
        studentId: String,
        request: UpdateStudentRequest
    ) {

        viewModelScope.launch {

            try {

                val student =
                    repository.updateStudent(
                        studentId,
                        request
                    )

                _uiState.value =
                    StudentProfileUiState.Loaded(
                        student
                    )

                _saveMessage.value =
                    "Profile updated."

            } catch (exception: CancellationException) {

                throw exception

            } catch (exception: IOException) {

                Log.e(
                    TAG,
                    "Network error while updating profile.",
                    exception
                )

                _saveError.value =
                    "Unable to connect to the ChommieHub API."

            } catch (exception: HttpException) {

                Log.e(
                    TAG,
                    "HTTP error while updating profile.",
                    exception
                )

                _saveError.value =
                    "Unable to update profile. " +
                            "Server returned ${exception.code()}."

            } catch (exception: Exception) {

                Log.e(
                    TAG,
                    "Unable to update student profile.",
                    exception
                )

                _saveError.value =
                    exception.message
                        ?: "Unable to update your profile."
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
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {

            if (
                modelClass.isAssignableFrom(
                    StudentProfileViewModel::class.java
                )
            ) {
                return StudentProfileViewModel(
                    repository
                ) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}

sealed interface StudentProfileUiState {

    data object Loading :
        StudentProfileUiState

    data object NotFound :
        StudentProfileUiState

    data class Loaded(
        val student: StudentResponse
    ) : StudentProfileUiState

    data class Error(
        val message: String
    ) : StudentProfileUiState
}