package com.chommiehub.app.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.chommiehub.app.data.model.admin.AdminReasonRequest
import com.chommiehub.app.data.model.admin.AdminUserSummary
import com.chommiehub.app.data.model.manager.CreateManagerRequest
import com.chommiehub.app.data.remote.ApiClient

/** Account search, suspension/reactivation, safe deletion and manager appointment. */
class AdminAccountsFragment : AdminBaseFragment() {
    private var role = "All"
    private var search = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
    }

    private fun refresh() {
        clearPage("Accounts & access", "Administrative actions are verified and recorded by the API")
        val filter = Spinner(requireContext())
        val roles = listOf("All", "Student", "Resident", "Manager")
        filter.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        filter.setSelection(roles.indexOf(role).coerceAtLeast(0))
        content.addView(filter)
        val query = field(content, "Find by name or email", search)
        button(content, "Search / Refresh") {
            role = roles[filter.selectedItemPosition]
            search = query.text.toString().trim()
            refresh()
        }
        button(content, "Appoint a residence manager") { appointmentDialog() }
        line(content, "Loading accounts…")
        launchTask {
            val accounts = ApiClient.api.getAdminUsers(role.takeUnless { it == "All" }, search.ifBlank { null })
            if (!isAdded) return@launchTask
            // Keep the search controls while replacing the temporary loading message.
            val loading = content.getChildAt(content.childCount - 1)
            if (loading is android.widget.TextView) content.removeView(loading)
            if (accounts.isEmpty()) line(content, "No matching accounts.")
            accounts.forEach { user ->
                card("${user.name} ${user.surname}", "${user.email}\n${user.role} · ${if (user.isDisabled) "Disabled" else "Active"}${if (user.accommodationName.isNotBlank()) "\n${user.accommodationName}" else ""}") {
                    loadUser(user)
                }
            }
        }
    }

    private fun field(parent: LinearLayout, hint: String, value: String = ""): EditText =
        EditText(requireContext()).apply {
            this.hint = hint
            setText(value)
            minHeight = dp(48)
            parent.addView(this, LinearLayout.LayoutParams(-1, -2))
        }

    private fun loadUser(user: AdminUserSummary) {
        launchTask {
            val detail = ApiClient.api.getAdminUser(user.id)
            if (!isAdded) return@launchTask
            val profile = detail.user
            val message = """
                ${profile.name} ${profile.surname}
                ${profile.email}
                Phone: ${profile.phoneNumber}
                Role: ${profile.role}
                Account: ${if (profile.isDisabled) "Disabled" else "Active"}
                Accommodation: ${profile.accommodationName.ifBlank { "Not assigned" }}
                Applications: ${detail.applications.size}
                Created: ${profile.createdAtUtc}
            """.trimIndent()
            val options = if (profile.isDisabled) arrayOf("Enable", "Delete", "Close") else arrayOf("Disable", "Delete", "Close")
            AlertDialog.Builder(requireContext()).setTitle("Account details").setMessage(message)
                .setItems(options) { _, which ->
                    when (options[which]) {
                        "Enable" -> confirm("Enable account", "Restore access to ${profile.email}?") {
                            execute { ApiClient.api.enableAdminUser(profile.id) }
                        }
                        "Disable" -> askReason("Disable ${profile.email}") { reason ->
                            execute { ApiClient.api.disableAdminUser(profile.id, AdminReasonRequest(reason)) }
                        }
                        "Delete" -> askReason("Delete ${profile.email}") { reason ->
                            confirm("Final confirmation", "Only accounts without dependent records may be deleted. Otherwise, disable instead.") {
                                execute { ApiClient.api.deleteAdminUser(profile.id, AdminReasonRequest(reason)) }
                            }
                        }
                    }
                }.setNegativeButton("Close", null).show()
        }
    }

    private fun execute(action: suspend () -> com.chommiehub.app.data.model.admin.AdminActionResponse) {
        launchTask {
            val result = action()
            if (!isAdded) return@launchTask
            toast(result.message)
            refresh()
        }
    }

    private fun appointmentDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }
        val uid = field(layout, "Existing Firebase UID")
        val name = field(layout, "First name")
        val surname = field(layout, "Surname")
        val email = field(layout, "Firebase account email")
        val phone = field(layout, "Contact phone")
        val scroll = android.widget.ScrollView(requireContext()).apply { addView(layout) }
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Appoint manager")
            .setMessage("Create the manager's Firebase Authentication user in Firebase Console first. Copy the user's UID here. The API checks that the UID exists and that the email matches.")
            .setView(scroll)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Appoint", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val fields = listOf(uid, name, surname, email, phone)
                if (fields.any { it.text.toString().trim().isEmpty() }) {
                    fields.firstOrNull { it.text.toString().trim().isEmpty() }?.error = "Required"
                    return@setOnClickListener
                }
                val request = CreateManagerRequest(
                    firebaseUid = uid.text.toString().trim(),
                    name = name.text.toString().trim(),
                    surname = surname.text.toString().trim(),
                    email = email.text.toString().trim(),
                    phoneNumber = phone.text.toString().trim(),
                    accommodationId = ""
                )
                dialog.dismiss()
                launchTask {
                    val result = ApiClient.api.appointManager(request)
                    if (!isAdded) return@launchTask
                    toast("Manager ${result.name} ${result.surname} appointed successfully.")
                    role = "Manager"
                    refresh()
                }
            }
        }
        dialog.show()
    }
}
