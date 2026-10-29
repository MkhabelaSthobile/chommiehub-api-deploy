package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.local.entity.SyncState
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.AccommodationRepository
import com.chommiehub.app.ui.student.adapter.AccommodationAdapter
import androidx.lifecycle.asLiveData
import com.chommiehub.app.data.network.NetworkMonitor

class ShortlistFragment : Fragment(R.layout.fragment_shortlist) {

    private lateinit var viewModel: ShortlistViewModel

    // Latest sync status per residence, kept so a new adapter can use it straight away.
    private var latestStatuses: Map<String, SyncState> = emptyMap()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerShortlist)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressShortlist)
        val tvError = view.findViewById<TextView>(R.id.tvShortlistError)
        val tvEmpty = view.findViewById<TextView>(R.id.tvShortlistEmpty)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Show a banner while the phone has no internet connection.
        val tvOfflineBanner = view.findViewById<TextView>(R.id.tvOfflineBanner)
        NetworkMonitor.getInstance(requireContext()).isOnline.asLiveData()
            .observe(viewLifecycleOwner) { isOnline ->
                tvOfflineBanner.visibility = if (isOnline) View.GONE else View.VISIBLE
            }

        val repository = AccommodationRepository(ApiClient.api)
        viewModel = ViewModelProvider(
            this,
            ShortlistViewModel.Factory(repository)
        )[ShortlistViewModel::class.java]

        viewModel.statuses.observe(viewLifecycleOwner) { statuses ->
            latestStatuses = statuses
            (recyclerView.adapter as? AccommodationAdapter)?.updateStatuses(statuses)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ShortlistUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                is ShortlistUiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE

                    if (state.accommodations.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        val adapter = AccommodationAdapter(
                            items = state.accommodations,
                            onViewDetails = { accommodation ->
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(
                                        R.id.studentContainer,
                                        AccommodationDetailFragment.newInstance(accommodation.id)
                                    )
                                    .addToBackStack(null)
                                    .commit()
                            },
                            onToggleShortlist = { accommodation ->
                                viewModel.removeFromShortlist(accommodation.id)
                            },
                            showAsShortlisted = true
                        )
                        adapter.updateStatuses(latestStatuses)
                        recyclerView.adapter = adapter
                    }
                }
                is ShortlistUiState.Error -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    tvEmpty.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadShortlist()
    }
}