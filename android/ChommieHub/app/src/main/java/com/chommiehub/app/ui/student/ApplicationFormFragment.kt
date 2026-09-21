package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.student.SubmitApplicationRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Lets a signed-in student apply for accommodation. Populated the residence spinner from
 * every active accommodation and submits through the same "applications" workflow the
 * residence manager reviews.
 */
class ApplicationFormFragment : Fragment() {

    private lateinit var viewModel: ApplicationFormViewModel

    private lateinit var spAccommodation: Spinner
    private lateinit var spRoomType: Spinner
    private lateinit var etRentalPeriod: EditText
    private lateinit var etSupportingInfo: EditText
    private lateinit var btnSubmitApplication: Button

    private var accommodations: List<AccommodationResponse> = emptyList()

    private val roomTypeOptions = listOf(
        "Single room",
        "Shared room",
        "Bachelor flat"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_application_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spAccommodation = view.findViewById(R.id.spAccommodation)
        spRoomType = view.findViewById(R.id.spRoomType)
        etRentalPeriod = view.findViewById(R.id.etRentalPeriod)
        etSupportingInfo = view.findViewById(R.id.etSupportingInfo)
        btnSubmitApplication = view.findViewById(R.id.btnSubmitApplication)

        spRoomType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            roomTypeOptions
        )

        val repository = StudentRepository(ApiClient.api)
        viewModel = ViewModelProvider(
            this,
            ApplicationFormViewModel.Factory(repository)
        )[ApplicationFormViewModel::class.java]

        observeState()

        btnSubmitApplication.setOnClickListener { submit() }

        viewModel.loadAccommodations()
    }

    private fun observeState() {
        viewModel.accommodations.observe(viewLifecycleOwner) { list ->
            accommodations = list
            spAccommodation.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list.map { it.name }
            )

            val preselectId = requireArguments().getString(ARG_ACCOMMODATION_ID)
            if (preselectId != null) {
                val index = list.indexOfFirst { it.id == preselectId }
                if (index >= 0) spAccommodation.setSelection(index)
            }
        }

        viewModel.loadError.observe(viewLifecycleOwner) { message ->
            if (message != null) toast(message)
        }

        viewModel.isSubmitting.observe(viewLifecycleOwner) { submitting ->
            btnSubmitApplication.isEnabled = !submitting
        }

        viewModel.submitResult.observe(viewLifecycleOwner) { application ->
            if (application != null) {
                toast("Application submitted.")
                viewModel.consumeSubmitResult()
                parentFragmentManager.popBackStack()
            }
        }

        viewModel.submitError.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                toast(message)
                viewModel.consumeSubmitError()
            }
        }
    }

    private fun submit() {
        if (accommodations.isEmpty()) {
            toast("No residences are available to apply to right now.")
            return
        }

        val accommodation = accommodations.getOrNull(spAccommodation.selectedItemPosition)
        if (accommodation == null) {
            toast("Choose a residence.")
            return
        }

        val roomType = roomTypeOptions.getOrNull(spRoomType.selectedItemPosition)
        if (roomType == null) {
            toast("Choose a room preference.")
            return
        }

        val rentalPeriod = etRentalPeriod.text.toString().trim()
        if (rentalPeriod.isBlank()) {
            toast("Enter your preferred rental period.")
            return
        }

        val supportingInfo = etSupportingInfo.text.toString().trim()

        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid == null) {
            toast("No signed-in student found.")
            return
        }

        viewModel.submit(
            firebaseUid,
            SubmitApplicationRequest(
                accommodationId = accommodation.id,
                roomTypePreference = roomType,
                rentalPeriod = rentalPeriod,
                supportingInfo = supportingInfo
            )
        )
    }

    private fun toast(message: String) =
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

    companion object {
        private const val ARG_ACCOMMODATION_ID = "accommodationId"

        /** Opens the form with a residence already selected, e.g. from the accommodation detail screen. */
        fun newInstance(accommodationId: String? = null) = ApplicationFormFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ACCOMMODATION_ID, accommodationId)
            }
        }
    }
}
