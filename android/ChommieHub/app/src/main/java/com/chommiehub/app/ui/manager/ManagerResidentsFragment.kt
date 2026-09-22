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
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.ResidentResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository
import com.chommiehub.app.ui.manager.adapter.ManagerResidentAdapter

/**
 * Displays residents currently assigned to the
 * manager's accommodation.
 */
class ManagerResidentsFragment : Fragment() {

    private lateinit var viewModel: ManagerResidentsViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMessage: TextView

    private val residentAdapter by lazy {
        ManagerResidentAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_residents,
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
        configureRecyclerView()
        configureViewModel()
        configureClickListeners(view)
        observeState()
        loadResidents()
    }

    /**
     * Connects the fragment to the views in
     * fragment_manager_residents.xml.
     */
    private fun bindViews(view: View) {
        progressBar = view.findViewById(
            R.id.progressResidents
        )

        recyclerView = view.findViewById(
            R.id.recyclerResidents
        )

        tvMessage = view.findViewById(
            R.id.tvResidentsMessage
        )
    }

    /**
     * Configures the residents RecyclerView.
     */
    private fun configureRecyclerView() {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerView.adapter =
            residentAdapter
    }

    /**
     * Creates the repository and ViewModel.
     */
    private fun configureViewModel() {
        val repository =
            ManagerRepository(ApiClient.api)

        val factory =
            ManagerResidentsViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerResidentsViewModel::class.java]
    }

    /**
     * Configures screen navigation.
     */
    private fun configureClickListeners(view: View) {
        view.findViewById<Button>(
            R.id.btnResidentsBack
        ).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    /**
     * Loads residents belonging to the manager's accommodation.
     */
    private fun loadResidents() {
        val accommodationId = CurrentManager.ACCOMMODATION_ID

        if (accommodationId.isBlank()) {
            showMessage(
                "Manager accommodation could not be identified."
            )
            return
        }

        viewModel.loadResidents(
            accommodationId
        )
    }

    /**
     * Observes resident data returned by the ViewModel.
     */
    private fun observeState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            when (state) {
                ManagerResidentsUiState.Loading -> {
                    showLoading()
                }

                is ManagerResidentsUiState.Success -> {
                    showResidents(
                        state.residents
                    )
                }

                is ManagerResidentsUiState.Error -> {
                    showMessage(
                        state.message
                    )
                }
            }
        }
    }

    /**
     * Displays the loading state.
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvMessage.visibility = View.GONE
    }

    /**
     * Displays residents returned by the API.
     */
    private fun showResidents(
        residents: List<ResidentResponse>
    ) {
        progressBar.visibility = View.GONE

        residentAdapter.submitList(
            residents
        )

        if (residents.isEmpty()) {
            recyclerView.visibility = View.GONE

            tvMessage.text =
                "There are currently no residents."

            tvMessage.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvMessage.visibility = View.GONE
        }
    }

    /**
     * Displays an empty or error message.
     */
    private fun showMessage(
        message: String
    ) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE

        tvMessage.text = message
        tvMessage.visibility = View.VISIBLE
    }
}