package com.chommiehub.app.ui.admin

import android.os.Bundle
import android.view.View
import com.chommiehub.app.data.remote.ApiClient

/** Live platform metrics and shortcuts into moderation and the audit trail. */
class AdminDashboardFragment : AdminBaseFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
    }

    private fun refresh() {
        clearPage("Platform overview", "Live information from the secured ChommieHub API")
        line(content, "Loading dashboard…")
        launchTask {
            val data = ApiClient.api.getAdminDashboard()
            if (!isAdded) return@launchTask
            clearPage("Platform overview", "Latest student, manager and listing counts")
            button(content, "Refresh dashboard") { refresh() }
            card("Users", "Students: ${data.students}  •  Residents: ${data.residents}\nManagers: ${data.managers}")
            card("Accommodation", "Total: ${data.listingsTotal}\nDraft: ${data.listingsDraft}  •  Pending: ${data.listingsPending}\nApproved: ${data.listingsApproved}  •  Rejected: ${data.listingsRejected}  •  Archived: ${data.listingsArchived}") {
                (activity as? AdminActivity)?.openListings("Pending")
            }
            card("Applications", "All applications: ${data.applicationsTotal}\nOpen: ${data.applicationsOpen}")
            button(content, "Review ${data.listingsPending} pending listing(s)") {
                (activity as? AdminActivity)?.openListings("Pending")
            }
            // Member 4: platform incident oversight, announcements and aggregate reporting.
            listOf(
                getString(com.chommiehub.app.R.string.community_admin_incidents) to com.chommiehub.app.ui.community.CommunityActivity.INCIDENTS,
                getString(com.chommiehub.app.R.string.community_admin_announcements) to com.chommiehub.app.ui.community.CommunityActivity.ANNOUNCEMENTS,
                getString(com.chommiehub.app.R.string.community_admin_reports) to com.chommiehub.app.ui.community.CommunityActivity.REPORTS
            ).forEach { (label, mode) ->
                button(content, label) {
                    startActivity(android.content.Intent(requireContext(),
                        com.chommiehub.app.ui.community.CommunityActivity::class.java)
                        .putExtra(com.chommiehub.app.ui.community.CommunityActivity.MODE, mode))
                }
            }
            heading(content, "Recent administrator actions")
            if (data.recentActivity.isEmpty()) line(content, "No recorded actions yet.")
            data.recentActivity.take(6).forEach { record ->
                card(record.action, "${record.targetName}\n${record.createdAtUtc}\n${record.reason}")
            }
            button(content, "View complete audit log") { (activity as? AdminActivity)?.openAudit() }
        }
    }
}
