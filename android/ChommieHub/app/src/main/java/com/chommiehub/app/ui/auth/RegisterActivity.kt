package com.chommiehub.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R
import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.remote.ApiClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            register()
        }

        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            finish()
        }
    }

    private fun register() {

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

        // Validate required fields
        if (
            name.isBlank() ||
            surname.isBlank() ||
            phone.isBlank()
        ) {
            toast("Complete all required fields")
            return
        }

        // Validate email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Enter a valid email")
            return
        }

        // Validate password
        if (password.length < 6) {
            toast("Password must be at least 6 characters")
            return
        }

        // Confirm passwords match
        if (password != confirmPassword) {
            toast("Passwords do not match")
            return
        }

        // Create Firebase Authentication account
        auth.createUserWithEmailAndPassword(
            email,
            password
        ).addOnSuccessListener { result ->

            val firebaseUid = result.user?.uid

            if (firebaseUid == null) {
                toast("Could not retrieve Firebase user ID")
                return@addOnSuccessListener
            }

            // Create the student's profile through the ChommieHub API
            CoroutineScope(Dispatchers.IO).launch {

                try {

                    val request = CreateStudentRequest(
                        firebaseUid = firebaseUid,
                        name = name,
                        surname = surname,
                        email = email,
                        phoneNumber = phone,
                        preferredLanguage = "English"
                    )

                    ApiClient.api.createStudent(request)

                    withContext(Dispatchers.Main) {

                        toast("Registration successful")

                        startActivity(
                            Intent(
                                this@RegisterActivity,
                                LoginActivity::class.java
                            )
                        )

                        finish()
                    }

                } catch (exception: Exception) {

                    withContext(Dispatchers.Main) {
                        toast(
                            "Account created. Profile sync failed: " +
                                    (exception.message ?: "Unknown error")
                        )
                    }
                }
            }

        }.addOnFailureListener { exception ->

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