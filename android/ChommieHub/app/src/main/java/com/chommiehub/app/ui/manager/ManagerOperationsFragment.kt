package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.chommiehub.app.R

/**
 * Provides access to the residence manager's day-to-day operational features.
 *
 * The Operations screen acts as a hub for maintenance requests, visitor requests,
 * digital room inspections and residence/room management.
 */
class ManagerOperationsFragment : Fragment(R.layout.fragment_manager_operations) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cardManagerMaintenance).setOnClickListener {
            openFragment(ManagerMaintenanceFragment())
        }

        view.findViewById<View>(R.id.cardManagerVisitors).setOnClickListener {
            openFragment(ManagerVisitorRequestsFragment())
        }

        // Part 1: managers review residence incidents, publish notices and view reports.
        val operationsPanel = (view as androidx.core.widget.NestedScrollView).getChildAt(0) as android.widget.LinearLayout
        listOf(
            getString(R.string.community_manager_incidents) to com.chommiehub.app.ui.community.CommunityActivity.INCIDENTS,
            getString(R.string.community_manager_announcements) to com.chommiehub.app.ui.community.CommunityActivity.ANNOUNCEMENTS,
            getString(R.string.community_manager_reports) to com.chommiehub.app.ui.community.CommunityActivity.REPORTS
        ).forEach { (label, mode) ->
            operationsPanel.addView(android.widget.Button(requireContext()).apply {
                text = label; isAllCaps = false
                setOnClickListener {
                    startActivity(android.content.Intent(requireContext(),
                        com.chommiehub.app.ui.community.CommunityActivity::class.java)
                        .putExtra(com.chommiehub.app.ui.community.CommunityActivity.MODE, mode))
                }
            })
        }

        view.findViewById<View>(R.id.cardManagerInspections).setOnClickListener {
            openFragment(ManagerInspectionsFragment())
        }

        view.findViewById<View>(R.id.cardManagerResidence).setOnClickListener {
            openFragment(ManagerResidenceFragment())
        }
    }

    /**
     * Opens the selected operational feature while retaining the Operations
     * screen on the back stack.
     */
    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.managerFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}