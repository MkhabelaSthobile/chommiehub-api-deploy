package com.chommiehub.app.ui.admin

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** Common readable, accessible layout helpers for all administrator screens. */
abstract class AdminBaseFragment : Fragment() {
    protected lateinit var content: LinearLayout

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(24))
        }
        return ScrollView(requireContext()).apply {
            fillViewport = true
            addView(content)
        }
    }

    protected fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    protected fun clearPage(title: String, subtitle: String = "") {
        content.removeAllViews()
        heading(content, title, 24)
        if (subtitle.isNotBlank()) line(content, subtitle, 14)
    }

    protected fun heading(parent: LinearLayout, text: String, size: Int = 19): TextView =
        TextView(requireContext()).apply {
            this.text = text
            textSize = size.toFloat()
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.chommie_navy))
            setPadding(dp(4), dp(12), dp(4), dp(7))
            parent.addView(this)
        }

    protected fun line(parent: LinearLayout, text: String, size: Int = 15): TextView =
        TextView(requireContext()).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(ContextCompat.getColor(requireContext(), R.color.chommie_text_primary))
            setPadding(dp(4), dp(3), dp(4), dp(6))
            parent.addView(this)
        }

    protected fun button(parent: LinearLayout, label: String, onClick: () -> Unit): Button =
        Button(requireContext()).apply {
            text = label
            isAllCaps = false
            minHeight = dp(48)
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.chommie_navy)
            )
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            setOnClickListener { onClick() }
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(8)
            })
        }

    protected fun card(title: String, summary: String, onClick: (() -> Unit)? = null) {
        val panel = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(13), dp(14), dp(13))
            backgroundTintList = null
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.chommie_surface))
            elevation = dp(2).toFloat()
            isClickable = onClick != null
            isFocusable = onClick != null
            if (onClick != null) setOnClickListener { onClick() }
        }
        heading(panel, title, 17)
        line(panel, summary, 14)
        content.addView(panel, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
    }

    protected fun toast(message: String) = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

    protected fun launchTask(block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                block()
            } catch (e: Exception) {
                if (isAdded) toast(if (e is HttpException) "Server returned ${e.code()}: ${e.message()}" else e.message ?: "Request failed")
            }
        }
    }

    protected fun askReason(title: String, action: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.admin_action_reason)
            minLines = 2
            setPadding(dp(18), dp(8), dp(18), dp(8))
        }
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val reason = input.text.toString().trim()
                if (reason.isBlank() || reason.length > 500) {
                    input.error = "Enter a reason (up to 500 characters)."
                } else {
                    dialog.dismiss()
                    action(reason)
                }
            }
        }
        dialog.show()
    }

    protected fun confirm(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title).setMessage(message)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ -> onConfirm() }
            .show()
    }
}
