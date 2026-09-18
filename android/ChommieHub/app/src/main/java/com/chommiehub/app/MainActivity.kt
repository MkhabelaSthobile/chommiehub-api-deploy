package com.chommiehub.app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.HealthRepository
import kotlinx.coroutines.launch
import android.content.Intent
import com.chommiehub.app.ui.manager.ManagerActivity
import com.chommiehub.app.ui.student.StudentActivity

/**
 * Temporary integration screen used to verify communication between the native Android application and the custom ChommieHub REST API.
 * This screen will be replaced by the role-based application flow once backend connectivity has been verified.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChommieHub.Main"
    }

    private lateinit var tvApiStatus: TextView
    private lateinit var btnRetry: Button

    private lateinit var btnOpenManager: Button

    private lateinit var btnOpenStudentProfile: Button

    private val healthRepository by lazy {
        HealthRepository(ApiClient.api)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity created.")

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->
            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        tvApiStatus = findViewById(R.id.tvApiStatus)
        btnRetry = findViewById(R.id.btnRetry)
        btnOpenManager = findViewById(R.id.btnOpenManager)
        btnOpenStudentProfile = findViewById(R.id.btnOpenStudentProfile)

        btnRetry.setOnClickListener {
            Log.d(TAG, "Manual API connection retry requested.")
            checkApiConnection()
        }

        btnOpenManager.setOnClickListener {
            Log.d(
                TAG,
                "Opening residence manager feature area."
            )

            startActivity(
                Intent(
                    this,
                    ManagerActivity::class.java
                )
            )
        }

        btnOpenStudentProfile.setOnClickListener {
            Log.d(
                TAG,
                "Opening student profile feature area."
            )

            startActivity(
                Intent(
                    this,
                    StudentActivity::class.java
                )
            )
        }

        checkApiConnection()
    }

    /**
     * Calls the custom backend health endpoint without blocking the UI thread and provides visible and logged connection feedback.
     */
    private fun checkApiConnection() {
        tvApiStatus.text = getString(R.string.api_checking)
        btnRetry.isEnabled = false

        lifecycleScope.launch {
            try {
                val health = healthRepository.getHealth()

                tvApiStatus.text =
                    getString(
                        R.string.api_connected,
                        health.service,
                        health.status
                    )

                Log.i(
                    TAG,
                    "API connection successful. Service=${health.service}, " +
                            "status=${health.status}"
                )
            } catch (exception: Exception) {
                tvApiStatus.text =
                    getString(R.string.api_connection_failed)

                Log.e(
                    TAG,
                    "Unable to connect to ChommieHub API.",
                    exception
                )
            } finally {
                btnRetry.isEnabled = true
            }
        }
    }
}