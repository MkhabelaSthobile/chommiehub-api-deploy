package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.AccommodationRepository

/**
 * Displays a specific room and allows the student to begin an application for that room.
 */
class RoomDetailFragment : Fragment(R.layout.fragment_room_detail) {

    companion object {
        private const val ARG_ROOM_ID = "roomId"

        fun newInstance(roomId: String): RoomDetailFragment {
            return RoomDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_ROOM_ID, roomId) }
            }
        }
    }

    private lateinit var viewModel: RoomDetailViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roomId = requireArguments().getString(ARG_ROOM_ID) ?: throw IllegalStateException("RoomDetailFragment requires a roomId argument.")

        val progressBar = view.findViewById<ProgressBar>(R.id.progressRoomDetail)
        val tvError = view.findViewById<TextView>(R.id.tvRoomDetailError)
        val content = view.findViewById<View>(R.id.layoutRoomContent)
        val textType = view.findViewById<TextView>(R.id.textRoomDetailType)
        val textPrice = view.findViewById<TextView>(R.id.textRoomDetailPrice)
        val textCapacity = view.findViewById<TextView>(R.id.textRoomDetailCapacity)
        val textAvailability = view.findViewById<TextView>(R.id.textRoomDetailAvailability)
        val btnApply = view.findViewById<Button>(R.id.btnApplyForRoom)

        val repository = AccommodationRepository(ApiClient.api)
        viewModel = ViewModelProvider(this, RoomDetailViewModel.Factory(repository))[RoomDetailViewModel::class.java]

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                RoomDetailUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                    content.visibility = View.GONE
                }

                is RoomDetailUiState.Success -> {
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                    content.visibility = View.VISIBLE

                    textType.text = state.room.roomType
                    textPrice.text = "R${state.room.price.toInt()} / month"
                    textCapacity.text = "Capacity: ${state.room.capacity}"
                    textAvailability.text = if (state.room.availability) "Available" else "Not currently available"

                    btnApply.isEnabled = state.room.availability

                    btnApply.setOnClickListener {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.studentContainer, ApplicationFormFragment.newInstance(state.room.accommodationId, state.room.id))
                            .addToBackStack(null)
                            .commit()
                    }
                }

                is RoomDetailUiState.Error -> {
                    progressBar.visibility = View.GONE
                    content.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }

        viewModel.loadRoom(roomId)
    }
}