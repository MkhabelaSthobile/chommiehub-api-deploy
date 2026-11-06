package com.chommiehub.app.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.chommiehub.app.data.model.admin.AdminListingDetailResponse
import com.chommiehub.app.data.model.admin.AdminListingSummary
import com.chommiehub.app.data.model.admin.AdminReasonRequest
import com.chommiehub.app.data.remote.ApiClient

/** Approve, reject, archive, restore or safely delete a residence. */
class AdminListingsFragment : AdminBaseFragment() {
    companion object {
        private const val ARG_STATUS = "status"
        fun newInstance(status: String) = AdminListingsFragment().apply {
            arguments = Bundle().apply { putString(ARG_STATUS, status) }
        }
    }

    private val filters = listOf("All", "Pending", "Approved", "Rejected", "Draft", "Archived")
    private var filter = "All"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        filter = arguments?.getString(ARG_STATUS)?.takeIf { it in filters } ?: "All"
        refresh()
    }

    private fun refresh() {
        clearPage("Accommodation moderation", "Review requests and maintain publication status")
        val selector = Spinner(requireContext())
        selector.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filters)
        selector.setSelection(filters.indexOf(filter))
        content.addView(selector)
        button(content, "Filter / Refresh") {
            filter = filters[selector.selectedItemPosition]
            refresh()
        }
        line(content, "Loading listings…")
        launchTask {
            val listings = ApiClient.api.getAdminListings(filter.takeUnless { it == "All" })
            if (!isAdded) return@launchTask
            clearPage("Accommodation moderation", "Tap a listing for details and available actions")
            val chooser = Spinner(requireContext()).apply {
                adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filters)
                setSelection(filters.indexOf(filter))
            }
            content.addView(chooser)
            button(content, "Apply filter / Refresh") {
                filter = filters[chooser.selectedItemPosition]
                refresh()
            }
            if (listings.isEmpty()) line(content, "No accommodation listings in this category.")
            listings.sortedByDescending { it.submittedAtUtc.orEmpty() }.forEach { listing ->
                card(listing.name, "${listing.institutionArea}  •  ${listing.reviewStatus}${if (listing.isArchived) " · Archived" else ""}\nManager: ${listing.managerName}\n${listing.address}") {
                    loadDetail(listing)
                }
            }
        }
    }

    private fun loadDetail(listing: AdminListingSummary) {
        launchTask {
            val detail = ApiClient.api.getAdminListing(listing.id)
            if (!isAdded) return@launchTask
            presentDetail(listing, detail)
        }
    }

    private fun presentDetail(listing: AdminListingSummary, detail: AdminListingDetailResponse) {
        val a = detail.accommodation
        val description = """
            ${a.name}
            ${a.address} · ${a.institutionArea}

            ${a.description}
            Amenities: ${a.amenities.joinToString()}
            Map: ${a.latitude}, ${a.longitude}
            Contact: ${a.phoneNumber}
            Status: ${a.reviewStatus}${if (a.isArchived) " (Archived)" else ""}
            Review reason: ${a.reviewReason.ifBlank { "None" }}
            Manager: ${detail.manager?.name.orEmpty()} ${detail.manager?.surname.orEmpty()}
            Rooms: ${detail.rooms.size} | Residents: ${detail.residents.size} | Applications: ${detail.applicationCount}
            Image references: ${a.imageUrls.size}
        """.trimIndent()

        val options = when {
            a.isArchived -> arrayOf("Unarchive", "Delete", "Close")
            a.reviewStatus.equals("Pending", true) -> arrayOf("Approve", "Reject", "Close")
            a.reviewStatus.equals("Approved", true) -> arrayOf("Archive", "Close")
            else -> arrayOf("Close")
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Accommodation details")
            .setMessage(description)
            .setItems(options) { _, which ->
                when (options[which]) {
                    "Approve" -> confirm("Approve ${a.name}", "Publish this listing to students?") {
                        performAction { ApiClient.api.approveAdminListing(listing.id) }
                    }
                    "Reject" -> askReason("Reject ${a.name}") { reason ->
                        performAction { ApiClient.api.rejectAdminListing(listing.id, AdminReasonRequest(reason)) }
                    }
                    "Archive" -> askReason("Archive ${a.name}") { reason ->
                        performAction { ApiClient.api.archiveAdminListing(listing.id, AdminReasonRequest(reason)) }
                    }
                    "Unarchive" -> confirm("Restore listing", "Return this listing to its previous review state?") {
                        performAction { ApiClient.api.unarchiveAdminListing(listing.id) }
                    }
                    "Delete" -> askReason("Permanently delete ${a.name}") { reason ->
                        confirm("Final confirmation", "Deletion is permanent and allowed only when no rooms, applications or residents exist.") {
                            performAction { ApiClient.api.deleteAdminListing(listing.id, AdminReasonRequest(reason)) }
                        }
                    }
                }
            }.setNegativeButton("Close", null).show()
    }

    private fun performAction(action: suspend () -> com.chommiehub.app.data.model.admin.AdminActionResponse) {
        launchTask {
            val result = action()
            if (!isAdded) return@launchTask
            toast(result.message)
            refresh()
        }
    }
}
