package com.chommiehub.app.ui.resident

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.chommiehub.app.R

/**
 * Provides the Resident with access to day-to-day residence services.
 */
class ResidentServicesFragment : Fragment(R.layout.fragment_resident_services) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cardResidentMaintenance).setOnClickListener {
            openFragment(MaintenanceFragment())
        }

        view.findViewById<View>(R.id.cardResidentVisitors).setOnClickListener {
            openFragment(VisitorRequestFragment())
        }

        // Part 1 resident operations: incident reporting and residence notices.
        val servicesPanel = (view as android.widget.ScrollView).getChildAt(0) as android.widget.LinearLayout
        listOf(
            getString(R.string.community_resident_incidents) to com.chommiehub.app.ui.community.CommunityActivity.INCIDENTS,
            getString(R.string.community_announcements) to com.chommiehub.app.ui.community.CommunityActivity.ANNOUNCEMENTS
        ).forEach { (label, mode) ->
            servicesPanel.addView(android.widget.Button(requireContext()).apply {
                text = label; isAllCaps = false
                setOnClickListener {
                    startActivity(android.content.Intent(requireContext(),
                        com.chommiehub.app.ui.community.CommunityActivity::class.java)
                        .putExtra(com.chommiehub.app.ui.community.CommunityActivity.MODE, mode))
                }
            })
        }

        view.findViewById<View>(R.id.cardResidentInspections).setOnClickListener {
            openFragment(InspectionFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.studentContainer, fragment).addToBackStack(null).commit()
    }
}