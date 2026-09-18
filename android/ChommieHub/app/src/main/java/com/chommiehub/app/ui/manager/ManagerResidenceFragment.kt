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
import com.chommiehub.app.BuildConfig
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.UpdateAccommodationRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository

/**
 *Allows a residence manager to review and update accommodation details
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
        super.onViewCreated(
            view,
            savedInstanceState
        )

        bindViews(view)
        configureViewModel()
        observeState()

        view.findViewById<Button>(R.id.btnResidenceBack).setOnClickListener {
            parentFragmentManager
                .popBackStack()
        }

        btnSave.setOnClickListener {
            saveResidence()
        }

        view.findViewById<Button>(R.id.btnManageRooms).setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.managerFragmentContainer,
                    ManagerRoomsFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        viewModel.loadAccommodation(BuildConfig.MANAGER_ACCOMMODATION_ID
        )
    }

    private fun bindViews(view: View)
    {
        progressBar = view.findViewById(R.id.progressResidence)

        tvError = view.findViewById(R.id.tvResidenceError)

        etName = view.findViewById(R.id.etResidenceName)

        etAddress = view.findViewById(R.id.etResidenceAddress)

        etDescription = view.findViewById(R.id.etResidenceDescription)

        etPhone = view.findViewById(R.id.etResidencePhone)

        etInstitutionArea = view.findViewById(R.id.etResidenceInstitutionArea)

        etAmenities = view.findViewById(R.id.etResidenceAmenities)

        etLatitude = view.findViewById(R.id.etResidenceLatitude)

        etLongitude = view.findViewById(R.id.etResidenceLongitude)

        btnSave = view.findViewById(R.id.btnSaveResidence)
    }

    private fun configureViewModel() {
        val repository = ManagerRepository(ApiClient.api)

        viewModel = ViewModelProvider(
                this,
                ManagerResidenceViewModel.Factory(repository)
            )[ManagerResidenceViewModel::class.java]
    }

    private fun observeState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            when (state) {
                ManagerResidenceUiState.Loading -> {
                    progressBar.visibility = View.VISIBLE

                    tvError.visibility = View.GONE

                    btnSave.isEnabled = false
                }

                is ManagerResidenceUiState.Success -> {
                    progressBar.visibility = View.GONE

                    tvError.visibility = View.GONE

                    btnSave.isEnabled = true

                    displayAccommodation(state.accommodation)
                }

                is ManagerResidenceUiState.Error -> {
                    progressBar.visibility = View.GONE

                    btnSave.isEnabled = true

                    tvError.text = state.message

                    tvError.visibility = View.VISIBLE
                }
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
            }
        }
    }

    private fun displayAccommodation(accommodation: AccommodationResponse)
    {
        etName.setText(accommodation.name)

        etAddress.setText(accommodation.address)

        etDescription.setText(accommodation.description)

        etPhone.setText(accommodation.phoneNumber)

        etInstitutionArea.setText(accommodation.institutionArea)

        etAmenities.setText(accommodation.amenities.joinToString(", "))

        etLatitude.setText(accommodation.latitude.toString())

        etLongitude.setText(accommodation.longitude.toString())
    }

    private fun saveResidence() {
        val name = etName.text.toString().trim()

        val address = etAddress.text.toString().trim()

        val description = etDescription.text
                .toString()
                .trim()

        val latitude = etLatitude.text
                .toString()
                .toDoubleOrNull()

        val longitude = etLongitude.text
                .toString()
                .toDoubleOrNull()

        if (name.isBlank() || address.isBlank() || description.isBlank())
        {
            Toast.makeText(
                requireContext(),
                "Name, address and description are required.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (latitude == null || longitude == null)
        {
            Toast.makeText(
                requireContext(),
                "Enter valid latitude and longitude values.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val amenities = etAmenities.text
                .toString()
                .split(",")
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }

        val request = UpdateAccommodationRequest(
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            description = description,
            phoneNumber =
                etPhone.text
                    .toString()
                    .trim(),
            amenities = amenities,
            institutionArea =
                etInstitutionArea.text
                    .toString()
                    .trim(),
            isActive = true
        )

        viewModel.updateAccommodation(
            BuildConfig.MANAGER_ACCOMMODATION_ID,
            request
        )
    }
}