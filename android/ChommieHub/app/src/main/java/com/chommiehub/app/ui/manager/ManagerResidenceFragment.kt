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
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.UpdateAccommodationRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository

/**
 * Allows a residence manager to review and update
 * accommodation details.
 */
class ManagerResidenceFragment : Fragment() {

    private lateinit var viewModel: ManagerResidenceViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    private lateinit var etName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPhone: EditText
    private lateinit var etInstitutionArea: EditText
    private lateinit var etAmenities: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_residence,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)
        configureViewModel()
        configureClickListeners(view)
        observeState()
        loadAccommodation()
    }

    /**
     * Connects the Fragment to the views in
     * fragment_manager_residence.xml.
     */
    private fun bindViews(view: View) {
        progressBar =
            view.findViewById(R.id.progressResidence)

        tvError =
            view.findViewById(R.id.tvResidenceError)

        etName =
            view.findViewById(R.id.etResidenceName)

        etAddress =
            view.findViewById(R.id.etResidenceAddress)

        etDescription =
            view.findViewById(R.id.etResidenceDescription)

        etPhone =
            view.findViewById(R.id.etResidencePhone)

        etInstitutionArea =
            view.findViewById(R.id.etResidenceInstitutionArea)

        etAmenities =
            view.findViewById(R.id.etResidenceAmenities)

        etLatitude =
            view.findViewById(R.id.etResidenceLatitude)

        etLongitude =
            view.findViewById(R.id.etResidenceLongitude)

        btnSave =
            view.findViewById(R.id.btnSaveResidence)
    }

    /**
     * Creates the repository and ViewModel used by this screen.
     */
    private fun configureViewModel() {
        val repository =
            ManagerRepository(ApiClient.api)

        val factory =
            ManagerResidenceViewModel.Factory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ManagerResidenceViewModel::class.java]
    }

    /**
     * Configures navigation and save actions.
     */
    private fun configureClickListeners(view: View) {

        view.findViewById<Button>(
            R.id.btnResidenceBack
        ).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            saveResidence()
        }

        view.findViewById<Button>(
            R.id.btnManageRooms
        ).setOnClickListener {

            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.managerFragmentContainer,
                    ManagerRoomsFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }

    /**
     * Loads the accommodation belonging to the manager.
     */
    private fun loadAccommodation() {

        val accommodationId = CurrentManager.ACCOMMODATION_ID

        if (accommodationId.isBlank()) {
            showError(
                "Manager accommodation could not be identified."
            )
            return
        }

        viewModel.loadAccommodation(
            accommodationId
        )
    }

    /**
     * Observes accommodation information and update messages.
     */
    private fun observeState() {

        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            when (state) {

                ManagerResidenceUiState.Loading -> {
                    showLoading()
                }

                is ManagerResidenceUiState.Success -> {
                    showAccommodation(
                        state.accommodation
                    )
                }

                is ManagerResidenceUiState.Error -> {
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

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        btnSave.isEnabled = false
    }

    private fun showAccommodation(
        accommodation: AccommodationResponse
    ) {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
        btnSave.isEnabled = true

        displayAccommodation(
            accommodation
        )
    }

    private fun showError(
        message: String
    ) {
        progressBar.visibility = View.GONE
        btnSave.isEnabled = true

        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    /**
     * Displays accommodation information in the form.
     */
    private fun displayAccommodation(
        accommodation: AccommodationResponse
    ) {
        etName.setText(
            accommodation.name
        )

        etAddress.setText(
            accommodation.address
        )

        etDescription.setText(
            accommodation.description
        )

        etPhone.setText(
            accommodation.phoneNumber
        )

        etInstitutionArea.setText(
            accommodation.institutionArea
        )

        etAmenities.setText(
            accommodation.amenities.joinToString(", ")
        )

        etLatitude.setText(
            accommodation.latitude.toString()
        )

        etLongitude.setText(
            accommodation.longitude.toString()
        )
    }

    /**
     * Validates the accommodation form and sends the
     * updated information to the ViewModel.
     */
    private fun saveResidence() {

        val name =
            etName.text.toString().trim()

        val address =
            etAddress.text.toString().trim()

        val description =
            etDescription.text.toString().trim()

        val phoneNumber =
            etPhone.text.toString().trim()

        val institutionArea =
            etInstitutionArea.text.toString().trim()

        val latitude =
            etLatitude.text
                .toString()
                .trim()
                .toDoubleOrNull()

        val longitude =
            etLongitude.text
                .toString()
                .trim()
                .toDoubleOrNull()

        if (
            name.isBlank() ||
            address.isBlank() ||
            description.isBlank()
        ) {
            Toast.makeText(
                requireContext(),
                "Name, address and description are required.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(
                requireContext(),
                "Enter valid latitude and longitude values.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (latitude !in -90.0..90.0) {
            Toast.makeText(
                requireContext(),
                "Latitude must be between -90 and 90.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (longitude !in -180.0..180.0) {
            Toast.makeText(
                requireContext(),
                "Longitude must be between -180 and 180.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val amenities =
            etAmenities.text
                .toString()
                .split(",")
                .map { amenity ->
                    amenity.trim()
                }
                .filter { amenity ->
                    amenity.isNotBlank()
                }

        val request =
            UpdateAccommodationRequest(
                name = name,
                address = address,
                latitude = latitude,
                longitude = longitude,
                description = description,
                phoneNumber = phoneNumber,
                amenities = amenities,
                institutionArea = institutionArea,
                isActive = true
            )

        val accommodationId = CurrentManager.ACCOMMODATION_ID

        if (accommodationId.isBlank()) {
            showError(
                "Manager accommodation could not be identified."
            )
            return
        }

        viewModel.updateAccommodation(
            accommodationId,
            request
        )
    }
}