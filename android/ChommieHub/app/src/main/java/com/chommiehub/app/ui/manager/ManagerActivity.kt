package com.chommiehub.app.ui.manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R

/**
 * Hosts the existing residence manager feature area.
 *
 * Authorised manager accounts are routed to this Activity
 * after authentication.
 */
class ManagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_manager)

        // Only load the dashboard when the Activity is first created.
        // This prevents duplicate fragments after configuration changes.
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.managerFragmentContainer,
                    ManagerDashboardFragment()
                )
                .commit()
        }
    }
}