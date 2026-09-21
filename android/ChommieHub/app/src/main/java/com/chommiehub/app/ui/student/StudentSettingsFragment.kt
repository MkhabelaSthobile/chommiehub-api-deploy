package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.student.UpdateStudentSettingsRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

/**
 * Lets the signed-in student change their preferred language and whether they receive
 * notifications, consistent with the Part 1 settings specification.
 */
class StudentSettingsFragment : Fragment() {

    private lateinit var viewModel: StudentSettingsViewModel

    private lateinit var spLanguage: Spinner
    private lateinit var swNotifications: SwitchMaterial
    private lateinit var btnSaveSettings: Button

    private val languageOptions = listOf("English", "Afrikaans", "isiZulu", "Sesotho")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_student_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spLanguage = view.findViewById(R.id.spLanguage)
        swNotifications = view.findViewById(R.id.swNotifications)
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings)

        spLanguage.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            languageOptions
        )

        val repository = StudentRepository(ApiClient.api)
        viewModel = ViewModelProvider(
            this,
            StudentSettingsViewModel.Factory(repository)
        )[StudentSettingsViewModel::class.java]

        observeState()

        btnSaveSettings.setOnClickListener { save() }

        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid == null) {
            toast("No signed-in student found.")
        } else {
            viewModel.loadSettings(firebaseUid)
        }
    }

    private fun observeState() {
        viewModel.student.observe(viewLifecycleOwner) { student ->
            if (student != null) {
                val index = languageOptions.indexOf(student.preferredLanguage)
                spLanguage.setSelection(if (index >= 0) index else 0)
                swNotifications.isChecked = student.notificationsEnabled
            }
        }

        viewModel.loadError.observe(viewLifecycleOwner) { message ->
            if (message != null) toast(message)
        }

        viewModel.saveMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                toast(message)
                viewModel.consumeSaveMessage()
            }
        }

        viewModel.saveError.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                toast(message)
                viewModel.consumeSaveError()
            }
        }
    }

    private fun save() {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid == null) {
            toast("No signed-in student found.")
            return
        }

        val language = languageOptions.getOrNull(spLanguage.selectedItemPosition) ?: "English"

        viewModel.saveSettings(
            firebaseUid,
            UpdateStudentSettingsRequest(
                preferredLanguage = language,
                notificationsEnabled = swNotifications.isChecked
            )
        )
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}
