package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Lets the signed-in student change their preferred language and whether they receive
 * notifications, consistent with the Part 1 settings specification.
 */
class StudentSettingsFragment : Fragment(R.layout.fragment_student_settings) {

    private lateinit var viewModel: StudentSettingsViewModel
    private lateinit var spLanguage: Spinner
    private lateinit var swNotifications: SwitchMaterial
    private lateinit var btnSaveSettings: Button

    private val languageOptions = listOf("English", "Afrikaans", "isiZulu", "Sesotho")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spLanguage = view.findViewById(R.id.spLanguage)
        swNotifications = view.findViewById(R.id.swNotifications)
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings)

        spLanguage.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languageOptions)

        val repository = StudentRepository(ApiClient.api)
        viewModel = ViewModelProvider(this, StudentSettingsViewModel.Factory(repository))[StudentSettingsViewModel::class.java]

        observeViewModel()

        btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        if (CurrentStudent.ID.isBlank()) {
            toast("No signed-in user was found.")
            btnSaveSettings.isEnabled = false
        } else {
            viewModel.load(CurrentStudent.ID)
        }
    }

    /**
     * Observes the current settings, loading state and save result returned by the ViewModel.
     */
    private fun observeViewModel() {
        viewModel.student.observe(viewLifecycleOwner) { student ->
            if (student != null) {
                val languageIndex = languageOptions.indexOfFirst { language ->
                    language.equals(student.preferredLanguage, ignoreCase = true)
                }

                spLanguage.setSelection(if (languageIndex >= 0) languageIndex else 0)
                swNotifications.isChecked = student.notificationsEnabled
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            btnSaveSettings.isEnabled = !loading
        }


        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                val selectedLanguage = languageOptions.getOrNull(
                    spLanguage.selectedItemPosition
                ) ?: "English"

                val languageCode = when (selectedLanguage) {
                    "Afrikaans" -> "af"
                    "isiZulu" -> "zu"
                    "Sesotho" -> "st"
                    else -> "en"
                }

                viewModel.consumeSaved()

                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageCode)
                )

                toast("Settings saved successfully.")
            }
        }


        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                toast(message)
                viewModel.consumeError()
            }
        }
    }

    /**
     * Saves the currently selected preferences through the ChommieHub REST API.
     */
    private fun saveSettings() {
        if (CurrentStudent.ID.isBlank()) {
            toast("No signed-in user was found.")
            return
        }

        val preferredLanguage = languageOptions.getOrNull(spLanguage.selectedItemPosition) ?: "English"

        viewModel.save(
            studentId = CurrentStudent.ID,
            preferredLanguage = preferredLanguage,
            notificationsEnabled = swNotifications.isChecked
        )
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}