
package com.chommiehub.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.ui.manager.CurrentManager
import com.chommiehub.app.ui.student.CurrentStudent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private lateinit var btnRegister: Button

    private var registrationInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            register()
        }

        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            if (!registrationInProgress) {
                finish()
            }
        }
    }

    private fun register() {
        if (registrationInProgress) return

        fun getValue(id: Int): String {
            return findViewById<EditText>(id)
                .text
                .toString()
                .trim()
        }

        val name = getValue(R.id.etName)
        val surname = getValue(R.id.etSurname)
        val email = getValue(R.id.etEmail)
        val phone = getValue(R.id.etPhone)
        val password = getValue(R.id.etPassword)
        val confirmPassword = getValue(R.id.etConfirmPassword)

        val isManager =
            findViewById<RadioGroup>(R.id.rgRole)
                .checkedRadioButtonId == R.id.rbManager

        if (isManager) {
            toast(
                "Manager accounts must be created by an administrator."
            )
            return
        }

        if (
            name.isBlank() ||
            surname.isBlank() ||
            phone.isBlank()
        ) {
            toast("Complete all required fields")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Enter a valid email")
            return
        }

        if (password.length < 6) {
            toast("Password must be at least 6 characters")
            return
        }

        if (password != confirmPassword) {
            toast("Passwords do not match")
            return
        }

        registrationInProgress = true
        btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(
            email,
            password
        ).addOnSuccessListener { result ->

            val firebaseUid = result.user?.uid

            if (firebaseUid == null) {
                registrationInProgress = false
                btnRegister.isEnabled = true
                toast("Could not retrieve Firebase user ID")
                return@addOnSuccessListener
            }

            lifecycleScope.launch {
                try {
                    val request = CreateStudentRequest(
                        firebaseUid = firebaseUid,
                        name = name,
                        surname = surname,
                        email = email,
                        phoneNumber = phone,
                        preferredLanguage = "English"
                    )

                    // Create the student profile through the API.
                    ApiClient.api.createStudent(request)

                    toast(
                        "Registration successful. Please sign in."
                    )

                } catch (exception: Exception) {
                    toast(
                        "Account created, but profile setup may be incomplete. " +
                                "Please sign in to finish setup."
                    )
                } finally {
                    // Registration does not register an FCM device.
                    // End the temporary Firebase session.
                    auth.signOut()

                    CurrentStudent.clear()
                    CurrentManager.ID = ""
                    CurrentManager.ACCOMMODATION_ID = ""

                    val intent = Intent(
                        this@RegisterActivity,
                        LoginActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
            }

        }.addOnFailureListener { exception ->
            registrationInProgress = false
            btnRegister.isEnabled = true

            toast(
                exception.message ?: "Registration failed"
            )
        }
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
