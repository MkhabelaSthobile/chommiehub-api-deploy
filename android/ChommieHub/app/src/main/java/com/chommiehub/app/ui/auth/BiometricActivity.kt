package com.chommiehub.app.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.chommiehub.app.R
import com.google.firebase.auth.FirebaseAuth

class BiometricActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)
        findViewById<Button>(R.id.btnBiometric).setOnClickListener { scan() }
        findViewById<TextView>(R.id.tvUsePassword).setOnClickListener { finish() }
    }

    private fun scan() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, "Sign in with your password once before using biometrics.", Toast.LENGTH_LONG).show()
            return
        }
        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    AuthRouter.route(this@BiometricActivity)
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock ChommieHub")
                .setSubtitle("Confirm your identity to continue")
                .setNegativeButtonText("Use password")
                .build()
        )
    }
}
