package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.data.model.manager.ManagerAccommodationResponse
import com.chommiehub.app.data.model.manager.SaveAccommodationRequest
import com.chommiehub.app.data.remote.ApiClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Manager onboarding. A newly appointed manager saves a draft and submits
 * it for administrator approval before operational tabs become accessible.
 */
class ManagerSetupFragment : Fragment() {
    private lateinit var content: LinearLayout
    private lateinit var name: EditText
    private lateinit var address: EditText
    private lateinit var area: EditText
    private lateinit var description: EditText
    private lateinit var phone: EditText
    private lateinit var amenities: EditText
    private lateinit var latitude: EditText
    private lateinit var longitude: EditText
    private lateinit var save: Button
    private lateinit var submit: Button
    private lateinit var status: TextView
    private var current: ManagerAccommodationResponse? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(20)
            setPadding(pad, pad, pad, pad)
        }
        return ScrollView(requireContext()).apply { addView(content) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        content.addView(TextView(requireContext()).apply {
            text = "My accommodation"
            textSize = 25f
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        status = TextView(requireContext()).apply {
            text = "Loading your accommodation…"
            textSize = 15f
            setPadding(0, dp(10), 0, dp(12))
        }
        content.addView(status)
        name = input("Residence name")
        address = input("Street address")
        area = input("University / institution area")
        description = input("Describe your accommodation")
        phone = input("Contact phone")
        amenities = input("Amenities separated by commas")
        latitude = input("Latitude (-90 to 90)", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED)
        longitude = input("Longitude (-180 to 180)", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED)
        save = action("Save draft") { saveDraft() }
        submit = action("Submit for administrator review") { submitDraft() }
        action("Check review status") { load() }
        load()
    }

    private fun dp(v: Int) = (resources.displayMetrics.density * v).toInt()

    private fun input(hintText: String, inputTypeValue: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES): EditText =
        EditText(requireContext()).also { field ->
            field.hint = hintText
            field.inputType = inputTypeValue
            field.setPadding(dp(12), dp(12), dp(12), dp(12))
            content.addView(field, LinearLayout.LayoutParams(-1, -2))
        }

    private fun action(textValue: String, callback: () -> Unit): Button =
        Button(requireContext()).also { button ->
            button.text = textValue
            button.isAllCaps = false
            button.setOnClickListener { callback() }
            content.addView(button, LinearLayout.LayoutParams(-1, -2))
        }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = ApiClient.api.getMyAccommodation()
                if (!isAdded) return@launch
                current = result
                CurrentManager.ACCOMMODATION_ID = result.id
                name.setText(result.name)
                address.setText(result.address)
                area.setText(result.institutionArea)
                description.setText(result.description)
                phone.setText(result.phoneNumber)
                amenities.setText(result.amenities.joinToString(", "))
                latitude.setText(result.latitude.toString())
                longitude.setText(result.longitude.toString())
                updateStatus(result)
                if (result.reviewStatus.equals("Approved", true) && !result.isArchived) {
                    (activity as? ManagerActivity)?.refreshWorkflow()
                }
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    status.text = "No accommodation registered. Create a draft below."
                    current = null
                    CurrentManager.ACCOMMODATION_ID = ""
                    save.isEnabled = true
                    submit.isEnabled = false
                } else if (isAdded) toast("Unable to load accommodation (${e.code()})")
            } catch (e: Exception) {
                if (isAdded) toast(e.message ?: "Unable to contact the API")
            }
        }
    }

    private fun updateStatus(result: ManagerAccommodationResponse) {
        val statusName = if (result.isArchived) "Archived" else result.reviewStatus
        status.text = "Status: $statusName" + if (result.reviewReason.isNotBlank()) "\nReason: ${result.reviewReason}" else ""
        val editable = !result.isArchived && (statusName == "Draft" || statusName == "Rejected")
        listOf(name, address, area, description, phone, amenities, latitude, longitude).forEach { it.isEnabled = editable }
        save.isEnabled = editable
        submit.isEnabled = editable
    }

    private fun saveDraft() {
        val title = name.text.toString().trim()
        val location = address.text.toString().trim()
        val institution = area.text.toString().trim()
        val desc = description.text.toString().trim()
        val lat = latitude.text.toString().trim().toDoubleOrNull()
        val lon = longitude.text.toString().trim().toDoubleOrNull()
        if (title.isBlank() || location.isBlank() || institution.isBlank() || desc.isBlank() || lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0 || (lat == 0.0 && lon == 0.0)) {
            toast("Complete all required fields and enter valid map coordinates.")
            return
        }
        val request = SaveAccommodationRequest(
            name = title, address = location, latitude = lat, longitude = lon,
            description = desc, phoneNumber = phone.text.toString().trim(),
            amenities = amenities.text.toString().split(',').map { it.trim() }.filter { it.isNotEmpty() },
            institutionArea = institution
        )
        save.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.api.saveMyAccommodation(request)
                current = response
                CurrentManager.ACCOMMODATION_ID = response.id
                toast("Draft saved. Submit it for review when ready.")
                updateStatus(response)
            } catch (e: Exception) {
                if (isAdded) toast(e.message ?: "Unable to save draft")
                save.isEnabled = true
            }
        }
    }

    private fun submitDraft() {
        if (current?.id.isNullOrBlank()) {
            toast("Save your draft first, then submit.")
            return
        }
        submit.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updated = ApiClient.api.submitMyAccommodation()
                current = updated
                updateStatus(updated)
                toast("Submitted. An administrator must approve this accommodation.")
            } catch (e: Exception) {
                if (isAdded) toast(e.message ?: "Unable to submit listing")
                submit.isEnabled = true
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
