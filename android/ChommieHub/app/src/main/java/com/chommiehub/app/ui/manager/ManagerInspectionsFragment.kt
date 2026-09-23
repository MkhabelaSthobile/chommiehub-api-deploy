package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.model.resident.InspectionResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ResidentServicesRepository
import kotlinx.coroutines.launch

/**
 * Allows residence managers to review and confirm move-in and move-out inspections.
 */
class ManagerInspectionsFragment : Fragment(R.layout.fragment_manager_inspections) {

    companion object {
        private const val TAG = "ManagerInspections"
    }

    private lateinit var container: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var repository: ResidentServicesRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.managerInspectionsContainer)
        progressBar = view.findViewById(R.id.progressManagerInspections)
        tvError = view.findViewById(R.id.tvManagerInspectionsError)
        repository = ResidentServicesRepository(ApiClient.api)

        loadInspections()
    }

    private fun loadInspections() {
        val accommodationId = CurrentManager.ACCOMMODATION_ID

        if (accommodationId.isBlank()) {
            showError("Manager accommodation could not be identified.")
            return
        }

        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val inspections = repository.getManagerInspections(accommodationId)
                showInspections(inspections)
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load inspections.", exception)
                showError(exception.message ?: "Unable to load inspections.")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showInspections(inspections: List<InspectionResponse>) {
        container.removeAllViews()

        if (inspections.isEmpty()) {
            container.addView(createText("No resident inspections have been submitted yet."))
            return
        }

        inspections.forEach { inspection ->
            val card = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card)
                setPadding(24, 24, 24, 24)
            }

            card.addView(createText("${inspection.type} • Room ${inspection.roomId}\n${inspection.items.size} item(s) • ${inspection.status}"))

            inspection.items.forEach { item ->
                val comments = if (item.comments.isBlank()) "" else "\n${item.comments}"
                val evidence = if (item.photoReference.isBlank()) "" else "\nPhoto evidence attached"
                card.addView(createText("• ${item.itemName}: ${item.condition}$comments$evidence"))
            }

            if (!inspection.managerConfirmed) {
                val confirmButton = Button(requireContext()).apply {
                    text = "Confirm Inspection"
                    setOnClickListener { confirmInspection(inspection.id) }
                }

                card.addView(confirmButton)
            } else {
                card.addView(createText("Confirmed by residence management"))

                if (inspection.managerNotes.isNotBlank()) {
                    card.addView(createText("Manager note: ${inspection.managerNotes}"))
                }
            }

            val parameters = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            parameters.bottomMargin = 16
            card.layoutParams = parameters

            container.addView(card)
        }
    }

    private fun confirmInspection(inspectionId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.confirmInspection(inspectionId, "Reviewed and confirmed by the residence manager.")

                Toast.makeText(requireContext(), "Inspection confirmed.", Toast.LENGTH_SHORT).show()

                loadInspections()
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to confirm inspection.", exception)
                showError(exception.message ?: "Unable to confirm inspection.")
            }
        }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun createText(value: String): TextView {
        return TextView(requireContext()).apply {
            text = value
            textSize = 15f
            setTextColor(resources.getColor(R.color.chommie_text_primary, requireContext().theme))
            setPadding(0, 8, 0, 8)
        }
    }
}