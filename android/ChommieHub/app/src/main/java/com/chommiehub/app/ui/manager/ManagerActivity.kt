package com.chommiehub.app.ui.manager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Residence manager home. The review-state gate prevents pending, draft and
 * rejected listings from granting access to operational management screens.
 */
class ManagerActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private var approved: Boolean? = null
    private var checking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)
        bottomNav = findViewById(R.id.managerBottomNav)
        bottomNav.menu.clear()
        bottomNav.setOnItemSelectedListener { item ->
            val page: Fragment = when (item.itemId) {
                R.id.nav_manager_home -> if (approved == true) ManagerDashboardFragment() else ManagerSetupFragment()
                R.id.nav_manager_setup -> ManagerSetupFragment()
                R.id.nav_manager_applications -> if (approved == true) ManagerApplicationsFragment() else ManagerSetupFragment()
                R.id.nav_manager_residents -> if (approved == true) ManagerResidentsFragment() else ManagerSetupFragment()
                R.id.nav_manager_operations -> if (approved == true) ManagerOperationsFragment() else ManagerSetupFragment()
                R.id.nav_manager_profile -> ManagerProfileFragment()
                else -> ManagerSetupFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.managerFragmentContainer, page)
                .commit()
            true
        }
        refreshWorkflow()
    }

    override fun onResume() {
        super.onResume()
        if (approved != null) refreshWorkflow()
    }

    /** Check the server, not a cached accommodation ID, before unlocking tabs. */
    fun refreshWorkflow() {
        if (checking) return
        checking = true
        lifecycleScope.launch {
            try {
                val listing = try {
                    ApiClient.api.getMyAccommodation()
                } catch (e: HttpException) {
                    if (e.code() == 404) null else throw e
                }
                CurrentManager.ACCOMMODATION_ID = listing?.id.orEmpty()
                val permitted = listing != null && listing.reviewStatus.equals("Approved", true) && !listing.isArchived
                if (approved != permitted || bottomNav.menu.size() == 0) {
                    approved = permitted
                    bottomNav.menu.clear()
                    bottomNav.inflateMenu(if (permitted) R.menu.manager_bottom_nav else R.menu.manager_review_nav)
                    bottomNav.selectedItemId = R.id.nav_manager_home
                }
            } catch (e: Exception) {
                Log.e("ManagerActivity", "Unable to check manager's listing approval status", e)
                // Fail closed: do not retain privileged operational tabs when status
                // cannot be checked; manager may retry after reconnecting.
                approved = false
                bottomNav.menu.clear()
                bottomNav.inflateMenu(R.menu.manager_review_nav)
                bottomNav.selectedItemId = R.id.nav_manager_home
            } finally {
                checking = false
            }
        }
    }
}
