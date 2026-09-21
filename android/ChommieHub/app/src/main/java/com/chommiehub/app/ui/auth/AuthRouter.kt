package com.chommiehub.app.ui.auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.chommiehub.app.ui.manager.ManagerActivity
import com.chommiehub.app.ui.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth

/** Routes an authenticated Firebase user without asking them to choose their own role. */
object AuthRouter {
    fun route(activity: Activity) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            activity.finish()
            return
        }

        user.getIdToken(false)
            .addOnSuccessListener { token ->
                val role = token.claims["role"]?.toString()?.trim()?.lowercase()
                val destination = if (role == "manager") ManagerActivity::class.java else StudentActivity::class.java
                activity.startActivity(Intent(activity, destination))
                activity.finish()
            }
            .addOnFailureListener {
                // Registered students do not need a custom role claim. Manager accounts do.
                Toast.makeText(activity, "Could not verify account role. Opening student account.", Toast.LENGTH_SHORT).show()
                activity.startActivity(Intent(activity, StudentActivity::class.java))
                activity.finish()
            }
    }
}
