package com.chommiehub.app.ui.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R

/**
 * Temporary host Activity used to preview the Student Home screen
 * during development, before app-wide navigation exists.
 */
class StudentHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_student_home)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.studentFragmentContainer,
                    StudentHomeFragment()
                )
                .commit()
        }
    }
}