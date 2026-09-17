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
import com.chommiehub.app.ui.manager.adapter.ManagerResidentAdapter

/**
 *Displays residents currently assigned to the manager's accommodation
 */
class ManagerResidentsFragment : Fragment()
{
    private lateinit var viewModel: ManagerResidentsViewModel

    private val adapter = ManagerResidentAdapter()

    private lateinit var progressBar: ProgressBar

    private lateinit var recyclerView: RecyclerView

    private lateinit var tvMessage: TextView

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        progressBar = view.findViewById(R.id.progressResidents)

        recyclerView = view.findViewById(R.id.recyclerResidents)

        tvMessage = view.findViewById(R.id.tvResidentsMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter

        val repository = ManagerRepository(ApiClient.api)

        viewModel = ViewModelProvider(
                this,
                ManagerResidentsViewModel.Factory(repository)
            )[ManagerResidentsViewModel::class.java]

        view.findViewById<Button>(R.id.btnResidentsBack).setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        observeState()

        viewModel.loadResidents(BuildConfig.MANAGER_ACCOMMODATION_ID)
    }

    private fun observeState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                ManagerResidentsUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE

                    tvMessage.visibility = View.GONE
                }

                is ManagerResidentsUiState.Success -> {
                    progressBar.visibility = View.GONE

                    adapter.submitList(state.residents)

                    if (
                        state.residents
                            .isEmpty()
                    ) {
                        recyclerView.visibility = View.GONE

                        tvMessage.text = "There are currently no residents."

                        tvMessage.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE

                        tvMessage.visibility = View.GONE
                    }
                }

                is ManagerResidentsUiState.Error -> {
                    progressBar.visibility = View.GONE

                    recyclerView.visibility = View.GONE

                    tvMessage.text = state.message

                    tvMessage.visibility = View.VISIBLE
                }
            }
        }
    }
}