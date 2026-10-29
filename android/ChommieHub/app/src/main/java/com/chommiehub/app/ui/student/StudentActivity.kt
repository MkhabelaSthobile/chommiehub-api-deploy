package com.chommiehub.app.ui.student

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.ui.resident.MyResidenceFragment
import com.chommiehub.app.ui.resident.ResidentHomeFragment
import com.chommiehub.app.ui.resident.ResidentServicesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import android.view.View
import android.widget.TextView
import androidx.lifecycle.asLiveData
import com.chommiehub.app.data.network.NetworkMonitor

/**
 * Hosts the student's Android feature area.
 *
 * Applicant accounts use discovery, applications and profile features.
 * Once room assignment changes the backend Student.UserType to Resident,
 * the same authenticated account automatically receives the resident navigation.
 */
class StudentActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "StudentActivity"
    }

    private lateinit var bottomNav: BottomNavigationView
    private var residentMode: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_student)

        bottomNav = findViewById(R.id.bottomNav)

        // Show a banner on every student screen while the phone is offline.
        val tvOfflineBanner = findViewById<TextView>(R.id.tvOfflineBanner)
        NetworkMonitor.getInstance(this).isOnline.asLiveData()
            .observe(this) { isOnline ->
                tvOfflineBanner.visibility = if (isOnline) View.GONE else View.VISIBLE
            }

        configureNavigation(CurrentStudent.USER_TYPE.equals("Resident", ignoreCase = true))
    }

    override fun onResume() {
        super.onResume()
        refreshStudentLifecycle()
    }

    /**
     * Retrieves the latest student profile so an approved applicant can transition
     * into the Resident interface without needing a new account.
     */
    private fun refreshStudentLifecycle() {
        if (CurrentStudent.ID.isBlank()) {
            return
        }

        lifecycleScope.launch {
            try {
                val student = ApiClient.api.getStudent(CurrentStudent.ID)
                CurrentStudent.update(student)

                val shouldUseResidentMode = student.userType.equals("Resident", ignoreCase = true)

                if (residentMode != shouldUseResidentMode) {
                    configureNavigation(shouldUseResidentMode)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to refresh the student's lifecycle state.", exception)
            }
        }
    }

    /**
     * Rebuilds the bottom navigation for either the Applicant or Resident experience.
     */
    private fun configureNavigation(isResident: Boolean) {
        residentMode = isResident

        bottomNav.menu.clear()
        bottomNav.inflateMenu(if (isResident) R.menu.resident_bottom_nav else R.menu.student_bottom_nav)

        bottomNav.setOnItemSelectedListener { item ->
            val destination: Fragment = if (isResident) {
                when (item.itemId) {
                    R.id.nav_resident_home -> ResidentHomeFragment()
                    R.id.nav_my_residence -> MyResidenceFragment()
                    R.id.nav_resident_services -> ResidentServicesFragment()
                    R.id.nav_profile -> StudentProfileFragment()
                    R.id.nav_settings -> StudentSettingsFragment()
                    else -> ResidentHomeFragment()
                }
            } else {
                when (item.itemId) {
                    R.id.nav_home -> StudentHomeFragment()
                    R.id.nav_apply -> ApplicationFormFragment.newInstance()
                    R.id.nav_applications -> MyApplicationsFragment()
                    R.id.nav_profile -> StudentProfileFragment()
                    R.id.nav_settings -> StudentSettingsFragment()
                    else -> StudentHomeFragment()
                }
            }

            supportFragmentManager.beginTransaction().replace(R.id.studentContainer, destination).commit()
            true
        }

        bottomNav.selectedItemId = if (isResident) R.id.nav_resident_home else R.id.nav_home
    }
}