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
 * Allows the residence manager to select an available
 * room for an approved accommodation application.
 */
class ManagerRoomAssignmentFragment : Fragment() {

    companion object {
        private const val ARG_APPLICATION_ID = "applicationId"
        private const val ARG_ACCOMMODATION_ID = "accommodationId"

        fun newInstance(
            applicationId: String,
            accommodationId: String
        ): ManagerRoomAssignmentFragment {

            return ManagerRoomAssignmentFragment().apply {
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

    private val roomAdapter by lazy {
        ManagerRoomAdapter { room ->
            selectedRoomId = room.id

            if (::btnConfirm.isInitialized) {
                btnConfirm.isEnabled = true
            }
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        applicationId =
            arguments?.getString(ARG_APPLICATION_ID)
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException(
                    "Application ID missing."
                )

        accommodationId =
            arguments?.getString(ARG_ACCOMMODATION_ID)
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException(
                    "Accommodation ID missing."
                )
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

        bindViews(view)
        configureRecyclerView()
        configureViewModel()
        configureClickListeners(view)
        observeState()
        loadAvailableRooms()
    }

    /**
     * Connects the Fragment to its XML views.
     */
    private fun bindViews(
        view: View
    ) {
        progressBar =
            view.findViewById(R.id.progressRooms)

        recyclerView =
            view.findViewById(R.id.recyclerAvailableRooms)

        tvMessage =
            view.findViewById(R.id.tvRoomsMessage)

        btnConfirm =
            view.findViewById(R.id.btnConfirmRoomAssignment)

        // A room must be selected before assignment.
        btnConfirm.isEnabled = false
    }

    /**
     * Configures the list of available rooms.
     */
    private fun configureRecyclerView() {
        recyclerView.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerView.adapter =
            roomAdapter
    }

    /**
     * Creates the repository and ViewModel.
     */
    private fun configureViewModel() {
        val repository =
            ManagerRepository(
                ApiClient.api
            )

        val factory =
            ManagerRoomAssignmentViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerRoomAssignmentViewModel::class.java]
    }

    /**
     * Configures the screen buttons.
     */
    private fun configureClickListeners(
        view: View
    ) {
        view.findViewById<Button>(
            R.id.btnRoomAssignmentBack
        ).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnConfirm.setOnClickListener {
            confirmRoomAssignment()
        }
    }

    /**
     * Loads rooms that are currently available
     * for the application's accommodation.
     */
    private fun loadAvailableRooms() {
        if (accommodationId.isBlank()) {
            showError(
                "Accommodation could not be identified."
            )
            return
        }

        viewModel.loadAvailableRooms(
            accommodationId
        )
    }

    /**
     * Assigns the selected room to the application.
     */
    private fun confirmRoomAssignment() {
        val roomId =
            selectedRoomId

        if (roomId.isNullOrBlank()) {
            showError(
                "Please select a room before confirming."
            )
            return
        }

        if (applicationId.isBlank()) {
            showError(
                "Application could not be identified."
            )
            return
        }

        btnConfirm.isEnabled = false

        viewModel.assignRoom(
            applicationId = applicationId,
            roomId = roomId
        )
    }

    /**
     * Observes the room-assignment state.
     */
    private fun observeState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            when (state) {

                RoomAssignmentUiState.Loading -> {
                    showLoading()
                }

                is RoomAssignmentUiState.Rooms -> {
                    showRooms(
                        state.rooms
                    )
                }

                is RoomAssignmentUiState.Assigned -> {
                    showAssignmentSuccess()
                }

                is RoomAssignmentUiState.Error -> {
                    showError(
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
        progressBar.visibility =
            View.VISIBLE

        recyclerView.visibility =
            View.GONE

        tvMessage.visibility =
            View.GONE

        btnConfirm.isEnabled =
            false
    }

    /**
     * Displays the available rooms.
     */
    private fun showRooms(
        rooms: List<com.chommiehub.app.data.model.manager.RoomResponse>
    ) {
        progressBar.visibility =
            View.GONE

        roomAdapter.submitList(
            rooms
        )

        if (rooms.isEmpty()) {
            recyclerView.visibility =
                View.GONE

            tvMessage.text =
                "No rooms are currently available."

            tvMessage.visibility =
                View.VISIBLE

            selectedRoomId =
                null

            btnConfirm.isEnabled =
                false
        } else {
            recyclerView.visibility =
                View.VISIBLE

            tvMessage.visibility =
                View.GONE

            // Keep disabled until the manager selects a room.
            btnConfirm.isEnabled =
                selectedRoomId != null
        }
    }

    /**
     * Handles successful room assignment.
     */
    private fun showAssignmentSuccess() {
        progressBar.visibility =
            View.GONE

        btnConfirm.isEnabled =
            false

        Toast.makeText(
            requireContext(),
            "Room assigned successfully.",
            Toast.LENGTH_SHORT
        ).show()

        parentFragmentManager.popBackStack()
    }

    /**
     * Displays an error.
     */
    private fun showError(
        message: String
    ) {
        progressBar.visibility =
            View.GONE

        tvMessage.text =
            message

        tvMessage.visibility =
            View.VISIBLE

        btnConfirm.isEnabled =
            selectedRoomId != null
    }
}