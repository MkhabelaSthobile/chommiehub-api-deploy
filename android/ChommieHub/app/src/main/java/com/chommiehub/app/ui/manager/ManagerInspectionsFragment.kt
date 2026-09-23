package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.model.resident.InspectionResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.remote.ApiErrorMapper
import com.chommiehub.app.data.repository.ResidentServicesRepository
import kotlinx.coroutines.launch

/**
 * Allows residence managers to review and confirm resident move-in and move-out inspections.
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

    /**
     * Loads inspections submitted by residents belonging to the Manager's accommodation.
     */
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

                Log.i(TAG, "Loaded ${inspections.size} resident inspections.")
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to load inspections.", exception)
                showError(ApiErrorMapper.message(exception, "Unable to load resident inspections."))
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Displays the complete inspection history together with each room item's
     * condition, comments and photo-evidence state.
     */
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
                val comments = if (item.comments.isBlank()) "" else "\nComment: ${item.comments}"
                val evidence = if (item.photoReference.isBlank()) "" else "\nPhoto evidence attached"

                card.addView(createText("• ${item.itemName}: ${item.condition}$comments$evidence"))
            }

            if (!inspection.managerConfirmed) {
                val confirmButton = Button(requireContext()).apply {
                    text = "Review and Confirm"
                    setOnClickListener { showConfirmationDialog(inspection.id) }
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

    /**
     * Allows the Manager to add a meaningful review note before confirming the inspection.
     */
    private fun showConfirmationDialog(inspectionId: String) {
        val notesInput = EditText(requireContext()).apply {
            hint = "Manager review notes"
            minLines = 3
            setPadding(24, 18, 24, 18)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Inspection")
            .setMessage("Add a review note before confirming this inspection.")
            .setView(notesInput)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm") { _, _ ->
                val notes = notesInput.text.toString().trim()

                if (notes.isBlank()) {
                    Toast.makeText(requireContext(), "Enter a manager review note.", Toast.LENGTH_SHORT).show()
                } else {
                    confirmInspection(inspectionId, notes)
                }
            }
            .show()
    }

    /**
     * Persists Manager confirmation through the ChommieHub REST API.
     */
    private fun confirmInspection(inspectionId: String, managerNotes: String) {
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repository.confirmInspection(inspectionId, managerNotes)

                Log.i(TAG, "Inspection $inspectionId confirmed.")
                Toast.makeText(requireContext(), "Inspection confirmed.", Toast.LENGTH_SHORT).show()

                loadInspections()
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to confirm inspection.", exception)
                showError(ApiErrorMapper.message(exception, "Unable to confirm the inspection."))
            } finally {
                progressBar.visibility = View.GONE
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