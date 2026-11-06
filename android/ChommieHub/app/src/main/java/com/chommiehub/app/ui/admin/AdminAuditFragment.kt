package com.chommiehub.app.ui.admin

import android.os.Bundle
import android.view.View
import com.chommiehub.app.data.remote.ApiClient

/** Read-only time-ordered audit trail for administrator decisions. */
class AdminAuditFragment : AdminBaseFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
    }

    private fun refresh() {
        clearPage("Audit log", "Who changed what, when, and why")
        button(content, "Refresh audit records") { refresh() }
        line(content, "Loading activity…")
        launchTask {
            val entries = ApiClient.api.getAdminAudit(100)
            if (!isAdded) return@launchTask
            clearPage("Audit log", "Latest 100 administrative actions")
            button(content, "Refresh audit records") { refresh() }
            if (entries.isEmpty()) line(content, "No administrator activity recorded yet.")
            entries.forEach { entry ->
                card(entry.action, "${entry.targetType}: ${entry.targetName}\nAt: ${entry.createdAtUtc}\nAdministrator: ${entry.adminId}\nReason: ${entry.reason.ifBlank { "Not required" }}")
            }
        }
    }
}
