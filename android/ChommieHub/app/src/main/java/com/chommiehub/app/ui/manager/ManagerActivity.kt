package com.chommiehub.app.ui.manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chommiehub.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Hosts the residence manager feature area: Home, Applications,
 * Residents, Operations and Profile, switched through the
 * bottom navigation bar.
 */
class ManagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_manager)

        val bottomNav =
            findViewById<BottomNavigationView>(R.id.managerBottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            val destination: Fragment = when (item.itemId) {
                R.id.nav_manager_home -> ManagerDashboardFragment()
                R.id.nav_manager_applications -> ManagerApplicationsFragment()
                R.id.nav_manager_residents -> ManagerResidentsFragment()
                R.id.nav_manager_operations -> ManagerOperationsFragment()
                R.id.nav_manager_profile -> ManagerProfileFragment()
                else -> ManagerDashboardFragment()
            }

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.managerFragmentContainer, destination)
                .commit()

            true
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_manager_home
        }
    }
}