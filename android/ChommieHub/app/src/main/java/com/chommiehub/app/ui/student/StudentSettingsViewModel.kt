package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.student.StudentResponse
import com.chommiehub.app.data.model.student.UpdateStudentSettingsRequest
import com.chommiehub.app.data.remote.ApiErrorMapper
import com.chommiehub.app.data.repository.StudentRepository
import kotlinx.coroutines.launch

/**
 * Loads and persists application preferences for the signed-in Student or Resident.
 *
 * Settings are stored through the ChommieHub REST API so preferences remain available
 * after the application is closed or the user signs in on another device.
 */
class StudentSettingsViewModel(private val repository: StudentRepository) : ViewModel() {

    companion object {
        private const val TAG = "StudentSettingsVM"
    }

    private val _student = MutableLiveData<StudentResponse?>()
    val student: LiveData<StudentResponse?> get() = _student

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> get() = _saved

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun load(studentId: String) {
        if (studentId.isBlank()) {
            _error.value = "No signed-in user was found."
            return
        }

        _loading.value = true

        viewModelScope.launch {
            try {
                val student = repository.getStudent(studentId)

                _student.value = student
                CurrentStudent.update(student)

                Log.i(TAG, "Student settings loaded for ${student.id}.")
            } catch (exception: Exception) {
                _error.value = ApiErrorMapper.message(exception, "Unable to load your settings.")
            } finally {
                _loading.value = false
            }
        }
    }

    fun save(studentId: String, preferredLanguage: String, notificationsEnabled: Boolean) {
        if (studentId.isBlank()) {
            _error.value = "No signed-in user was found."
            return
        }

        if (preferredLanguage.isBlank()) {
            _error.value = "Select a preferred language."
            return
        }

        _loading.value = true
        _saved.value = false

        viewModelScope.launch {
            try {
                val request = UpdateStudentSettingsRequest(
                    preferredLanguage = preferredLanguage,
                    notificationsEnabled = notificationsEnabled
                )

                val updatedStudent = repository.updateSettings(studentId, request)

                _student.value = updatedStudent
                CurrentStudent.update(updatedStudent)
                _saved.value = true

                Log.i(TAG, "Settings saved for student ${updatedStudent.id}.")
            } catch (exception: Exception) {
                _error.value = ApiErrorMapper.message(exception, "Unable to save your settings.")
            } finally {
                _loading.value = false
            }
        }
    }

    fun consumeSaved() {
        _saved.value = false
    }

    fun consumeError() {
        _error.value = null
    }

    class Factory(private val repository: StudentRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudentSettingsViewModel::class.java)) {
                return StudentSettingsViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}