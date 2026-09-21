package com.chommiehub.app.ui.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chommiehub.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Hosts the student's Android feature area: Home, Apply, My Applications, Profile and Settings,
 * switched through the bottom navigation bar.
 */
class StudentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_student)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            val destination: Fragment = when (item.itemId) {
                R.id.nav_home -> StudentHomeFragment()
                R.id.nav_apply -> ApplicationFormFragment.newInstance()
                R.id.nav_applications -> MyApplicationsFragment()
                R.id.nav_profile -> StudentProfileFragment()
                R.id.nav_settings -> StudentSettingsFragment()
                else -> StudentHomeFragment()
            }

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.studentContainer, destination)
                .commit()

            true
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }
}
