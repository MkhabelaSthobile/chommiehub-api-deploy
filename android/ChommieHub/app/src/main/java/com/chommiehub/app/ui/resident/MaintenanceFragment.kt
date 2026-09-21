package com.chommiehub.app.ui.resident

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.resident.MaintenanceRequestResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ResidentServicesRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class MaintenanceFragment : Fragment() {

    private lateinit var viewModel: MaintenanceViewModel

    private lateinit var etCategory: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var requestsContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_maintenance,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        configureViewModel()
        configureClickListeners()
        observeState()
        loadResident()
    }

    private fun loadResident() {
        val studentId =
            FirebaseAuth.getInstance().currentUser?.uid

        if (studentId.isNullOrBlank()) {
            tvError.text =
                "You must be signed in to access maintenance services."

            tvError.visibility = View.VISIBLE
            return
        }

        viewModel.loadResident(studentId)
    }

    private fun bindViews(view: View) {
        etCategory =
            view.findViewById(R.id.etMaintenanceCategory)

        etDescription =
            view.findViewById(R.id.etMaintenanceDescription)

        btnSubmit =
            view.findViewById(R.id.btnSubmitMaintenance)

        progressBar =
            view.findViewById(R.id.progressMaintenance)

        tvError =
            view.findViewById(R.id.tvMaintenanceError)

        tvEmpty =
            view.findViewById(R.id.tvMaintenanceEmpty)

        requestsContainer =
            view.findViewById(R.id.maintenanceRequestsContainer)
    }

    private fun configureViewModel() {
        val repository =
            ResidentServicesRepository(
                ApiClient.api
            )

        val factory =
            MaintenanceViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[MaintenanceViewModel::class.java]
    }

    private fun configureClickListeners() {
        btnSubmit.setOnClickListener {
            submitRequest()
        }
    }

    private fun observeState() {
        viewModel.isLoading.observe(
            viewLifecycleOwner
        ) { loading ->

            progressBar.visibility =
                if (loading) View.VISIBLE else View.GONE

            btnSubmit.isEnabled = !loading
        }

        viewModel.requests.observe(
            viewLifecycleOwner
        ) { requests ->
            displayRequests(requests)
        }

        viewModel.message.observe(
            viewLifecycleOwner
        ) { message ->

            if (!message.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                etCategory.text.clear()
                etDescription.text.clear()

                viewModel.consumeMessage()
            }
        }

        viewModel.error.observe(
            viewLifecycleOwner
        ) { message ->

            if (!message.isNullOrBlank()) {
                tvError.text = message
                tvError.visibility = View.VISIBLE

                viewModel.consumeError()
            } else {
                tvError.visibility = View.GONE
            }
        }
    }

    private fun submitRequest() {
        tvError.visibility = View.GONE

        val category =
            etCategory.text.toString().trim()

        val description =
            etDescription.text.toString().trim()

        viewModel.submitRequest(
            category = category,
            description = description
        )
    }

    private fun displayRequests(
        requests: List<MaintenanceRequestResponse>
    ) {
        requestsContainer.removeAllViews()

        tvEmpty.visibility =
            if (requests.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        requests.forEach { request ->

            val requestView =
                TextView(requireContext()).apply {

                    text =
                        "${request.category}\n" +
                                "${request.description}\n" +
                                "Status: ${request.status}"

                    textSize = 15f

                    setPadding(
                        24,
                        24,
                        24,
                        24
                    )
                }

            requestsContainer.addView(requestView)
        }
    }
}