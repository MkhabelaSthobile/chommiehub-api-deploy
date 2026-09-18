package com.chommiehub.app.ui.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R

/**
 * Hosts the student's Android feature area.
 * Authentication will later route signed-in student accounts here.
 */
class StudentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_student)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.studentFragmentContainer,
                    StudentProfileFragment()
                )
                .commit()
        }
    }
}
