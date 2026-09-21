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
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.AccommodationRepository
import com.chommiehub.app.ui.student.adapter.RoomAdapter

class AccommodationDetailFragment : Fragment(R.layout.fragment_accommodation_detail) {

    companion object {
        private const val ARG_ACCOMMODATION_ID = "accommodationId"

        fun newInstance(accommodationId: String): AccommodationDetailFragment {
            val fragment = AccommodationDetailFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_ACCOMMODATION_ID, accommodationId)
            }
            return fragment
        }
    }

    private lateinit var viewModel: AccommodationDetailViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accommodationId = requireArguments().getString(ARG_ACCOMMODATION_ID)
            ?: throw IllegalStateException("AccommodationDetailFragment requires an accommodationId argument.")

        val progressBar = view.findViewById<ProgressBar>(R.id.progressDetail)
        val tvError = view.findViewById<TextView>(R.id.tvDetailError)
        val scrollDetail = view.findViewById<View>(R.id.scrollDetail)
        val textName = view.findViewById<TextView>(R.id.textDetailName)
        val textAddress = view.findViewById<TextView>(R.id.textDetailAddress)
        val textDescription = view.findViewById<TextView>(R.id.textDetailDescription)
        val textAmenities = view.findViewById<TextView>(R.id.textDetailAmenities)
        val recyclerRooms = view.findViewById<RecyclerView>(R.id.recyclerRooms)
        recyclerRooms.layoutManager = LinearLayoutManager(requireContext())

        val repository = AccommodationRepository(ApiClient.api)
        viewModel = ViewModelProvider(
            this,
            AccommodationDetailViewModel.Factory(repository)
        )[AccommodationDetailViewModel::class.java]

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AccommodationDetailUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                    scrollDetail.visibility = View.GONE
                }
                is AccommodationDetailUiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                    scrollDetail.visibility = View.VISIBLE

                    textName.text = state.accommodation.name
                    textAddress.text = state.accommodation.address
                    textDescription.text = state.accommodation.description
                    textAmenities.text = "Amenities: ${state.accommodation.amenities.joinToString(", ")}"

                    recyclerRooms.adapter = RoomAdapter(state.rooms) { room ->
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.studentFragmentContainer,
                                RoomDetailFragment.newInstance(room.id)
                            )
                            .addToBackStack(null)
                            .commit()
                    }
                }
                is AccommodationDetailUiState.Error -> {
                    progressBar.visibility = View.GONE
                    scrollDetail.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }

        viewModel.loadDetail(accommodationId)
    }
}