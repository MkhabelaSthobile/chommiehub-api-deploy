package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.BuildConfig
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository
import com.chommiehub.app.ui.manager.adapter.ManagerApplicationAdapter

/**
 * Displays applications submitted to the manager's accommodation
 */
class ManagerApplicationsFragment : Fragment() {

    private lateinit var viewModel: ManagerApplicationsViewModel
    private val adapter = ManagerApplicationAdapter { application ->
            openApplication(application.id)
        }
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_applications,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        progressBar = view.findViewById(R.id.progressApplications)

        recyclerView = view.findViewById(R.id.recyclerApplications)

        tvEmpty = view.findViewById(R.id.tvApplicationsEmpty)

        tvError = view.findViewById(R.id.tvApplicationsError)

        btnRetry = view.findViewById(R.id.btnApplicationsRetry)

        recyclerView.layoutManager =LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter

        val repository = ManagerRepository(ApiClient.api)

        viewModel = ViewModelProvider(
                this,
                ManagerApplicationsViewModel.Factory(repository)
        )[ManagerApplicationsViewModel::class.java]

        view.findViewById<Button>(R.id.btnApplicationsBack).setOnClickListener {
            parentFragmentManager
                .popBackStack()
        }

        btnRetry.setOnClickListener {
            loadApplications()
        }

        observeState()
    }

    override fun onResume() {
        super.onResume()
        loadApplications()
    }

    private fun loadApplications() {
        viewModel.loadApplications(
            BuildConfig.MANAGER_ACCOMMODATION_ID
        )
    }

    private fun observeState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                ManagerApplicationsUiState.Loading -> showLoading()

                is ManagerApplicationsUiState.Success -> showApplications(state.applications)

                is ManagerApplicationsUiState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun showApplications(applications: List<com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse>
    ) {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE

        adapter.submitList(applications)

        if (applications.isEmpty()) {
            recyclerView.visibility = View.GONE

            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE

            tvEmpty.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE

        tvError.text = message
        tvError.visibility = View.VISIBLE
        btnRetry.visibility = View.VISIBLE
    }

    /**
     * Opens the selected application for full manager review.
     */
    private fun openApplication(applicationId: String)
    {
        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.managerFragmentContainer,
                ManagerApplicationDetailFragment
                    .newInstance(applicationId)
            )
            .addToBackStack(null)
            .commit()
    }
}