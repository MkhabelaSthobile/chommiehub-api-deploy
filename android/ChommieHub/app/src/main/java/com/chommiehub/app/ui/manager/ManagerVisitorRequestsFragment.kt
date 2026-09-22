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
import com.chommiehub.app.R
import com.chommiehub.app.data.model.resident.VisitorRequestResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ResidentServicesRepository
import com.google.android.material.button.MaterialButton

class ManagerVisitorRequestsFragment : Fragment() {

    private lateinit var viewModel: ManagerVisitorRequestsViewModel

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
            R.layout.fragment_manager_visitor_requests,
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
            view.findViewById(R.id.progressManagerVisitors)

        tvError =
            view.findViewById(R.id.tvManagerVisitorsError)

        tvEmpty =
            view.findViewById(R.id.tvManagerVisitorsEmpty)

        requestsContainer =
            view.findViewById(R.id.managerVisitorsContainer)
    }

    private fun configureViewModel() {
        val repository =
            ResidentServicesRepository(ApiClient.api)

        val factory =
            ManagerVisitorRequestsViewModel.Factory(repository)

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerVisitorRequestsViewModel::class.java]
    }

    private fun loadRequests() {
        viewModel.loadRequests(
            CurrentManager.ACCOMMODATION_ID
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
        requests: List<VisitorRequestResponse>
    ) {
        requestsContainer.removeAllViews()

        tvEmpty.visibility =
            if (requests.isEmpty()) View.VISIBLE else View.GONE

        requests.forEach { request ->

            val requestView =
                layoutInflater.inflate(
                    R.layout.item_manager_visitor_request,
                    requestsContainer,
                    false
                )

            requestView.findViewById<TextView>(
                R.id.tvManagerVisitorDetails
            ).text =
                "Visitor: ${request.visitorName}\n" +
                        "ID/Passport: ${request.visitorIdentification}\n" +
                        "Arrival: ${request.arrivalDateUtc}\n" +
                        "Departure: ${request.departureDateUtc}\n" +
                        "Sleepover: ${if (request.isSleepover) "Yes" else "No"}\n" +
                        "Status: ${request.status}"

            requestView.findViewById<MaterialButton>(
                R.id.btnApproveVisitor
            ).setOnClickListener {
                updateStatus(
                    request.id,
                    "Approved"
                )
            }

            requestView.findViewById<MaterialButton>(
                R.id.btnRejectVisitor
            ).setOnClickListener {
                updateStatus(
                    request.id,
                    "Rejected"
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
            accommodationId = CurrentManager.ACCOMMODATION_ID
        )
    }
}