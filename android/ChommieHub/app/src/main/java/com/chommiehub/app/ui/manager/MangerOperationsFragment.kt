package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chommiehub.app.R

/**
 * Hub screen for the manager's day-to-day operations.
 * Each card opens an existing fragment.
 */
class ManagerOperationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_manager_operations,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.cardManagerMaintenance)
            .setOnClickListener {
                openFragment(ManagerMaintenanceFragment())
            }

        view.findViewById<View>(R.id.cardManagerVisitors)
            .setOnClickListener {
                openFragment(ManagerVisitorRequestsFragment())
            }

        view.findViewById<View>(R.id.cardManagerResidence)
            .setOnClickListener {
                openFragment(ManagerResidenceFragment())
            }
    }

    /**
     * Replaces the operations hub with a sub-screen, keeping
     * the back stack so the system Back button returns here.
     */
    private fun openFragment(fragment: Fragment) {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.managerFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}