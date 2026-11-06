package com.chommiehub.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.chommiehub.app.R
import com.chommiehub.app.firebase.FcmTokenManager
import com.chommiehub.app.ui.auth.LoginActivity
import com.chommiehub.app.ui.manager.CurrentManager
import com.chommiehub.app.ui.student.CurrentStudent
import com.google.firebase.auth.FirebaseAuth

/** Admin language preference, identity information and secure sign-out. */
class AdminSettingsFragment : AdminBaseFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clearPage("Admin settings", "Manage your account and preferences")
        val user = FirebaseAuth.getInstance().currentUser
        card("Signed-in administrator", user?.email ?: "No active account")
        heading(content, getString(R.string.admin_settings_language))
        val names = listOf("English", "Afrikaans", "isiZulu", "Sesotho")
        val codes = listOf("en", "af", "zu", "st")
        val spinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, names)
            val code = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-')
            setSelection(codes.indexOf(code).coerceAtLeast(0))
        }
        content.addView(spinner)
        button(content, "Save language") {
            val code = codes[spinner.selectedItemPosition]
            // AppCompat controls strings for all Android screens using application locales.
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
            toast("Language preference saved.")
        }
        button(content, getString(R.string.admin_sign_out)) {
            confirm("Sign out", "Sign out of this administrator account?") {
                launchTask {
                    FcmTokenManager.unregisterCurrentDevice()
                    FirebaseAuth.getInstance().signOut()
                    CurrentManager.ID = ""
                    CurrentManager.ACCOMMODATION_ID = ""
                    CurrentStudent.clear()
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    activity?.finish()
                }
            }
        }
    }
}
