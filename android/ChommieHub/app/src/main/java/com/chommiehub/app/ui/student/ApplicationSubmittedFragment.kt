package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chommiehub.app.R

/**
 * Confirms that the application was successfully created by the backend.
 */
class ApplicationSubmittedFragment : Fragment(R.layout.fragment_application_submitted) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvSubmittedApplicationId).text = requireArguments().getString(ARG_APPLICATION_ID).orEmpty()
        view.findViewById<TextView>(R.id.tvSubmittedResidence).text = requireArguments().getString(ARG_ACCOMMODATION_NAME).orEmpty()
        view.findViewById<TextView>(R.id.tvSubmittedRoom).text = requireArguments().getString(ARG_ROOM_TYPE).orEmpty()
        view.findViewById<TextView>(R.id.tvSubmittedStatus).text = requireArguments().getString(ARG_STATUS).orEmpty()

        view.findViewById<Button>(R.id.btnViewMyApplications).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.studentContainer, MyApplicationsFragment())
                .commit()
        }

        view.findViewById<Button>(R.id.btnReturnHome).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.studentContainer, StudentHomeFragment())
                .commit()
        }
    }

    companion object {
        private const val ARG_APPLICATION_ID = "applicationId"
        private const val ARG_ACCOMMODATION_NAME = "accommodationName"
        private const val ARG_ROOM_TYPE = "roomType"
        private const val ARG_STATUS = "status"

        fun newInstance(applicationId: String, accommodationName: String, roomType: String, status: String): ApplicationSubmittedFragment {
            return ApplicationSubmittedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_APPLICATION_ID, applicationId)
                    putString(ARG_ACCOMMODATION_NAME, accommodationName)
                    putString(ARG_ROOM_TYPE, roomType)
                    putString(ARG_STATUS, status)
                }
            }
        }
    }
}