package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.AccommodationRepository

/**
 * Collects the student's selected residence, room and application information before
 * moving to the review screen. No API submission occurs until the student confirms
 * the information on ApplicationReviewFragment.
 */
class ApplicationFormFragment : Fragment(R.layout.fragment_application_form) {

    private lateinit var viewModel: ApplicationFormViewModel

    private lateinit var spAccommodation: Spinner
    private lateinit var spRoom: Spinner
    private lateinit var etRentalPeriod: EditText
    private lateinit var etSupportingInfo: EditText
    private lateinit var btnReviewApplication: Button

    private var accommodations: List<AccommodationResponse> = emptyList()
    private var rooms: List<RoomResponse> = emptyList()

    private var requestedAccommodationId: String? = null
    private var requestedRoomId: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestedAccommodationId = arguments?.getString(ARG_ACCOMMODATION_ID)
        requestedRoomId = arguments?.getString(ARG_ROOM_ID)

        spAccommodation = view.findViewById(R.id.spAccommodation)
        spRoom = view.findViewById(R.id.spRoom)
        etRentalPeriod = view.findViewById(R.id.etRentalPeriod)
        etSupportingInfo = view.findViewById(R.id.etSupportingInfo)
        btnReviewApplication = view.findViewById(R.id.btnReviewApplication)

        val repository = AccommodationRepository(ApiClient.api)
        viewModel = ViewModelProvider(this, ApplicationFormViewModel.Factory(repository))[ApplicationFormViewModel::class.java]

        observeState()

        spAccommodation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, selectedView: View?, position: Int, id: Long) {
                val accommodation = accommodations.getOrNull(position) ?: return
                viewModel.loadRooms(accommodation.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        btnReviewApplication.setOnClickListener {
            reviewApplication()
        }

        viewModel.loadAccommodations()
    }

    private fun observeState() {
        viewModel.accommodations.observe(viewLifecycleOwner) { list ->
            accommodations = list

            spAccommodation.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, list.map { it.name })

            requestedAccommodationId?.let { accommodationId ->
                val index = list.indexOfFirst { it.id == accommodationId }

                if (index >= 0) {
                    spAccommodation.setSelection(index)
                }
            }
        }

        viewModel.rooms.observe(viewLifecycleOwner) { list ->
            rooms = list

            spRoom.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list.map { "${it.roomType} • R${it.price.toInt()}/month" }
            )

            requestedRoomId?.let { roomId ->
                val index = list.indexOfFirst { it.id == roomId }

                if (index >= 0) {
                    spRoom.setSelection(index)
                }
            }

            btnReviewApplication.isEnabled = list.isNotEmpty()
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun reviewApplication() {
        val accommodation = accommodations.getOrNull(spAccommodation.selectedItemPosition)

        if (accommodation == null) {
            toast("Choose a residence.")
            return
        }

        val room = rooms.getOrNull(spRoom.selectedItemPosition)

        if (room == null) {
            toast("Choose an available room.")
            return
        }

        val rentalPeriod = etRentalPeriod.text.toString().trim()

        if (rentalPeriod.isBlank()) {
            toast("Enter your preferred rental period.")
            return
        }

        val supportingInfo = etSupportingInfo.text.toString().trim()

        parentFragmentManager.beginTransaction()
            .replace(
                R.id.studentContainer,
                ApplicationReviewFragment.newInstance(
                    accommodationId = accommodation.id,
                    accommodationName = accommodation.name,
                    roomId = room.id,
                    roomType = room.roomType,
                    roomPrice = room.price,
                    rentalPeriod = rentalPeriod,
                    supportingInfo = supportingInfo
                )
            )
            .addToBackStack(null)
            .commit()
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val ARG_ACCOMMODATION_ID = "accommodationId"
        private const val ARG_ROOM_ID = "roomId"

        fun newInstance(accommodationId: String? = null, roomId: String? = null): ApplicationFormFragment {
            return ApplicationFormFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACCOMMODATION_ID, accommodationId)
                    putString(ARG_ROOM_ID, roomId)
                }
            }
        }
    }
}