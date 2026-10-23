package com.chommiehub.app.ui.auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.ui.manager.CurrentManager
import com.chommiehub.app.ui.manager.ManagerActivity
import com.chommiehub.app.ui.student.CurrentStudent
import com.chommiehub.app.ui.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.GoogleAuthProvider
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

        CoroutineScope(Dispatchers.IO).launch {
            val destination = try {
                resolveDestination(user)
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, exception.message ?: "Could not reach the server. Check your connection and try again.", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                if (destination == null) {
                    Toast.makeText(activity, "We couldn't find an account for you. Please sign in again or register.", Toast.LENGTH_LONG).show()
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
     * Returns null when no supported backend profile can be resolved.
     *
     * As a side effect, populates CurrentStudent and CurrentManager so
     * downstream screens can query the API with the correct database
     * identifiers rather than the raw Firebase UID.
     */
    private suspend fun resolveDestination(user: FirebaseUser): Class<out Activity>? {
        val firebaseUid = user.uid
        val tokenResult = user.getIdToken(false).await()
        val role = tokenResult.claims["role"] as? String

        if (role == "admin") {
            throw IllegalStateException(
                "Administrator account recognised. Administrator dashboard is not available yet."
            )
        }

        try {
            val student = ApiClient.api.getStudentByFirebaseUid(firebaseUid)

            if (student.id.isNotBlank()) {
                CurrentStudent.update(student)
                CurrentManager.ID = ""
                CurrentManager.ACCOMMODATION_ID = ""
                return StudentActivity::class.java
            }
        } catch (exception: HttpException) {
            if (exception.code() != 404) {
                throw exception
            }
        }

        try {
            val manager = ApiClient.api.getManagerByFirebaseUid(firebaseUid)

            if (manager.id.isNotBlank()) {
                CurrentManager.ID = manager.id
                CurrentManager.ACCOMMODATION_ID = manager.accommodationId
                CurrentStudent.clear()
                return ManagerActivity::class.java
            }
        } catch (exception: HttpException) {
            if (exception.code() != 404) {
                throw exception
            }
        }

        val signedInWithGoogle = user.providerData.any { provider ->
            provider.providerId == GoogleAuthProvider.PROVIDER_ID
        }

        if (!signedInWithGoogle) {
            return null
        }

        return createStudentProfileForGoogleUser(user)
    }

    /**
     * Creates the ChommieHub Student profile required by a first-time Google SSO user.
     * Firebase has already authenticated the user before this method is called.
     */
    private suspend fun createStudentProfileForGoogleUser(user: FirebaseUser): Class<out Activity> {
        val email = user.email ?: throw IllegalStateException("The Google account did not provide an email address.")

        val displayName = user.displayName.orEmpty().trim()
        val nameParts = displayName.split(Regex("\\s+")).filter { it.isNotBlank() }

        val firstName = nameParts.firstOrNull() ?: email.substringBefore("@").ifBlank { "Student" }
        val surname = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else "User"

        val request = CreateStudentRequest(
            firebaseUid = user.uid,
            name = firstName,
            surname = surname,
            email = email,
            phoneNumber = user.phoneNumber ?: "Not provided",
            preferredLanguage = "English"
        )

        val student = ApiClient.api.createStudent(request)

        CurrentStudent.update(student)
        CurrentManager.ID = ""
        CurrentManager.ACCOMMODATION_ID = ""

        return StudentActivity::class.java
    }
}