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
import com.chommiehub.app.ui.manager.adapter.ManagerRoomManagementAdapter

/**
 * Displays and manages the room inventory belonging to the residence
 */
class ManagerRoomsFragment : Fragment()
{

    private lateinit var viewModel: ManagerRoomsViewModel

    private lateinit var progressBar: ProgressBar

    private lateinit var recyclerView: RecyclerView

    private lateinit var tvMessage: TextView

    private val adapter = ManagerRoomManagementAdapter {
        room -> openRoomForm(room.id)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        progressBar = view.findViewById(R.id.progressManagedRooms)

        recyclerView = view.findViewById(R.id.recyclerManagedRooms)

        tvMessage = view.findViewById(R.id.tvManagedRoomsMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter

        val repository = ManagerRepository(ApiClient.api)

        viewModel = ViewModelProvider(
                this,
                ManagerRoomsViewModel.Factory(repository)
            )[ManagerRoomsViewModel::class.java]

        view.findViewById<Button>(R.id.btnRoomsBack).setOnClickListener {
            parentFragmentManager
                .popBackStack()
        }

        view.findViewById<Button>(R.id.btnAddRoom).setOnClickListener {
            openRoomForm(
                null
            )
        }

        observeState()
    }

    override fun onResume() {
        super.onResume()

        viewModel.loadRooms(BuildConfig.MANAGER_ACCOMMODATION_ID)
    }

    private fun observeState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                ManagerRoomsUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }

                is ManagerRoomsUiState.Success -> {
                    progressBar.visibility = View.GONE

                    adapter.submitList(state.rooms)

                    recyclerView.visibility =
                        if (state.rooms.isEmpty())
                            View.GONE
                        else
                            View.VISIBLE

                    tvMessage.visibility =
                        if (state.rooms.isEmpty())
                            View.VISIBLE
                        else
                            View.GONE

                    if (state.rooms.isEmpty()) {
                        tvMessage.text = "No rooms have been created yet."
                    }
                }

                is ManagerRoomsUiState.Error -> {
                    progressBar.visibility = View.GONE

                    tvMessage.text = state.message

                    tvMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun openRoomForm(roomId: String?)
    {
        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.managerFragmentContainer,
                ManagerRoomFormFragment
                    .newInstance(roomId)
            )
            .addToBackStack(null)
            .commit()
    }
}