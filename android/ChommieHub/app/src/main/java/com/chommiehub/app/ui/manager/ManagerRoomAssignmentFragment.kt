package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository
import com.chommiehub.app.ui.manager.adapter.ManagerRoomAdapter

/**
 * Allows the residence manager to select an available room for an approved accommodation application.
 */
class ManagerRoomAssignmentFragment : Fragment()
{
    companion object {
        private const val ARG_APPLICATION_ID = "applicationId"

        private const val ARG_ACCOMMODATION_ID = "accommodationId"

        fun newInstance(
            applicationId: String,
            accommodationId: String
        ): ManagerRoomAssignmentFragment {
            return ManagerRoomAssignmentFragment()
                .apply {
                    arguments = Bundle().apply {
                            putString(
                                ARG_APPLICATION_ID,
                                applicationId
                            )

                            putString(
                                ARG_ACCOMMODATION_ID,
                                accommodationId
                            )
                        }
                }
        }
    }

    private lateinit var applicationId: String

    private lateinit var accommodationId: String

    private var selectedRoomId: String? = null

    private lateinit var viewModel: ManagerRoomAssignmentViewModel

    private lateinit var progressBar: ProgressBar

    private lateinit var recyclerView: RecyclerView

    private lateinit var tvMessage: TextView

    private lateinit var btnConfirm: Button

    private val adapter = ManagerRoomAdapter {

        room -> selectedRoomId = room.id

            btnConfirm.isEnabled = true
        }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        applicationId = requireArguments().getString(ARG_APPLICATION_ID)
            ?: error("Application ID missing.")

        accommodationId = requireArguments().getString(ARG_ACCOMMODATION_ID)
            ?: error("Accommodation ID missing.")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_room_assignment,
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

        progressBar = view.findViewById(R.id.progressRooms)

        recyclerView = view.findViewById(R.id.recyclerAvailableRooms)

        tvMessage = view.findViewById(R.id.tvRoomsMessage)

        btnConfirm = view.findViewById(R.id.btnConfirmRoomAssignment)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter

        val repository = ManagerRepository(ApiClient.api)

        viewModel = ViewModelProvider(
                this,
                ManagerRoomAssignmentViewModel.Factory(repository)
        )[ManagerRoomAssignmentViewModel::class.java]

        view.findViewById<Button>(R.id.btnRoomAssignmentBack).setOnClickListener {
            parentFragmentManager
                .popBackStack()
        }

        btnConfirm.setOnClickListener {
            selectedRoomId?.let {
                    roomId ->

                btnConfirm.isEnabled = false

                viewModel.assignRoom(
                    applicationId = applicationId,
                    roomId = roomId
                )
            }
        }

        observeState()

        viewModel.loadAvailableRooms(accommodationId)
    }

    private fun observeState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                RoomAssignmentUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE

                    tvMessage.visibility = View.GONE
                }

                is RoomAssignmentUiState.Rooms -> {
                    progressBar.visibility = View.GONE

                    adapter.submitList(state.rooms)

                    recyclerView.visibility =
                        if (
                            state.rooms.isEmpty()
                        ) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }

                    tvMessage.visibility =
                        if (
                            state.rooms.isEmpty()
                        ) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    if (
                        state.rooms.isEmpty()
                    ) {
                        tvMessage.text = "No rooms are currently available."
                    }
                }

                is RoomAssignmentUiState.Assigned -> {
                    progressBar.visibility = View.GONE

                    Toast.makeText(
                        requireContext(),
                        "Room assigned successfully.",
                        Toast.LENGTH_SHORT
                    ).show()

                    parentFragmentManager
                        .popBackStack()
                }

                is RoomAssignmentUiState.Error -> {
                    progressBar.visibility = View.GONE

                    btnConfirm.isEnabled = selectedRoomId != null

                    tvMessage.text = state.message

                    tvMessage.visibility = View.VISIBLE
                }
            }
        }
    }
}