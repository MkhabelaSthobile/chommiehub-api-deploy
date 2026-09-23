package com.chommiehub.app.ui.resident

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
import com.chommiehub.app.data.repository.ResidentServicesRepository
import com.chommiehub.app.ui.student.CurrentStudent

/**
 * Resident dashboard shown after a student's approved application receives a room assignment.
 */
class ResidentHomeFragment : Fragment(R.layout.fragment_resident_home) {

    private lateinit var viewModel: ResidentHomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBar = view.findViewById<ProgressBar>(R.id.progressResidentHome)
        val content = view.findViewById<View>(R.id.residentHomeContent)
        val tvError = view.findViewById<TextView>(R.id.tvResidentHomeError)
        val tvResidence = view.findViewById<TextView>(R.id.tvResidentHomeResidence)
        val tvAddress = view.findViewById<TextView>(R.id.tvResidentHomeAddress)
        val tvRoom = view.findViewById<TextView>(R.id.tvResidentHomeRoom)
        val tvPrice = view.findViewById<TextView>(R.id.tvResidentHomePrice)

        val residentRepository = ResidentServicesRepository(ApiClient.api)
        val accommodationRepository = AccommodationRepository(ApiClient.api)

        viewModel = ViewModelProvider(
            this,
            ResidentHomeViewModel.Factory(residentRepository, accommodationRepository)
        )[ResidentHomeViewModel::class.java]

        view.findViewById<Button>(R.id.btnResidentMyResidence).setOnClickListener {
            openFragment(MyResidenceFragment())
        }

        view.findViewById<Button>(R.id.btnResidentMaintenance).setOnClickListener {
            openFragment(MaintenanceFragment())
        }

        view.findViewById<Button>(R.id.btnResidentVisitors).setOnClickListener {
            openFragment(VisitorRequestFragment())
        }

        view.findViewById<Button>(R.id.btnResidentInspections).setOnClickListener {
            openFragment(InspectionFragment())
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ResidentHomeUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    content.visibility = View.GONE
                    tvError.visibility = View.GONE
                }

                is ResidentHomeUiState.Success -> {
                    progressBar.visibility = View.GONE
                    content.visibility = View.VISIBLE
                    tvError.visibility = View.GONE

                    tvResidence.text = state.accommodation.name
                    tvAddress.text = state.accommodation.address
                    tvRoom.text = "${state.room.roomType} • Capacity ${state.room.capacity}"
                    tvPrice.text = "R${state.room.price.toInt()} / month"
                }

                is ResidentHomeUiState.Error -> {
                    progressBar.visibility = View.GONE
                    content.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }

        viewModel.load(CurrentStudent.ID)
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.studentContainer, fragment).addToBackStack(null).commit()
    }
}