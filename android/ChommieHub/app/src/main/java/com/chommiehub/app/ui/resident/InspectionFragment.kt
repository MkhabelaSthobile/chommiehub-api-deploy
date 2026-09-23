package com.chommiehub.app.ui.resident

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.chommiehub.app.R
import com.chommiehub.app.data.model.resident.InspectionItemRequest
import com.chommiehub.app.data.model.resident.InspectionResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.ResidentServicesRepository
import com.chommiehub.app.ui.student.CurrentStudent

/**
 * Provides the resident move-in and move-out inspection workflow.
 *
 * Residents can record room items, choose each item's condition, enter comments,
 * optionally select photo evidence and submit the completed inspection through
 * the ChommieHub REST API.
 *
 * For Part 2, the selected image URI is stored as an evidence reference.
 * Dedicated cloud/blob image storage can replace this mechanism during the Final POE.
 */
class InspectionFragment : Fragment(R.layout.fragment_inspection) {

    private lateinit var viewModel: InspectionViewModel

    private val pendingItems = mutableListOf<InspectionItemRequest>()

    private var selectedPhotoUri: Uri? = null

    private lateinit var spType: Spinner
    private lateinit var spCondition: Spinner
    private lateinit var etItem: EditText
    private lateinit var etComments: EditText
    private lateinit var tvPhoto: TextView
    private lateinit var itemsContainer: LinearLayout
    private lateinit var historyContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var btnSubmit: Button

    /**
     * Opens the Android document picker so the resident can select optional
     * photographic evidence for an inspection item.
     */
    private val photoPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedPhotoUri = uri
        tvPhoto.text = if (uri == null) "No photo selected" else "Photo evidence selected"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spType = view.findViewById(R.id.spInspectionType)
        spCondition = view.findViewById(R.id.spInspectionCondition)
        etItem = view.findViewById(R.id.etInspectionItem)
        etComments = view.findViewById(R.id.etInspectionComments)
        tvPhoto = view.findViewById(R.id.tvInspectionPhoto)
        itemsContainer = view.findViewById(R.id.inspectionItemsContainer)
        historyContainer = view.findViewById(R.id.inspectionHistoryContainer)
        progressBar = view.findViewById(R.id.progressInspection)
        tvError = view.findViewById(R.id.tvInspectionError)
        btnSubmit = view.findViewById(R.id.btnSubmitInspection)

        spType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Move-In", "Move-Out"))
        spCondition.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Good", "Fair", "Damaged", "Needs attention"))

        val repository = ResidentServicesRepository(ApiClient.api)

        viewModel = ViewModelProvider(this, InspectionViewModel.Factory(repository))[InspectionViewModel::class.java]

        view.findViewById<Button>(R.id.btnSelectInspectionPhoto).setOnClickListener {
            photoPicker.launch("image/*")
        }

        view.findViewById<Button>(R.id.btnAddInspectionItem).setOnClickListener {
            addItem()
        }

        btnSubmit.setOnClickListener {
            viewModel.submit(spType.selectedItem.toString(), pendingItems.toList())
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnSubmit.isEnabled = !loading
        }

        viewModel.inspections.observe(viewLifecycleOwner) { inspections ->
            showHistory(inspections)
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                pendingItems.clear()
                selectedPhotoUri = null
                tvPhoto.text = "No photo selected"

                showPendingItems()
                viewModel.consumeMessage()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message.isNullOrBlank()) {
                tvError.visibility = View.GONE
            } else {
                tvError.text = message
                tvError.visibility = View.VISIBLE
                viewModel.consumeError()
            }
        }

        val studentId = CurrentStudent.ID

        if (studentId.isBlank()) {
            tvError.text = "You must be signed in to access inspections."
            tvError.visibility = View.VISIBLE
        } else {
            viewModel.loadResident(studentId)
        }

        showPendingItems()
    }

    /**
     * Adds the currently entered room item to the pending inspection.
     */
    private fun addItem() {
        val itemName = etItem.text.toString().trim()

        if (itemName.isBlank()) {
            tvError.text = "Enter the room item you inspected."
            tvError.visibility = View.VISIBLE
            return
        }

        val item = InspectionItemRequest(
            itemName = itemName,
            condition = spCondition.selectedItem.toString(),
            comments = etComments.text.toString().trim(),
            photoReference = selectedPhotoUri?.toString().orEmpty()
        )

        pendingItems.add(item)

        etItem.text.clear()
        etComments.text.clear()
        selectedPhotoUri = null
        tvPhoto.text = "No photo selected"
        tvError.visibility = View.GONE

        showPendingItems()
    }

    /**
     * Displays all room items that will be included when the inspection is submitted.
     */
    private fun showPendingItems() {
        itemsContainer.removeAllViews()

        if (pendingItems.isEmpty()) {
            itemsContainer.addView(createCardText("No items added yet."))
            return
        }

        pendingItems.forEachIndexed { index, item ->
            val comments = if (item.comments.isBlank()) "" else "\n${item.comments}"
            val evidence = if (item.photoReference.isBlank()) "" else "\nPhoto evidence selected"

            val row = createCardText("${item.itemName} — ${item.condition}$comments$evidence\nTap to remove")

            row.setOnClickListener {
                pendingItems.removeAt(index)
                showPendingItems()
            }

            itemsContainer.addView(row)
        }
    }

    /**
     * Displays previously submitted inspections and their manager-confirmation state.
     */
    private fun showHistory(inspections: List<InspectionResponse>) {
        historyContainer.removeAllViews()

        if (inspections.isEmpty()) {
            historyContainer.addView(createCardText("No inspections submitted yet."))
            return
        }

        inspections.forEach { inspection ->
            val confirmation = if (inspection.managerConfirmed) "Manager confirmed" else "Awaiting manager confirmation"
            val managerNote = if (inspection.managerNotes.isBlank()) "" else "\nManager note: ${inspection.managerNotes}"

            historyContainer.addView(
                createCardText(
                    "${inspection.type} • ${inspection.status}\n${inspection.items.size} item(s) • $confirmation$managerNote"
                )
            )
        }
    }

    /**
     * Creates a reusable card-like text component for inspection information.
     */
    private fun createCardText(value: String): TextView {
        return TextView(requireContext()).apply {
            text = value
            textSize = 15f
            setTextColor(resources.getColor(R.color.chommie_text_primary, requireContext().theme))
            setBackgroundResource(R.drawable.bg_card)
            setPadding(24, 24, 24, 24)

            val parameters = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            parameters.bottomMargin = 12
            layoutParams = parameters
        }
    }
}