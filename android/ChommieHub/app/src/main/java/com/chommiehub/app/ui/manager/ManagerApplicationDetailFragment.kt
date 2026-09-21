package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository

/**
 * Displays complete application information and allows
 * an authorised residence manager to approve or decline
 * a pending application.
 */
class ManagerApplicationDetailFragment : Fragment() {

    companion object {
        private const val ARG_APPLICATION_ID = "applicationId"

        fun newInstance(
            applicationId: String
        ): ManagerApplicationDetailFragment {

            return ManagerApplicationDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        ARG_APPLICATION_ID,
                        applicationId
                    )
                }
            }
        }
    }

    private lateinit var applicationId: String

    private lateinit var viewModel: ManagerApplicationDetailViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvStudent: TextView
    private lateinit var tvRoomPreference: TextView
    private lateinit var tvRentalPeriod: TextView
    private lateinit var tvSupportingInfo: TextView
    private lateinit var tvReviewNotes: TextView
    private lateinit var tvReviewSectionTitle: TextView

    private lateinit var etReviewNotes: EditText

    private lateinit var btnApprove: Button
    private lateinit var btnDecline: Button
    private lateinit var btnAssignRoom: Button

    private var currentApplication: ManagerApplicationDetailResponse? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        applicationId =
            arguments?.getString(ARG_APPLICATION_ID)
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException(
                    "Application ID was not supplied."
                )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_application_detail,
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
        configureClickListeners(view)
        observeState()
    }

    /**
     * Reloads the application when returning from
     * room assignment so the latest status is shown.
     */
    override fun onResume() {
        super.onResume()

        if (::viewModel.isInitialized) {
            loadApplication()
        }
    }

    /**
     * Connects the fragment to its XML views.
     */
    private fun bindViews(
        view: View
    ) {
        progressBar =
            view.findViewById(R.id.progressApplicationDetail)

        tvError =
            view.findViewById(R.id.tvApplicationDetailError)

        tvStatus =
            view.findViewById(R.id.tvDetailStatus)

        tvStudent =
            view.findViewById(R.id.tvDetailStudent)

        tvRoomPreference =
            view.findViewById(R.id.tvDetailRoomPreference)

        tvRentalPeriod =
            view.findViewById(R.id.tvDetailRentalPeriod)

        tvSupportingInfo =
            view.findViewById(R.id.tvDetailSupportingInfo)

        tvReviewNotes =
            view.findViewById(R.id.tvExistingReviewNotes)

        tvReviewSectionTitle =
            view.findViewById(R.id.tvReviewSectionTitle)

        etReviewNotes =
            view.findViewById(R.id.etManagerReviewNotes)

        btnApprove =
            view.findViewById(R.id.btnApproveApplication)

        btnDecline =
            view.findViewById(R.id.btnDeclineApplication)

        btnAssignRoom =
            view.findViewById(R.id.btnAssignApplicationRoom)
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
            ManagerApplicationDetailViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerApplicationDetailViewModel::class.java]
    }

    /**
     * Configures screen actions.
     */
    private fun configureClickListeners(
        view: View
    ) {

        view.findViewById<Button>(
            R.id.btnApplicationDetailBack
        ).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnApprove.setOnClickListener {
            submitReview(
                "Approved"
            )
        }

        btnDecline.setOnClickListener {
            submitReview(
                "Declined"
            )
        }

        btnAssignRoom.setOnClickListener {
            openRoomAssignment()
        }
    }

    /**
     * Loads the current application.
     */
    private fun loadApplication() {

        if (applicationId.isBlank()) {
            showError(
                "Application could not be identified."
            )
            return
        }

        viewModel.loadApplication(
            applicationId
        )
    }

    /**
     * Observes application information and messages.
     */
    private fun observeState() {

        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            when (state) {

                ManagerApplicationDetailUiState.Loading -> {
                    showLoading()
                }

                is ManagerApplicationDetailUiState.Success -> {
                    showApplication(
                        state.application
                    )
                }

                is ManagerApplicationDetailUiState.Error -> {
                    showError(
                        state.message
                    )
                }
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
            }
        }
    }

    /**
     * Displays the loading state.
     */
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE

        btnApprove.isEnabled = false
        btnDecline.isEnabled = false
        btnAssignRoom.isEnabled = false
    }

    /**
     * Displays application information.
     */
    private fun showApplication(
        application: ManagerApplicationDetailResponse
    ) {
        currentApplication = application

        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE

        tvStatus.text =
            getString(
                R.string.detail_status,
                application.status
            )

        tvStudent.text =
            getString(
                R.string.detail_student,
                application.studentId
            )

        tvRoomPreference.text =
            getString(
                R.string.detail_room_preference,
                application.roomTypePreference
            )

        tvRentalPeriod.text =
            getString(
                R.string.detail_rental_period,
                application.rentalPeriod
            )

        tvSupportingInfo.text =
            getString(
                R.string.detail_supporting_info,
                application.supportingInfo
            )

        val existingReviewNotes =
            application.reviewNotes
                .takeIf { it.isNotBlank() }
                ?: "None"

        tvReviewNotes.text =
            getString(
                R.string.detail_review_notes,
                existingReviewNotes
            )

        configureReviewSection(
            application
        )

        configureRoomAssignment(
            application
        )
    }

    /**
     * Controls whether the manager can approve
     * or decline the application.
     */
    private fun configureReviewSection(
        application: ManagerApplicationDetailResponse
    ) {
        val canReview =
            application.status.equals(
                "Submitted",
                ignoreCase = true
            ) ||
                    application.status.equals(
                        "Under Review",
                        ignoreCase = true
                    )

        val visibility =
            if (canReview) {
                View.VISIBLE
            } else {
                View.GONE
            }

        tvReviewSectionTitle.visibility =
            visibility

        etReviewNotes.visibility =
            visibility

        btnApprove.visibility =
            visibility

        btnDecline.visibility =
            visibility

        btnApprove.isEnabled =
            canReview

        btnDecline.isEnabled =
            canReview
    }

    /**
     * Shows the room-assignment button only when
     * the application has been approved and does
     * not already have an assigned room.
     */
    private fun configureRoomAssignment(
        application: ManagerApplicationDetailResponse
    ) {
        val canAssignRoom =
            application.status.equals(
                "Approved",
                ignoreCase = true
            ) &&
                    application.assignedRoomId.isBlank()

        btnAssignRoom.visibility =
            if (canAssignRoom) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnAssignRoom.isEnabled =
            canAssignRoom
    }

    /**
     * Submits the manager's application decision.
     */
    private fun submitReview(
        decision: String
    ) {
        if (applicationId.isBlank()) {
            showError(
                "Application could not be identified."
            )
            return
        }

        val reviewNotes =
            etReviewNotes.text
                .toString()
                .trim()

        viewModel.reviewApplication(
            applicationId = applicationId,
            decision = decision,
            reviewNotes = reviewNotes
        )
    }

    /**
     * Opens room assignment for an approved application.
     */
    private fun openRoomAssignment() {

        val application =
            currentApplication

        if (application == null) {
            showError(
                "Application information is not available."
            )
            return
        }

        if (application.accommodationId.isBlank()) {
            showError(
                "Accommodation could not be identified."
            )
            return
        }

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.managerFragmentContainer,
                ManagerRoomAssignmentFragment.newInstance(
                    applicationId =
                        application.id,
                    accommodationId =
                        application.accommodationId
                )
            )
            .addToBackStack(null)
            .commit()
    }

    /**
     * Displays an error message.
     */
    private fun showError(
        message: String
    ) {
        progressBar.visibility =
            View.GONE

        tvError.text =
            message

        tvError.visibility =
            View.VISIBLE

        btnApprove.isEnabled =
            false

        btnDecline.isEnabled =
            false

        btnAssignRoom.isEnabled =
            false
    }
}