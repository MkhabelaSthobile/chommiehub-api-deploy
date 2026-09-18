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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.chommiehub.app.BuildConfig
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.CreateRoomRequest
import com.chommiehub.app.data.model.manager.UpdateRoomRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository

/**
 * Provides one reusable screen for creating and editing rooms
 */
class ManagerRoomFormFragment : Fragment() {

    companion object {
        private const val ARG_ROOM_ID = "roomId"

        fun newInstance(
            roomId: String?
        ): ManagerRoomFormFragment {
            return ManagerRoomFormFragment()
                .apply {
                    arguments =
                        Bundle().apply {
                            roomId?.let {
                                putString(
                                    ARG_ROOM_ID,
                                    it
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

    private lateinit var tvError: TextView

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        etType = view.findViewById(R.id.etRoomType)

        etPrice = view.findViewById(R.id.etRoomPrice)

        etCapacity = view.findViewById(R.id.etRoomCapacity)

        availabilitySwitch = view.findViewById(R.id.switchRoomAvailable)

        tvError = view.findViewById(R.id.tvRoomFormError)

        view.findViewById<TextView>(
            R.id.tvRoomFormTitle
        ).text =
            if (roomId == null) {
                "Add Room"
            } else {
                "Edit Room"
            }

        val repository = ManagerRepository(ApiClient.api)

        viewModel = ViewModelProvider(
                this,
                ManagerRoomFormViewModel.Factory(
                    repository
                )
            )[ManagerRoomFormViewModel::class.java]

        view.findViewById<Button>(R.id.btnRoomFormBack).setOnClickListener {
            parentFragmentManager
                .popBackStack()
        }

        view.findViewById<Button>(R.id.btnSaveRoom).setOnClickListener { saveRoom()
        }

        observeState()

        roomId?.let {
            viewModel.loadRoom(it)
        }
    }

    private fun observeState() {
        viewModel.room.observe(
            viewLifecycleOwner
        ) { room ->
            if (room != null) {
                etType.setText(room.roomType
                )

                etPrice.setText(room.price.toString()
                )

                etCapacity.setText(room.capacity.toString()
                )

                availabilitySwitch.isChecked = room.availability
            }
        }

        viewModel.message.observe(
            viewLifecycleOwner
        ) { message ->
            if (message != null) {
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.consumeMessage()

                parentFragmentManager
                    .popBackStack()
            }
        }

        viewModel.error.observe(
            viewLifecycleOwner
        ) { message ->
            if (message != null) {
                tvError.text = message

                tvError.visibility = View.VISIBLE
            }
        }
    }

    private fun saveRoom() {
        val type = etType.text
                .toString()
                .trim()

        val price = etPrice.text
                .toString()
                .toDoubleOrNull()

        val capacity = etCapacity.text
                .toString()
                .toIntOrNull()

        if (
            type.isBlank() ||
            price == null ||
            capacity == null ||
            price < 0 ||
            capacity < 1
        ) {
            tvError.text = "Enter a valid room type, price and capacity."

            tvError.visibility = View.VISIBLE

            return
        }

        tvError.visibility = View.GONE

        val existingRoomId = roomId

        if (existingRoomId == null) {
            viewModel.createRoom(
                BuildConfig.MANAGER_ACCOMMODATION_ID,
                CreateRoomRequest(
                    roomType = type,
                    price = price,
                    capacity = capacity,
                    availability = availabilitySwitch.isChecked
                )
            )
        } else {
            viewModel.updateRoom(
                existingRoomId,
                UpdateRoomRequest(
                    roomType = type,
                    price = price,
                    capacity = capacity,
                    availability = availabilitySwitch.isChecked
                )
            )
        }
    }
}