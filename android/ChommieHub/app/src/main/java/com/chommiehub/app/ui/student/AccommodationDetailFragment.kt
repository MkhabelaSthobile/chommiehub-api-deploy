package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository
import kotlinx.coroutines.launch

/** Shows one accommodation's details and lets the student start an application for it. */
class AccommodationDetailFragment : Fragment(R.layout.fragment_accommodation_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accommodationId = requireArguments().getString(ARG_ACCOMMODATION_ID).orEmpty()

        val tvName = view.findViewById<TextView>(R.id.tvDetailName)
        val tvLocation = view.findViewById<TextView>(R.id.tvDetailLocation)
        val tvDescription = view.findViewById<TextView>(R.id.tvDetailDescription)
        val tvAmenities = view.findViewById<TextView>(R.id.tvDetailAmenities)
        val btnApplyNow = view.findViewById<Button>(R.id.btnApplyNow)

        btnApplyNow.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val accommodation = StudentRepository(ApiClient.api)
                    .getAccommodations()
                    .firstOrNull { it.id == accommodationId }

                if (accommodation == null) {
                    Toast.makeText(requireContext(), "Residence was not found.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                tvName.text = accommodation.name
                tvLocation.text = accommodation.address
                tvDescription.text = accommodation.description
                tvAmenities.text = accommodation.amenities.joinToString("\n")
                btnApplyNow.isEnabled = true
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), exception.message ?: "Unable to load residence.", Toast.LENGTH_LONG).show()
            }
        }

        btnApplyNow.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.studentContainer, ApplicationFormFragment.newInstance(accommodationId))
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {
        private const val ARG_ACCOMMODATION_ID = "accommodationId"

        fun newInstance(accommodationId: String) = AccommodationDetailFragment().apply {
            arguments = Bundle().apply { putString(ARG_ACCOMMODATION_ID, accommodationId) }
        }
    }
}
