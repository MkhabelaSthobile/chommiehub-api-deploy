package com.chommiehub.app.ui.student

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.BuildConfig
import com.chommiehub.app.R
import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.model.student.StudentResponse
import com.chommiehub.app.data.model.student.UpdateStudentRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository
import com.google.android.material.button.MaterialButton

/**
 * Displays the signed-in student's profile, and doubles as the setup form
 * the first time a profile is created for a Firebase account.
 */
class StudentProfileFragment : Fragment() {

    private lateinit var viewModel: StudentProfileViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var formContainer: View
    private lateinit var tvError: TextView
    private lateinit var btnRetry: MaterialButton

    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etLanguage: EditText
    private lateinit var tvFormError: TextView
    private lateinit var btnSave: MaterialButton

    /** True while no profile exists yet and the form is creating one. */
    private var isCreateMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_student_profile,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        configureViewModel()

        btnRetry.setOnClickListener { loadProfile() }
        btnSave.setOnClickListener { saveProfile() }

        observeState()
        loadProfile()
    }

    private fun bindViews(view: View) {
        progressBar = view.findViewById(R.id.progressProfile)
        formContainer = view.findViewById(R.id.profileFormContainer)
        tvError = view.findViewById(R.id.tvProfileError)
        btnRetry = view.findViewById(R.id.btnProfileRetry)

        etName = view.findViewById(R.id.etProfileName)
        etSurname = view.findViewById(R.id.etProfileSurname)
        etEmail = view.findViewById(R.id.etProfileEmail)
        etPhone = view.findViewById(R.id.etProfilePhone)
        etLanguage = view.findViewById(R.id.etProfileLanguage)
        tvFormError = view.findViewById(R.id.tvProfileFormError)
        btnSave = view.findViewById(R.id.btnSaveProfile)
    }

    private fun configureViewModel() {
        val repository = StudentRepository(ApiClient.api)

        viewModel = ViewModelProvider(
            this,
            StudentProfileViewModel.Factory(repository)
        )[StudentProfileViewModel::class.java]
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                StudentProfileUiState.Loading -> showLoading()
                StudentProfileUiState.NotFound -> showCreateForm()
                is StudentProfileUiState.Loaded -> showLoadedProfile(state.student)
                is StudentProfileUiState.Error -> showError(state.message)
            }
        }

        viewModel.saveMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.consumeSaveMessage()
            }
        }

        viewModel.saveError.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                tvFormError.text = message
                tvFormError.visibility = View.VISIBLE
            }
        }
    }

    private fun loadProfile() {
        viewModel.loadProfile(BuildConfig.STUDENT_ID)
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        formContainer.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun showCreateForm() {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
        formContainer.visibility = View.VISIBLE
        tvFormError.visibility = View.GONE

        isCreateMode = true

        etEmail.isEnabled = true
        etName.setText("")
        etSurname.setText("")
        etEmail.setText("")
        etPhone.setText("")
        etLanguage.setText("English")

        btnSave.text = "Create Profile"
    }

    private fun showLoadedProfile(student: StudentResponse) {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
        formContainer.visibility = View.VISIBLE
        tvFormError.visibility = View.GONE

        isCreateMode = false

        etName.setText(student.name)
        etSurname.setText(student.surname)
        etEmail.setText(student.email)
        // Email is tied to the Firebase account and isn't editable here.
        etEmail.isEnabled = false
        etPhone.setText(student.phoneNumber)
        etLanguage.setText(student.preferredLanguage)

        btnSave.text = "Save Profile"
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        formContainer.visibility = View.GONE

        tvError.text = message
        tvError.visibility = View.VISIBLE
        btnRetry.visibility = View.VISIBLE
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val language = etLanguage.text.toString().trim().ifBlank { "English" }

        if (name.isBlank()) {
            showFormError("Enter your first name.")
            return
        }

        if (surname.isBlank()) {
            showFormError("Enter your surname.")
            return
        }

        if (isCreateMode) {
            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showFormError("Enter a valid email address.")
                return
            }

            tvFormError.visibility = View.GONE

            viewModel.createProfile(
                CreateStudentRequest(
                    firebaseUid = BuildConfig.STUDENT_ID,
                    name = name,
                    surname = surname,
                    email = email,
                    phoneNumber = phone,
                    preferredLanguage = language
                )
            )
        } else {
            tvFormError.visibility = View.GONE

            viewModel.updateProfile(
                BuildConfig.STUDENT_ID,
                UpdateStudentRequest(
                    name = name,
                    surname = surname,
                    phoneNumber = phone,
                    preferredLanguage = language
                )
            )
        }
    }

    private fun showFormError(message: String) {
        tvFormError.text = message
        tvFormError.visibility = View.VISIBLE
    }
}
