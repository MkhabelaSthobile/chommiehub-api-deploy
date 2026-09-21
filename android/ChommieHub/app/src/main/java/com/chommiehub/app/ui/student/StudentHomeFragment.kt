package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.AccommodationRepository
import com.chommiehub.app.ui.student.adapter.AccommodationAdapter
import kotlinx.coroutines.launch

class StudentHomeFragment : Fragment(R.layout.fragment_student_home) {

    private lateinit var viewModel: StudentHomeViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerAccommodations)
        progressBar = view.findViewById(R.id.progressHome)
        tvError = view.findViewById(R.id.tvHomeError)
        btnRetry = view.findViewById(R.id.btnHomeRetry)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val repository = AccommodationRepository(ApiClient.api)
        viewModel = ViewModelProvider(
            this,
            StudentHomeViewModel.Factory(repository)
        )[StudentHomeViewModel::class.java]

        val etSearch = view.findViewById<android.widget.EditText>(R.id.etSearch)
        val chipPopular = view.findViewById<com.google.android.material.chip.Chip>(R.id.chipPopular)
        val chipNearCampus = view.findViewById<com.google.android.material.chip.Chip>(R.id.chipNearCampus)
        val chipVerified = view.findViewById<com.google.android.material.chip.Chip>(R.id.chipVerified)

        fun currentSelectedFilter(): HomeFilter = when {
            chipNearCampus.isChecked -> HomeFilter.NEAR_CAMPUS
            chipVerified.isChecked -> HomeFilter.VERIFIED
            else -> HomeFilter.ALL
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.applyFilter(s?.toString() ?: "", currentSelectedFilter())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val chipListener = { _: View ->
            viewModel.applyFilter(etSearch.text?.toString() ?: "", currentSelectedFilter())
        }
        chipPopular.setOnClickListener(chipListener)
        chipNearCampus.setOnClickListener(chipListener)
        chipVerified.setOnClickListener(chipListener)

        btnRetry.setOnClickListener { viewModel.loadAccommodations() }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is StudentHomeUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                    btnRetry.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                is StudentHomeUiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                    btnRetry.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = AccommodationAdapter(
                        items = state.accommodations,
                        onViewDetails = { accommodation ->
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.studentContainer,
                                    AccommodationDetailFragment.newInstance(accommodation.id)
                                )
                                .addToBackStack(null)
                                .commit()
                        },
                        onToggleShortlist = { accommodation ->
                            viewLifecycleOwner.lifecycleScope.launch {
                                try {
                                    viewModel.toggleShortlist(accommodation.id)
                                } catch (exception: Exception) {
                                    // Silently ignore for now; a toast/snackbar can be added later.
                                }
                            }
                        }
                    )
                }
                is StudentHomeUiState.Error -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                    btnRetry.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAccommodations()
    }
}