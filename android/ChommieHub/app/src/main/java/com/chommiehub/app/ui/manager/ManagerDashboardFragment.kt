package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.BuildConfig
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository

/**
 * Displays live operational statistics for a residence manager.
 */
class ManagerDashboardFragment : Fragment() {

    companion object {
        private const val TAG = "ManagerDashboard"
    }

    private lateinit var viewModel: ManagerDashboardViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button

    private lateinit var tvApplications: TextView
    private lateinit var tvPending: TextView
    private lateinit var tvApproved: TextView
    private lateinit var tvDeclined: TextView
    private lateinit var tvResidents: TextView
    private lateinit var tvRooms: TextView
    private lateinit var tvAvailableRooms: TextView
    private lateinit var tvUnavailableRooms: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_dashboard,
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

        btnRetry.setOnClickListener {
            loadDashboard()
        }

        view.findViewById<Button>(R.id.btnReviewApplications).setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.managerFragmentContainer,
                    ManagerApplicationsFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        observeDashboard()
    }

    override fun onResume() {
        super.onResume()

        // Refresh totals when returning from manager workflows.
        loadDashboard()
    }

    private fun bindViews(view: View) {
        progressBar = view.findViewById(
            R.id.progressDashboard
        )

        tvError = view.findViewById(
            R.id.tvDashboardError
        )

        btnRetry = view.findViewById(
            R.id.btnDashboardRetry
        )

        tvApplications = view.findViewById(
            R.id.tvTotalApplications
        )

        tvPending = view.findViewById(
            R.id.tvPendingApplications
        )

        tvApproved = view.findViewById(
            R.id.tvApprovedApplications
        )

        tvDeclined = view.findViewById(
            R.id.tvDeclinedApplications
        )

        tvResidents = view.findViewById(
            R.id.tvActiveResidents
        )

        tvRooms = view.findViewById(
            R.id.tvTotalRooms
        )

        tvAvailableRooms = view.findViewById(
            R.id.tvAvailableRooms
        )

        tvUnavailableRooms = view.findViewById(
            R.id.tvUnavailableRooms
        )
    }

    private fun configureViewModel() {
        val repository =
            ManagerRepository(ApiClient.api)

        val factory =
            ManagerDashboardViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerDashboardViewModel::class.java]
    }

    private fun observeDashboard() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                ManagerDashboardUiState.Loading ->
                    showLoading()

                is ManagerDashboardUiState.Success ->
                    showDashboard(state.dashboard)

                is ManagerDashboardUiState.Error ->
                    showError(state.message)
            }
        }
    }

    private fun loadDashboard() {
        val accommodationId =
            BuildConfig.MANAGER_ACCOMMODATION_ID

        Log.d(
            TAG,
            "Loading dashboard for accommodation $accommodationId."
        )

        viewModel.loadDashboard(
            accommodationId
        )
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun showDashboard(
        dashboard: ManagerDashboardResponse
    ) {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE

        tvApplications.text =
            dashboard.totalApplications.toString()

        tvPending.text =
            dashboard.pendingApplications.toString()

        tvApproved.text =
            dashboard.approvedApplications.toString()

        tvDeclined.text =
            dashboard.declinedApplications.toString()

        tvResidents.text =
            dashboard.activeResidents.toString()

        tvRooms.text =
            dashboard.totalRooms.toString()

        tvAvailableRooms.text =
            dashboard.availableRooms.toString()

        tvUnavailableRooms.text =
            dashboard.unavailableRooms.toString()
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE

        tvError.text = message
        tvError.visibility = View.VISIBLE
        btnRetry.visibility = View.VISIBLE
    }
}