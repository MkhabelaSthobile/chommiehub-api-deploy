
package com.chommiehub.app.ui.student

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
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
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class StudentHomeFragment : Fragment(R.layout.fragment_student_home) {

    private lateinit var viewModel: StudentHomeViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var btnRetry: Button
    private lateinit var etSearch: EditText
    private lateinit var spArea: Spinner
    private lateinit var spSort: Spinner
    private lateinit var spPrice: Spinner

    private lateinit var chipAll: Chip
    private lateinit var chipWifi: Chip
    private lateinit var chipLaundry: Chip

    private val priceLimits: List<Double?> = listOf(
        null,
        2000.0,
        3000.0,
        4000.0,
        5000.0,
        7500.0,
        10000.0
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerAccommodations)
        progressBar = view.findViewById(R.id.progressHome)
        tvError = view.findViewById(R.id.tvHomeError)
        tvEmpty = view.findViewById(R.id.tvHomeEmpty)
        btnRetry = view.findViewById(R.id.btnHomeRetry)
        etSearch = view.findViewById(R.id.etSearch)
        spArea = view.findViewById(R.id.spArea)
        spSort = view.findViewById(R.id.spSort)
        spPrice = view.findViewById(R.id.spPrice)

        chipAll = view.findViewById(R.id.chipAll)
        chipWifi = view.findViewById(R.id.chipWifi)
        chipLaundry = view.findViewById(R.id.chipLaundry)

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        val repository = AccommodationRepository(ApiClient.api)

        viewModel = ViewModelProvider(
            this,
            StudentHomeViewModel.Factory(repository)
        )[StudentHomeViewModel::class.java]

        setupSortSpinner()
        setupPriceSpinner()
        setupListeners()
        observeViewModel()

        btnRetry.setOnClickListener {
            viewModel.loadAccommodations()
        }

        view.findViewById<Button>(
            R.id.btnAccommodationMap
        ).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.studentContainer,
                    AccommodationMapFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAccommodations()
    }

    private fun setupSortSpinner() {
        val sortOptions = listOf(
            getString(R.string.sort_name_az),
            getString(R.string.sort_name_za),
            getString(R.string.sort_area_az),
            getString(R.string.sort_most_amenities)
        )

        spSort.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions
        )
    }

    private fun setupPriceSpinner() {
        val priceOptions = listOf(
            getString(R.string.price_any),
            getString(R.string.price_up_to, "2,000"),
            getString(R.string.price_up_to, "3,000"),
            getString(R.string.price_up_to, "4,000"),
            getString(R.string.price_up_to, "5,000"),
            getString(R.string.price_up_to, "7,500"),
            getString(R.string.price_up_to, "10,000")
        )

        spPrice.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            priceOptions
        )
    }

    private fun setupListeners() {
        etSearch.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    text: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    applyCurrentSearch()
                }

                override fun afterTextChanged(
                    editable: Editable?
                ) = Unit
            }
        )

        val filterListener =
            View.OnClickListener { applyCurrentSearch() }

        chipAll.setOnClickListener(filterListener)
        chipWifi.setOnClickListener(filterListener)
        chipLaundry.setOnClickListener(filterListener)

        spArea.onItemSelectedListener =
            SimpleItemSelectedListener {
                applyCurrentSearch()
            }

        spSort.onItemSelectedListener =
            SimpleItemSelectedListener {
                applyCurrentSearch()
            }

        spPrice.onItemSelectedListener =
            SimpleItemSelectedListener {
                applyCurrentSearch()
            }
    }

    private fun observeViewModel() {
        viewModel.availableAreas.observe(
            viewLifecycleOwner
        ) { areas ->
            val previousIndex = spArea.selectedItemPosition

            val displayAreas = areas.map { area ->
                if (area == "All areas") {
                    getString(R.string.all_areas)
                } else {
                    area
                }
            }

            spArea.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                displayAreas
            )

            spArea.setSelection(
                if (previousIndex in displayAreas.indices) {
                    previousIndex
                } else {
                    0
                }
            )
        }

        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                StudentHomeUiState.Loading -> showLoading()

                is StudentHomeUiState.Success ->
                    showResults(state)

                is StudentHomeUiState.Error ->
                    showError(state.message)

                StudentHomeUiState.PriceUnavailable ->
                    showError(
                        getString(R.string.price_unavailable)
                    )
            }
        }
    }

    private fun applyCurrentSearch() {
        val selectedArea =
            if (spArea.selectedItemPosition <= 0) {
                "All areas"
            } else {
                spArea.selectedItem?.toString() ?: "All areas"
            }

        val maxPrice =
            priceLimits.getOrNull(spPrice.selectedItemPosition)

        viewModel.applySearch(
            query = etSearch.text?.toString().orEmpty(),
            area = selectedArea,
            filter = selectedFilter(),
            sort = selectedSort(),
            maxPrice = maxPrice
        )
    }

    private fun selectedFilter(): HomeFilter = when {
        chipWifi.isChecked -> HomeFilter.WIFI
        chipLaundry.isChecked -> HomeFilter.LAUNDRY
        else -> HomeFilter.ALL
    }

    private fun selectedSort(): HomeSort {
        return when (spSort.selectedItemPosition) {
            1 -> HomeSort.NAME_DESCENDING
            2 -> HomeSort.AREA_ASCENDING
            3 -> HomeSort.MOST_AMENITIES
            else -> HomeSort.NAME_ASCENDING
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvError.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        btnRetry.visibility = View.GONE
    }

    private fun showResults(state: StudentHomeUiState.Success) {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
        btnRetry.visibility = View.GONE

        if (state.accommodations.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            return
        }

        tvEmpty.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        recyclerView.adapter = AccommodationAdapter(
            items = state.accommodations,
            onViewDetails = { accommodation ->
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.studentContainer,
                        AccommodationDetailFragment.newInstance(
                            accommodation.id
                        )
                    )
                    .addToBackStack(null)
                    .commit()
            },
            onToggleShortlist = { accommodation ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        viewModel.toggleShortlist(accommodation.id)
                    } catch (_: Exception) {
                        // Existing shortlist behaviour preserved.
                    }
                }
            }
        )
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = message
        btnRetry.visibility = View.VISIBLE
    }
}
