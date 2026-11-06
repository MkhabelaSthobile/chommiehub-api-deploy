package com.chommiehub.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.chommiehub.app.R
import com.google.firebase.auth.FirebaseAuth

/** The welcome page is the launcher for signed-out users. */
class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null) {
            // Returning users must pass biometric/credential unlock first.
            startActivity(Intent(this, BiometricActivity::class.java))
            finish()
            return
        }

        val dp = resources.displayMetrics.density
        fun px(value: Int) = (value * dp).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(px(26), px(34), px(26), px(30))
            setBackgroundColor(ContextCompat.getColor(this@WelcomeActivity, R.color.chommie_background))
        }
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.chommiehub_logo)
            contentDescription = getString(R.string.logo_description)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        layout.addView(logo, LinearLayout.LayoutParams(px(190), px(130)))
        val heading = TextView(this).apply {
            text = "Find your next student home"
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(this@WelcomeActivity, R.color.chommie_navy))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        layout.addView(heading)
        val copy = TextView(this).apply {
            text = "Search for residence near your campus, compare room prices, apply online and track your application."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, px(12), 0, px(14))
        }
        layout.addView(copy)
        listOf("Search residences and view map locations", "Save a shortlist, even when offline", "Track application and approval status", "Resident maintenance and visitor services")
            .forEach { feature ->
                layout.addView(TextView(this).apply {
                    text = "• $feature"
                    textSize = 15f
                    setPadding(px(4), px(12), px(4), px(12))
                })
            }
        layout.addView(Button(this).apply {
            text = "Sign in"
            isAllCaps = false
            setOnClickListener { startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java)) }
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = px(22) })
        layout.addView(Button(this).apply {
            text = "Create a student account"
            isAllCaps = false
            setOnClickListener { startActivity(Intent(this@WelcomeActivity, RegisterActivity::class.java)) }
        }, LinearLayout.LayoutParams(-1, -2))
        setContentView(ScrollView(this).apply { fillViewport = true; addView(layout) })
    }
}
