package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.student.SubmitApplicationRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository

/**
 * Gives the student a final confirmation screen before an application is submitted.
 */
class ApplicationReviewFragment : Fragment(R.layout.fragment_application_review) {

    private lateinit var viewModel: ApplicationReviewViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accommodationId = requireArguments().getString(ARG_ACCOMMODATION_ID).orEmpty()
        val accommodationName = requireArguments().getString(ARG_ACCOMMODATION_NAME).orEmpty()
        val roomId = requireArguments().getString(ARG_ROOM_ID).orEmpty()
        val roomType = requireArguments().getString(ARG_ROOM_TYPE).orEmpty()
        val roomPrice = requireArguments().getDouble(ARG_ROOM_PRICE)
        val rentalPeriod = requireArguments().getString(ARG_RENTAL_PERIOD).orEmpty()
        val supportingInfo = requireArguments().getString(ARG_SUPPORTING_INFO).orEmpty()

        view.findViewById<TextView>(R.id.tvReviewResidence).text = accommodationName
        view.findViewById<TextView>(R.id.tvReviewRoom).text = "$roomType • R${roomPrice.toInt()}/month"
        view.findViewById<TextView>(R.id.tvReviewRentalPeriod).text = rentalPeriod
        view.findViewById<TextView>(R.id.tvReviewSupportingInfo).text = if (supportingInfo.isBlank()) "No additional supporting information." else supportingInfo

        val progressBar = view.findViewById<ProgressBar>(R.id.progressApplicationReview)
        val btnSubmit = view.findViewById<Button>(R.id.btnConfirmSubmitApplication)
        val repository = StudentRepository(ApiClient.api)

        viewModel = ViewModelProvider(this, ApplicationReviewViewModel.Factory(repository))[ApplicationReviewViewModel::class.java]

        btnSubmit.setOnClickListener {
            val studentId = CurrentStudent.ID

            if (studentId.isBlank()) {
                Toast.makeText(requireContext(), "No signed-in student was found.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            viewModel.submit(
                studentId,
                SubmitApplicationRequest(
                    accommodationId = accommodationId,
                    roomId = roomId,
                    rentalPeriod = rentalPeriod,
                    supportingInfo = supportingInfo
                )
            )
        }

        viewModel.submitting.observe(viewLifecycleOwner) { submitting ->
            progressBar.visibility = if (submitting) View.VISIBLE else View.GONE
            btnSubmit.isEnabled = !submitting
        }

        viewModel.result.observe(viewLifecycleOwner) { application ->
            if (application != null) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.studentContainer,
                        ApplicationSubmittedFragment.newInstance(
                            applicationId = application.id,
                            accommodationName = application.accommodationName,
                            roomType = application.roomTypePreference,
                            status = application.status
                        )
                    )
                    .commit()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val ARG_ACCOMMODATION_ID = "accommodationId"
        private const val ARG_ACCOMMODATION_NAME = "accommodationName"
        private const val ARG_ROOM_ID = "roomId"
        private const val ARG_ROOM_TYPE = "roomType"
        private const val ARG_ROOM_PRICE = "roomPrice"
        private const val ARG_RENTAL_PERIOD = "rentalPeriod"
        private const val ARG_SUPPORTING_INFO = "supportingInfo"

        fun newInstance(accommodationId: String, accommodationName: String, roomId: String, roomType: String, roomPrice: Double, rentalPeriod: String, supportingInfo: String): ApplicationReviewFragment {
            return ApplicationReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ACCOMMODATION_ID, accommodationId)
                    putString(ARG_ACCOMMODATION_NAME, accommodationName)
                    putString(ARG_ROOM_ID, roomId)
                    putString(ARG_ROOM_TYPE, roomType)
                    putDouble(ARG_ROOM_PRICE, roomPrice)
                    putString(ARG_RENTAL_PERIOD, rentalPeriod)
                    putString(ARG_SUPPORTING_INFO, supportingInfo)
                }
            }
        }
    }
}