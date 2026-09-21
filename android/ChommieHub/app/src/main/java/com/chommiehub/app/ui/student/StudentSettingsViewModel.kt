package com.chommiehub.app.ui.student

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chommiehub.app.data.model.student.StudentResponse
import com.chommiehub.app.data.model.student.UpdateStudentSettingsRequest
import com.chommiehub.app.data.repository.StudentRepository
import kotlinx.coroutines.launch

/**
 * Loads the signed-in student's current settings (preferred language, notifications) and
 * saves changes back through the student settings endpoint.
 */
class StudentSettingsViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "StudentSettingsVM"
    }

    private val _student = MutableLiveData<StudentResponse?>()
    val student: LiveData<StudentResponse?>
        get() = _student

    private val _loadError = MutableLiveData<String?>()
    val loadError: LiveData<String?>
        get() = _loadError

    private val _saveMessage = MutableLiveData<String?>()
    val saveMessage: LiveData<String?>
        get() = _saveMessage

    private val _saveError = MutableLiveData<String?>()
    val saveError: LiveData<String?>
        get() = _saveError

    fun loadSettings(studentId: String) {
        viewModelScope.launch {
            try {
                _student.value = repository.getStudent(studentId)
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load settings.", exception)
                _loadError.value = exception.message ?: "Unable to load your settings."
            }
        }
    }

    fun saveSettings(studentId: String, request: UpdateStudentSettingsRequest) {
        viewModelScope.launch {
            try {
                _student.value = repository.updateSettings(studentId, request)
                _saveMessage.value = "Settings saved."
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to save settings.", exception)
                _saveError.value = exception.message ?: "Unable to save your settings."
            }
        }
    }

    fun consumeSaveMessage() {
        _saveMessage.value = null
    }

    fun consumeSaveError() {
        _saveError.value = null
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
