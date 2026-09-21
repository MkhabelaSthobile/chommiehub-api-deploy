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
import com.chommiehub.app.data.model.resident.VisitorRequestResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ResidentServicesRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.FirebaseAuth

class VisitorRequestFragment : Fragment() {

    private lateinit var viewModel: VisitorRequestViewModel

    private lateinit var etVisitorName: EditText
    private lateinit var etVisitorIdentification: EditText
    private lateinit var etArrivalDate: EditText
    private lateinit var etDepartureDate: EditText
    private lateinit var checkSleepover: MaterialCheckBox
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
            R.layout.fragment_visitor_request,
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

    private fun bindViews(view: View) {
        etVisitorName = view.findViewById(R.id.etVisitorName)
        etVisitorIdentification = view.findViewById(R.id.etVisitorIdentification)
        etArrivalDate = view.findViewById(R.id.etArrivalDate)
        etDepartureDate = view.findViewById(R.id.etDepartureDate)
        checkSleepover = view.findViewById(R.id.checkSleepover)
        btnSubmit = view.findViewById(R.id.btnSubmitVisitor)
        progressBar = view.findViewById(R.id.progressVisitor)
        tvError = view.findViewById(R.id.tvVisitorError)
        tvEmpty = view.findViewById(R.id.tvVisitorEmpty)
        requestsContainer = view.findViewById(R.id.visitorRequestsContainer)
    }

    private fun configureViewModel() {
        val repository =
            ResidentServicesRepository(ApiClient.api)

        val factory =
            VisitorRequestViewModel.Factory(repository)

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[VisitorRequestViewModel::class.java]
    }

    private fun configureClickListeners() {
        btnSubmit.setOnClickListener {
            submitRequest()
        }
    }

    private fun loadResident() {
        val studentId =
            FirebaseAuth.getInstance().currentUser?.uid

        if (studentId.isNullOrBlank()) {
            tvError.text =
                "You must be signed in to access visitor services."

            tvError.visibility = View.VISIBLE
            return
        }

        viewModel.loadResident(studentId)
    }

    private fun submitRequest() {
        tvError.visibility = View.GONE

        viewModel.submitRequest(
            visitorName = etVisitorName.text.toString().trim(),
            visitorIdentification = etVisitorIdentification.text.toString().trim(),
            arrivalDateUtc = etArrivalDate.text.toString().trim(),
            departureDateUtc = etDepartureDate.text.toString().trim(),
            isSleepover = checkSleepover.isChecked
        )
    }

    private fun observeState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility =
                if (loading) View.VISIBLE else View.GONE

            btnSubmit.isEnabled = !loading
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

                etVisitorName.text.clear()
                etVisitorIdentification.text.clear()
                etArrivalDate.text.clear()
                etDepartureDate.text.clear()
                checkSleepover.isChecked = false

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
                TextView(requireContext()).apply {
                    text =
                        "${request.visitorName}\n" +
                                "ID: ${request.visitorIdentification}\n" +
                                "Arrival: ${request.arrivalDateUtc}\n" +
                                "Departure: ${request.departureDateUtc}\n" +
                                "Sleepover: ${if (request.isSleepover) "Yes" else "No"}\n" +
                                "Status: ${request.status}"

                    textSize = 15f
                    setPadding(24, 24, 24, 24)
                }

            requestsContainer.addView(requestView)
        }
    }
}