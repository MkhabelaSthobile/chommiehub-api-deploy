package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.BuildConfig
import com.chommiehub.app.R
import com.chommiehub.app.data.model.resident.MaintenanceRequestResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ResidentServicesRepository
import com.google.android.material.button.MaterialButton

class ManagerMaintenanceFragment : Fragment() {

    private lateinit var viewModel: ManagerMaintenanceViewModel

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
            R.layout.fragment_manager_maintenance,
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
        observeState()
        loadRequests()
    }

    private fun bindViews(view: View) {
        progressBar =
            view.findViewById(R.id.progressManagerMaintenance)

        tvError =
            view.findViewById(R.id.tvManagerMaintenanceError)

        tvEmpty =
            view.findViewById(R.id.tvManagerMaintenanceEmpty)

        requestsContainer =
            view.findViewById(R.id.managerMaintenanceContainer)
    }

    private fun configureViewModel() {
        val repository =
            ResidentServicesRepository(ApiClient.api)

        val factory =
            ManagerMaintenanceViewModel.Factory(repository)

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerMaintenanceViewModel::class.java]
    }

    private fun loadRequests() {
        viewModel.loadRequests(
            BuildConfig.MANAGER_ACCOMMODATION_ID
        )
    }

    private fun observeState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility =
                if (loading) View.VISIBLE else View.GONE
        }

        viewModel.requests.observe(viewLifecycleOwner) { requests ->
            displayRequests(requests)
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.consumeMessage()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                tvError.text = message
                tvError.visibility = View.VISIBLE
                viewModel.consumeError()
            } else {
                tvError.visibility = View.GONE
            }
        }
    }

    private fun displayRequests(
        requests: List<MaintenanceRequestResponse>
    ) {
        requestsContainer.removeAllViews()

        tvEmpty.visibility =
            if (requests.isEmpty()) View.VISIBLE else View.GONE

        requests.forEach { request ->
            val requestView =
                layoutInflater.inflate(
                    R.layout.item_manager_maintenance,
                    requestsContainer,
                    false
                )

            requestView.findViewById<TextView>(
                R.id.tvManagerMaintenanceDetails
            ).text =
                "${request.category}\n" +
                        "${request.description}\n" +
                        "Room: ${request.roomId}\n" +
                        "Status: ${request.status}"

            requestView.findViewById<MaterialButton>(
                R.id.btnMaintenanceInProgress
            ).setOnClickListener {
                updateStatus(
                    request.id,
                    "InProgress"
                )
            }

            requestView.findViewById<MaterialButton>(
                R.id.btnMaintenanceResolved
            ).setOnClickListener {
                updateStatus(
                    request.id,
                    "Resolved"
                )
            }

            requestsContainer.addView(requestView)
        }
    }

    private fun updateStatus(
        requestId: String,
        status: String
    ) {
        viewModel.updateStatus(
            requestId = requestId,
            status = status,
            accommodationId =
                BuildConfig.MANAGER_ACCOMMODATION_ID
        )
    }
}