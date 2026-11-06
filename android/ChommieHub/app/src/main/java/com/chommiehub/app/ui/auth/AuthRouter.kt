
package com.chommiehub.app.ui.auth

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.data.model.student.CreateStudentRequest
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.firebase.FcmTokenManager
import com.chommiehub.app.ui.admin.AdminActivity
import com.chommiehub.app.ui.manager.CurrentManager
import com.chommiehub.app.ui.manager.ManagerActivity
import com.chommiehub.app.ui.student.CurrentStudent
import com.chommiehub.app.ui.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Routes an authenticated Firebase user using their
 * backend profile, rather than a user-selected role.
 */
object AuthRouter {

    fun route(activity: AppCompatActivity) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            clearCachedIdentity()
            openLogin(activity)
            return
        }

        val expectedUid = user.uid

        activity.lifecycleScope.launch {
            // Clear information left by an earlier session.
            clearCachedIdentity()

            val destination = try {
                withContext(Dispatchers.IO) {
                    resolveDestination(user)
                }
            } catch (exception: Exception) {
                if (!isCurrentUser(expectedUid)) {
                    return@launch
                }

                Toast.makeText(
                    activity,
                    exception.message
                        ?: "Could not reach the server. Check your connection and try again.",
                    Toast.LENGTH_LONG
                ).show()

                return@launch
            }

            // The user may have signed out or switched accounts
            // while the backend requests were running.
            if (!isCurrentUser(expectedUid)) {
                clearCachedIdentity()
                return@launch
            }

            if (destination == null) {
                Toast.makeText(
                    activity,
                    "We couldn't find an account for you. Please sign in again or register.",
                    Toast.LENGTH_LONG
                ).show()

                // Remove an existing device registration before
                // ending the Firebase authentication session.
                val removed = withContext(Dispatchers.IO) {
                    FcmTokenManager.unregisterCurrentDevice()
                }

                if (!isCurrentUser(expectedUid)) {
                    clearCachedIdentity()
                    return@launch
                }

                if (!removed) {
                    android.util.Log.w(
                        "AuthRouter",
                        "FCM cleanup was not confirmed before sign-out."
                    )
                }

                auth.signOut()
                clearCachedIdentity()
                openLogin(activity)
                return@launch
            }

            // Register the FCM token only after resolving the
            // signed-in user's student or manager profile.
            FcmTokenManager.refreshTokenForCurrentUser()

            val intent = Intent(activity, destination)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            activity.startActivity(intent)
            activity.finish()
        }
    }

    /**
     * Looks up the signed-in user's backend profile.
     *
     * Returns null when no supported profile is found.
     */
    private suspend fun resolveDestination(
        user: FirebaseUser
    ): Class<out Activity>? {

        val firebaseUid = user.uid
        val tokenResult = user.getIdToken(false).await()
        val role = tokenResult.claims["role"] as? String

        if (role == "admin") {
            // The backend independently verifies the admin claim on every request.
            return AdminActivity::class.java
        }

        // Check the student profile first.
        try {
            val student =
                ApiClient.api.getStudentByFirebaseUid(firebaseUid)

            if (student.id.isNotBlank()) {
                ensureCurrentUser(firebaseUid)

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

        // Then check the manager profile.
        try {
            val manager =
                ApiClient.api.getManagerByFirebaseUid(firebaseUid)

            if (manager.id.isNotBlank()) {
                ensureCurrentUser(firebaseUid)

                CurrentManager.ID = manager.id
                CurrentManager.ACCOMMODATION_ID =
                    manager.accommodationId

                CurrentStudent.clear()

                return ManagerActivity::class.java
            }
        } catch (exception: HttpException) {
            if (exception.code() != 404) {
                throw exception
            }
        }

        // First-time Google users may create a student profile.
        val signedInWithGoogle = user.providerData.any { provider ->
            provider.providerId == GoogleAuthProvider.PROVIDER_ID
        }

        if (!signedInWithGoogle) {
            return null
        }

        return createStudentProfileForGoogleUser(user)
    }

    /**
     * Creates the student profile for a first-time Google user.
     */
    private suspend fun createStudentProfileForGoogleUser(
        user: FirebaseUser
    ): Class<out Activity> {

        val email = user.email
            ?: throw IllegalStateException(
                "The Google account did not provide an email address."
            )

        val displayName = user.displayName.orEmpty().trim()

        val nameParts = displayName
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val firstName = nameParts.firstOrNull()
            ?: email.substringBefore("@").ifBlank {
                "Student"
            }

        val surname = if (nameParts.size > 1) {
            nameParts.drop(1).joinToString(" ")
        } else {
            "User"
        }

        ensureCurrentUser(user.uid)

        val request = CreateStudentRequest(
            firebaseUid = user.uid,
            name = firstName,
            surname = surname,
            email = email,
            phoneNumber = user.phoneNumber ?: "Not provided",
            preferredLanguage = "English"
        )

        val student = ApiClient.api.createStudent(request)

        ensureCurrentUser(user.uid)

        CurrentStudent.update(student)
        CurrentManager.ID = ""
        CurrentManager.ACCOMMODATION_ID = ""

        return StudentActivity::class.java
    }

    private fun isCurrentUser(expectedUid: String): Boolean {
        return FirebaseAuth.getInstance().currentUser?.uid ==
                expectedUid
    }

    private fun ensureCurrentUser(expectedUid: String) {
        check(isCurrentUser(expectedUid)) {
            "Firebase authentication session changed."
        }
    }

    private fun clearCachedIdentity() {
        CurrentStudent.clear()
        CurrentManager.ID = ""
        CurrentManager.ACCOMMODATION_ID = ""
    }

    private fun openLogin(activity: Activity) {
        val intent = Intent(
            activity,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        activity.startActivity(intent)
        activity.finish()
    }
}
