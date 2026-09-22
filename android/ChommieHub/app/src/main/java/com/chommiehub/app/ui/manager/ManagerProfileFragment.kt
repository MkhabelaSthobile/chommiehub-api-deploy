package com.chommiehub.app.ui.manager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.ManagerResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ManagerRepository
import com.chommiehub.app.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

/**
 * Displays the signed-in manager's profile information and
 * provides the sign-out action.
 */
class ManagerProfileFragment : Fragment() {

    companion object {
        private const val TAG = "ManagerProfile"
    }

    private lateinit var viewModel: ManagerProfileViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var content: View

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAccommodation: TextView
    private lateinit var btnSignOut: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_profile,
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
        configureClickListeners()
        observeState()
        loadProfile()
    }

    private fun bindViews(view: View) {
        progressBar = view.findViewById(R.id.progressManagerProfile)
        tvError = view.findViewById(R.id.tvManagerProfileError)
        content = view.findViewById(R.id.managerProfileContent)

        tvName = view.findViewById(R.id.tvManagerProfileName)
        tvEmail = view.findViewById(R.id.tvManagerProfileEmail)
        tvPhone = view.findViewById(R.id.tvManagerProfilePhone)
        tvAccommodation = view.findViewById(R.id.tvManagerProfileAccommodation)
        btnSignOut = view.findViewById(R.id.btnManagerSignOut)
    }

    private fun configureViewModel() {
        val repository = ManagerRepository(ApiClient.api)
        val factory = ManagerProfileViewModel.Factory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[ManagerProfileViewModel::class.java]
    }

    private fun configureClickListeners() {
        btnSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun observeState() {
        viewModel.manager.observe(viewLifecycleOwner) { manager ->
            if (manager != null) showManager(manager)
        }

        viewModel.accommodation.observe(viewLifecycleOwner) { accommodation ->
            if (accommodation != null) showAccommodation(accommodation)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                tvError.text = message
                tvError.visibility = View.VISIBLE
                content.visibility = View.GONE
                viewModel.consumeError()
            } else {
                tvError.visibility = View.GONE
            }
        }
    }

    private fun loadProfile() {
        val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
        if (firebaseUid.isNullOrBlank()) {
            tvError.text = "You are not signed in."
            tvError.visibility = View.VISIBLE
            return
        }

        // Try to render whatever AuthRouter already cached, then
        // fetch fresh data from the API.
        if (CurrentManager.ACCOMMODATION_ID.isNotBlank()) {
            viewModel.loadAccommodation(CurrentManager.ACCOMMODATION_ID)
        }
        viewModel.loadManager(firebaseUid)
    }

    private fun showManager(manager: ManagerResponse) {
        content.visibility = View.VISIBLE

        tvName.text = "${manager.name} ${manager.surname}"
        tvEmail.text = manager.email
        tvPhone.text = if (manager.phoneNumber.isBlank()) {
            "No phone number on file"
        } else {
            manager.phoneNumber
        }
    }

    private fun showAccommodation(accommodation: AccommodationResponse) {
        tvAccommodation.text = "Assigned residence: ${accommodation.name}"
    }

    private fun signOut() {
        Log.i(TAG, "Manager signing out.")

        FirebaseAuth.getInstance().signOut()
        CurrentManager.ID = ""
        CurrentManager.ACCOMMODATION_ID = ""

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}