package com.chommiehub.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.ui.auth.LoginActivity
import com.chommiehub.app.ui.auth.AuthRouter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Member 4 administrator area. Role claims are not trusted merely because this
 * Activity was opened: the backend independently checks every administrator API call.
 */
class AdminActivity : AppCompatActivity() {
    private lateinit var navigation: BottomNavigationView
    private var initialized = false
    private var checking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        navigation = findViewById(R.id.adminBottomNav)
        navigation.visibility = View.GONE
        navigation.setOnItemSelectedListener { item ->
            val page: Fragment = when (item.itemId) {
                R.id.nav_admin_dashboard -> AdminDashboardFragment()
                R.id.nav_admin_listings -> AdminListingsFragment()
                R.id.nav_admin_accounts -> AdminAccountsFragment()
                R.id.nav_admin_audit -> AdminAuditFragment()
                R.id.nav_admin_settings -> AdminSettingsFragment()
                else -> AdminDashboardFragment()
            }
            show(page)
            true
        }
        showStatus("Checking administrator access…")
    }

    override fun onResume() {
        super.onResume()
        verifyAccess()
    }

    private fun verifyAccess() {
        if (checking) return
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            denyAccess()
            return
        }
        checking = true
        lifecycleScope.launch {
            try {
                // Refresh the claim: newly granted admin access is not assumed from a
                // locally cached Firebase token.
                val role = user.getIdToken(true).await().claims["role"] as? String
                if (FirebaseAuth.getInstance().currentUser?.uid != user.uid || role != "admin") {
                    denyAccess()
                    return@launch
                }
                // A backend round-trip also verifies that the token is valid/not revoked.
                ApiClient.api.getAdminDashboard()
                navigation.visibility = View.VISIBLE
                if (!initialized) {
                    initialized = true
                    navigation.selectedItemId = R.id.nav_admin_dashboard
                }
            } catch (exception: Exception) {
                // No offline admin data is exposed when authorization cannot be checked.
                navigation.visibility = View.GONE
                showStatus("Administrator verification failed. Check your connection and sign in again.\n${exception.message.orEmpty()}")
                Toast.makeText(this@AdminActivity, "Administrator access could not be verified.", Toast.LENGTH_LONG).show()
            } finally {
                checking = false
            }
        }
    }

    fun openListings(status: String = "") {
        navigation.selectedItemId = R.id.nav_admin_listings
        show(AdminListingsFragment.newInstance(status))
    }

    fun openAudit() {
        navigation.selectedItemId = R.id.nav_admin_audit
    }

    private fun show(page: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminContainer, page)
            .commit()
    }

    private fun showStatus(message: String) {
        val view = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(28, 44, 28, 28)
        }
        findViewById<android.widget.FrameLayout>(R.id.adminContainer).apply {
            removeAllViews()
            addView(view)
        }
    }

    private fun denyAccess() {
        Toast.makeText(this, getString(R.string.admin_role_denied), Toast.LENGTH_LONG).show()
        if (FirebaseAuth.getInstance().currentUser != null) {
            AuthRouter.route(this)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
