
package com.chommiehub.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.firebase.FcmTokenManager
import com.chommiehub.app.ui.manager.CurrentManager
import com.chommiehub.app.ui.student.CurrentStudent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class BiometricActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BiometricActivity"
    }

    private val auth = FirebaseAuth.getInstance()

    private var isReturningToLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)

        findViewById<Button>(R.id.btnBiometric).setOnClickListener {
            checkBiometricAvailability()
        }

        findViewById<TextView>(R.id.tvUsePassword).setOnClickListener {
            returnToLogin()
        }
    }

    private fun checkBiometricAvailability() {

        if (auth.currentUser == null) {
            returnToLogin()
            return
        }

        val biometricManager = BiometricManager.from(this)

        val result = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )

        when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                scan()
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showMessage(
                    "This device does not support biometric authentication."
                )
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showMessage(
                    "Biometric authentication is temporarily unavailable."
                )
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showMessage(
                    "Please set up fingerprint or face recognition in your phone settings."
                )
            }

            else -> {
                showMessage(
                    "Biometric authentication is unavailable. Use password instead."
                )
            }
        }
    }

    private fun scan() {

        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    if (!isReturningToLogin) {
                        AuthRouter.route(this@BiometricActivity)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    showMessage(
                        "Biometric verification failed. Please try again."
                    )
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        returnToLogin()
                    } else if (
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        showMessage("Authentication error: $errString")
                    }
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock ChommieHub")
            .setSubtitle("Verify your identity to continue")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .setNegativeButtonText("Use password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun returnToLogin() {

        if (isReturningToLogin) {
            return
        }

        isReturningToLogin = true

        findViewById<TextView>(R.id.tvUsePassword).isEnabled = false
        findViewById<Button>(R.id.btnBiometric).isEnabled = false

        lifecycleScope.launch {

            // Unregister the device while the Firebase session
            // is still active.
            val removed =
                FcmTokenManager.unregisterCurrentDevice()

            if (!removed) {
                Log.w(
                    TAG,
                    "FCM cleanup was not confirmed before sign-out."
                )
            }

            // End the cached Firebase session.
            auth.signOut()

            // Clear cached identity information.
            CurrentStudent.clear()
            CurrentManager.ID = ""
            CurrentManager.ACCOMMODATION_ID = ""

            val intent = Intent(
                this@BiometricActivity,
                LoginActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
