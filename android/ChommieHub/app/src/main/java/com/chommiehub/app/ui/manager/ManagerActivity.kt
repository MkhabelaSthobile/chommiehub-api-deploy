package com.chommiehub.app.ui.manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R

/**
 * Hosts the residence manager's Android feature area
 * Authentication will later route authorised manager accounts here.
 */
class ManagerActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_manager
        )

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