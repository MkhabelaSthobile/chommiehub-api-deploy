package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.CreateRoomRequest
import com.chommiehub.app.data.model.manager.UpdateRoomRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * Provides one reusable screen for creating and editing rooms.
 */
class ManagerRoomFormFragment : Fragment() {

    companion object {
        private const val ARG_ROOM_ID = "roomId"

        fun newInstance(
            roomId: String?
        ): ManagerRoomFormFragment {
            return ManagerRoomFormFragment().apply {
                arguments = Bundle().apply {
                    if (!roomId.isNullOrBlank()) {
                        putString(
                            ARG_ROOM_ID,
                            roomId
                        )
                    }
                }
            }
        }
    }

    private val roomId: String?
        get() = arguments?.getString(ARG_ROOM_ID)

    private lateinit var viewModel: ManagerRoomFormViewModel

    private lateinit var etType: EditText
    private lateinit var etPrice: EditText
    private lateinit var etCapacity: EditText

    private lateinit var availabilitySwitch: SwitchMaterial

    private lateinit var tvTitle: TextView
    private lateinit var tvError: TextView

    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_room_form,
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
        configureViewModel()
        configureScreen()
        configureClickListeners(view)
        observeState()
        loadExistingRoom()
    }

    /**
     * Connects the Fragment to the views in
     * fragment_manager_room_form.xml.
     */
    private fun bindViews(
        view: View
    ) {
        etType =
            view.findViewById(R.id.etRoomType)

        etPrice =
            view.findViewById(R.id.etRoomPrice)

        etCapacity =
            view.findViewById(R.id.etRoomCapacity)

        availabilitySwitch =
            view.findViewById(R.id.switchRoomAvailable)

        tvTitle =
            view.findViewById(R.id.tvRoomFormTitle)

        tvError =
            view.findViewById(R.id.tvRoomFormError)

        btnSave =
            view.findViewById(R.id.btnSaveRoom)
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
            ManagerRoomFormViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerRoomFormViewModel::class.java]
    }

    /**
     * Changes the title depending on whether
     * a room is being created or edited.
     */
    private fun configureScreen() {
        tvTitle.text =
            if (roomId == null) {
                "Add Room"
            } else {
                "Edit Room"
            }
    }

    /**
     * Configures the screen buttons.
     */
    private fun configureClickListeners(
        view: View
    ) {
        view.findViewById<Button>(
            R.id.btnRoomFormBack
        ).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            saveRoom()
        }
    }

    /**
     * Loads room information when editing
     * an existing room.
     */
    private fun loadExistingRoom() {
        roomId?.let { existingRoomId ->

            if (existingRoomId.isNotBlank()) {
                viewModel.loadRoom(
                    existingRoomId
                )
            }
        }
    }

    /**
     * Observes room data, success messages
     * and errors from the ViewModel.
     */
    private fun observeState() {

        viewModel.room.observe(
            viewLifecycleOwner
        ) { room ->

            if (room != null) {
                etType.setText(
                    room.roomType
                )

                etPrice.setText(
                    room.price.toString()
                )

                etCapacity.setText(
                    room.capacity.toString()
                )

                availabilitySwitch.isChecked =
                    room.availability
            }
        }

        viewModel.message.observe(
            viewLifecycleOwner
        ) { message ->

            if (!message.isNullOrBlank()) {

                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.consumeMessage()

                parentFragmentManager.popBackStack()
            }
        }

        viewModel.error.observe(
            viewLifecycleOwner
        ) { message ->

            if (!message.isNullOrBlank()) {
                showError(
                    message
                )
            } else {
                hideError()
            }
        }
    }

    /**
     * Validates the room form and creates
     * or updates the room.
     */
    private fun saveRoom() {

        val type =
            etType.text
                .toString()
                .trim()

        val price =
            etPrice.text
                .toString()
                .trim()
                .toDoubleOrNull()

        val capacity =
            etCapacity.text
                .toString()
                .trim()
                .toIntOrNull()

        if (type.isBlank()) {
            showError(
                "Room type is required."
            )
            return
        }

        if (price == null || price < 0) {
            showError(
                "Enter a valid room price."
            )
            return
        }

        if (capacity == null || capacity < 1) {
            showError(
                "Room capacity must be at least 1."
            )
            return
        }

        hideError()

        val existingRoomId =
            roomId

        if (existingRoomId == null) {
            createRoom(
                type = type,
                price = price,
                capacity = capacity
            )
        } else {
            updateRoom(
                roomId = existingRoomId,
                type = type,
                price = price,
                capacity = capacity
            )
        }
    }

    /**
     * Creates a new room.
     */
    private fun createRoom(
        type: String,
        price: Double,
        capacity: Int
    ) {
        val accommodationId = CurrentManager.ACCOMMODATION_ID

        if (accommodationId.isBlank()) {
            showError(
                "Manager accommodation could not be identified."
            )
            return
        }

        val request =
            CreateRoomRequest(
                roomType = type,
                price = price,
                capacity = capacity,
                availability =
                    availabilitySwitch.isChecked
            )

        viewModel.createRoom(
            accommodationId,
            request
        )
    }

    /**
     * Updates an existing room.
     */
    private fun updateRoom(
        roomId: String,
        type: String,
        price: Double,
        capacity: Int
    ) {
        if (roomId.isBlank()) {
            showError(
                "Room could not be identified."
            )
            return
        }

        val request =
            UpdateRoomRequest(
                roomType = type,
                price = price,
                capacity = capacity,
                availability =
                    availabilitySwitch.isChecked
            )

        viewModel.updateRoom(
            roomId,
            request
        )
    }

    private fun showError(
        message: String
    ) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvError.text = ""
        tvError.visibility = View.GONE
    }
}