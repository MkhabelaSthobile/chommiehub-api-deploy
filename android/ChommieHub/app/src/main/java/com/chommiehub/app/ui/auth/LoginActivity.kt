package com.chommiehub.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private val auth by lazy { FirebaseAuth.getInstance() }

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrBlank()) {
                Log.e(TAG, "Google Sign-In completed without an ID token.")
                toast("Google sign-in could not be completed.")
                return@registerForActivityResult
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    Log.i(TAG, "Google SSO authentication succeeded for Firebase user ${it.user?.uid}.")
                    AuthRouter.route(this)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Firebase rejected the Google credential.", exception)
                    toast(exception.message ?: "Google sign-in failed.")
                }
        } catch (exception: ApiException) {
            Log.e(TAG, "Google Sign-In failed with status ${exception.statusCode}.", exception)

            if (exception.statusCode == CommonStatusCodes.DEVELOPER_ERROR) {
                toast("Google sign-in configuration is incomplete for this app build.")
            } else {
                toast("Google sign-in was cancelled or could not be completed.")
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Unexpected Google Sign-In error.", exception)
            toast(exception.message ?: "Google sign-in failed.")
        }
    }

    // Handles the result of the notification permission request.
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                android.util.Log.d(
                    "ChommieHubFCM",
                    "Notification permission granted"
                )
            } else {
                android.util.Log.d(
                    "ChommieHubFCM",
                    "Notification permission denied"
                )
            }
        }

    private fun requestNotificationPermission() {

        // Runtime permission is required from Android 13 onwards.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permission = Manifest.permission.POST_NOTIFICATIONS

            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        // Ask permission to display ChommieHub notifications.
        requestNotificationPermission()

        // Returning users must verify their identity before
        // accessing their account again.
        if (auth.currentUser != null) {
            startActivity(
                Intent(this, BiometricActivity::class.java)
            )
            finish()
            return
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener { login() }
        findViewById<Button>(R.id.btnGoogle).setOnClickListener { googleSignIn() }

        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<TextView>(R.id.tvBiometric).setOnClickListener {
            startActivity(Intent(this, BiometricActivity::class.java))
        }
    }

    private fun login() {
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Enter a valid email address.")
            return
        }

        if (password.length < 6) {
            toast("Password must be at least 6 characters.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                Log.i(TAG, "Email/password authentication succeeded for Firebase user ${result.user?.uid}.")
                AuthRouter.route(this)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Email/password authentication failed.", exception)
                toast(exception.message ?: "Login failed.")
            }
    }

    private fun googleSignIn() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        Log.i(TAG, "Launching Google SSO flow.")

        googleLauncher.launch(GoogleSignIn.getClient(this, options).signInIntent)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}