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
 *Displays complete application information and allows an authorised residence manager to approve or decline a pending application.
 */
class ManagerApplicationDetailFragment : Fragment() {
    companion object {
        private const val ARG_APPLICATION_ID = "applicationId"

        fun newInstance(applicationId: String): ManagerApplicationDetailFragment
        {
            return ManagerApplicationDetailFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(ARG_APPLICATION_ID, applicationId)
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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        applicationId = requireArguments().getString(ARG_APPLICATION_ID)
            ?: error("Application ID was not supplied.")
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
        observeState()

        view.findViewById<Button>(R.id.btnApplicationDetailBack).setOnClickListener {
            parentFragmentManager
                .popBackStack()
        }

        btnApprove.setOnClickListener {
            submitReview("Approved")
        }

        btnDecline.setOnClickListener {
            submitReview("Declined")
        }

        viewModel.loadApplication(applicationId)
    }

    private fun bindViews(
        view: View
    ) {
        progressBar = view.findViewById(R.id.progressApplicationDetail)

        tvError = view.findViewById(R.id.tvApplicationDetailError)

        tvStatus = view.findViewById(R.id.tvDetailStatus)

        tvStudent = view.findViewById(R.id.tvDetailStudent)

        tvRoomPreference = view.findViewById(R.id.tvDetailRoomPreference)

        tvRentalPeriod = view.findViewById(R.id.tvDetailRentalPeriod)

        tvSupportingInfo = view.findViewById(R.id.tvDetailSupportingInfo)

        tvReviewNotes = view.findViewById(R.id.tvExistingReviewNotes)

        tvReviewSectionTitle = view.findViewById(R.id.tvReviewSectionTitle)

        etReviewNotes = view.findViewById(R.id.etManagerReviewNotes)

        btnApprove = view.findViewById(R.id.btnApproveApplication)

        btnDecline = view.findViewById(R.id.btnDeclineApplication)
    }

    private fun configureViewModel() {
        val repository = ManagerRepository(ApiClient.api)

        viewModel = ViewModelProvider(
                this,
                ManagerApplicationDetailViewModel.Factory(repository)
            )[ManagerApplicationDetailViewModel::class.java]
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner)
        { state ->
            when (state) {
                ManagerApplicationDetailUiState.Loading -> showLoading()

                is ManagerApplicationDetailUiState.Success -> showApplication(state.application)

                is ManagerApplicationDetailUiState.Error -> showError(state.message)
            }
        }

        viewModel.message.observe(viewLifecycleOwner)
        { message ->
            if (message != null) {
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.consumeMessage()
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE

        tvError.visibility = View.GONE

        btnApprove.isEnabled = false

        btnDecline.isEnabled = false
    }

    private fun showApplication(application: ManagerApplicationDetailResponse)
    {
        progressBar.visibility = View.GONE

        tvError.visibility = View.GONE

        tvStatus.text = getString(
            R.string.detail_status,
            application.status
        )

        tvStudent.text = getString(
            R.string.detail_student,
            application.studentId
        )

        tvRoomPreference.text = getString(
            R.string.detail_room_preference,
            application.roomTypePreference
        )

        tvRentalPeriod.text = getString(
            R.string.detail_rental_period,
            application.rentalPeriod
        )

        tvSupportingInfo.text = getString(
            R.string.detail_supporting_info,
            application.supportingInfo
        )

        tvReviewNotes.text = getString(
            R.string.detail_review_notes,
            application.reviewNotes
                .ifBlank {
                    "None"
                }
        )

        val canReview = application.status == "Submitted" || application.status == "Under Review"

        tvReviewSectionTitle.visibility = if (canReview)
        {
                View.VISIBLE
            } else {
                View.GONE
            }

        etReviewNotes.visibility = if (canReview)
        {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnApprove.visibility = if (canReview)
        {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnDecline.visibility = if (canReview)
        {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnApprove.isEnabled = canReview

        btnDecline.isEnabled = canReview
    }

    private fun showError(message: String)
    {
        progressBar.visibility = View.GONE

        tvError.text = message

        tvError.visibility = View.VISIBLE
    }

    private fun submitReview(decision: String)
    {
        viewModel.reviewApplication(
            applicationId = applicationId,
            decision = decision,
            reviewNotes = etReviewNotes.text
                .toString()
                .trim()
        )
    }
}