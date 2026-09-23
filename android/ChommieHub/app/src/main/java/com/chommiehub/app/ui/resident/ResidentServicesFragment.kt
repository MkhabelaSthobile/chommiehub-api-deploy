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

        view.findViewById<View>(R.id.cardResidentInspections).setOnClickListener {
            openFragment(InspectionFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().replace(R.id.studentContainer, fragment).addToBackStack(null).commit()
    }
}