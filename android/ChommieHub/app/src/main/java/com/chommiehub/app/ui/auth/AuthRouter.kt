package com.chommiehub.app.ui.auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.ui.manager.ManagerActivity
import com.chommiehub.app.ui.student.CurrentStudent
import com.chommiehub.app.ui.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/** Routes an authenticated Firebase user without asking them to choose their own role. */
object AuthRouter {

    fun route(activity: Activity) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            activity.finish()
            return
        }

        val firebaseUid = user.uid

        CoroutineScope(Dispatchers.IO).launch {
            val destination = try {
                resolveDestination(firebaseUid)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        activity,
                        e.message ?: "Could not reach the server. Check your connection and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                if (destination == null) {
                    Toast.makeText(
                        activity,
                        "We couldn't find an account for you. Please sign in again or register.",
                        Toast.LENGTH_LONG
                    ).show()
                    FirebaseAuth.getInstance().signOut()
                    activity.startActivity(Intent(activity, LoginActivity::class.java))
                } else {
                    activity.startActivity(Intent(activity, destination))
                }
                activity.finish()
            }
        }
    }

    /**
     * Looks up the signed-in user's role via the ChommieHub API.
     * Returns null when no student or manager profile is linked to
     * this Firebase UID.
     */
    private suspend fun resolveDestination(firebaseUid: String): Class<out Activity>? {
        try {
            val student = ApiClient.api.getStudentByFirebaseUid(firebaseUid)
            CurrentStudent.ID = student.id
            return StudentActivity::class.java
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
        }

        try {
            ApiClient.api.getManagerByFirebaseUid(firebaseUid)
            return ManagerActivity::class.java
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
        }

        return null
    }
}
