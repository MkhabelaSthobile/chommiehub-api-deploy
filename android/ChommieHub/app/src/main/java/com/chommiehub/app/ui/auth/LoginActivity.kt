package com.chommiehub.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { AuthRouter.route(this) }
                .addOnFailureListener { toast(it.message ?: "Google sign-in failed") }
        } catch (e: Exception) {
            toast(e.message ?: "Google sign-in failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
            toast("Enter a valid email address")
            return
        }
        if (password.length < 6) {
            toast("Password must be at least 6 characters")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { AuthRouter.route(this) }
            .addOnFailureListener { toast(it.message ?: "Login failed") }
    }

    private fun googleSignIn() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleLauncher.launch(GoogleSignIn.getClient(this, options).signInIntent)
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
