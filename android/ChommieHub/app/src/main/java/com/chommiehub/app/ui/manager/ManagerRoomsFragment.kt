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
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository
import com.chommiehub.app.ui.manager.adapter.ManagerRoomManagementAdapter

/**
 * Displays and manages the room inventory
 * belonging to the manager's residence.
 */
class ManagerRoomsFragment : Fragment() {

    private lateinit var viewModel: ManagerRoomsViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMessage: TextView

    private val roomAdapter by lazy {
        ManagerRoomManagementAdapter { room ->
            openRoomForm(room.id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_rooms,
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
    }

    /**
     * Refresh rooms whenever the manager returns
     * to this screen.
     */
    override fun onResume() {
        super.onResume()
        loadRooms()
    }

    /**
     * Connects the fragment to the views in
     * fragment_manager_rooms.xml.
     */
    private fun bindViews(view: View) {
        progressBar =
            view.findViewById(R.id.progressManagedRooms)

        recyclerView =
            view.findViewById(R.id.recyclerManagedRooms)

        tvMessage =
            view.findViewById(R.id.tvManagedRoomsMessage)
    }

    /**
     * Configures the room list.
     */
    private fun configureRecyclerView() {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        recyclerView.adapter =
            roomAdapter
    }

    /**
     * Creates the repository and ViewModel.
     */
    private fun configureViewModel() {
        val repository =
            ManagerRepository(ApiClient.api)

        val factory =
            ManagerRoomsViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerRoomsViewModel::class.java]
    }

    /**
     * Configures navigation and room-management actions.
     */
    private fun configureClickListeners(view: View) {

        view.findViewById<Button>(
            R.id.btnRoomsBack
        ).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(
            R.id.btnAddRoom
        ).setOnClickListener {
            openRoomForm(null)
        }
    }

    /**
     * Loads rooms belonging to the manager's accommodation.
     */
    private fun loadRooms() {
        val accommodationId = CurrentManager.ACCOMMODATION_ID

        if (accommodationId.isBlank()) {
            showError(
                "Manager accommodation could not be identified."
            )
            return
        }

        viewModel.loadRooms(
            accommodationId
        )
    }

    /**
     * Observes room data returned by the ViewModel.
     */
    private fun observeState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            when (state) {

                ManagerRoomsUiState.Loading -> {
                    showLoading()
                }

                is ManagerRoomsUiState.Success -> {
                    showRooms(
                        state.rooms
                    )
                }

                is ManagerRoomsUiState.Error -> {
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
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvMessage.visibility = View.GONE
    }

    /**
     * Displays rooms returned by the API.
     */
    private fun showRooms(
        rooms: List<com.chommiehub.app.data.model.manager.RoomResponse>
    ) {
        progressBar.visibility = View.GONE

        roomAdapter.submitList(
            rooms
        )

        if (rooms.isEmpty()) {
            recyclerView.visibility = View.GONE

            tvMessage.text =
                "No rooms have been created yet."

            tvMessage.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvMessage.visibility = View.GONE
        }
    }

    /**
     * Displays an error message.
     */
    private fun showError(
        message: String
    ) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE

        tvMessage.text = message
        tvMessage.visibility = View.VISIBLE
    }

    /**
     * Opens the room form.
     *
     * A null room ID means a new room is being created.
     * An existing room ID means the room is being edited.
     */
    private fun openRoomForm(
        roomId: String?
    ) {
        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.managerFragmentContainer,
                ManagerRoomFormFragment.newInstance(
                    roomId
                )
            )
            .addToBackStack(null)
            .commit()
    }
}