package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch

class MyApplicationsFragment : Fragment(R.layout.fragment_my_applications) {

    // Remembered so coming back from a details screen keeps the same view.
    private var showingShortlist = false

    override fun onViewCreated(v: View, b: Bundle?) {
        setupViewSwitch(v)

        val list = v.findViewById<LinearLayout>(R.id.applicationList)
        val studentId = CurrentStudent.ID
        if (studentId.isBlank()) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val xs = StudentRepository(ApiClient.api).applications(studentId)
                list.removeAllViews()
                if (xs.isEmpty()) v.findViewById<TextView>(R.id.tvEmpty).visibility = View.VISIBLE
                xs.forEach { a ->
                    val item = layoutInflater.inflate(R.layout.item_student_application, list, false)
                    item.findViewById<TextView>(R.id.tvAppAccommodation).text = a.accommodationName
                    item.findViewById<TextView>(R.id.tvAppRoom).text = "${a.roomTypePreference} • ${a.rentalPeriod}"
                    item.findViewById<TextView>(R.id.tvAppStatus).text = a.status
                    item.findViewById<TextView>(R.id.tvAppNotes).text =
                        if (a.reviewNotes.isBlank()) "Awaiting manager update" else a.reviewNotes
                    item.setOnClickListener {
                        parentFragmentManager.beginTransaction()
                            .replace(
                                R.id.studentContainer,
                                ApplicationStatusFragment.newInstance(
                                    a.status,
                                    a.assignedRoomId,
                                    a.accommodationName,
                                    a.reviewNotes
                                )
                            )
                            .addToBackStack(null)
                            .commit()
                    }
                    list.addView(item)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Sets up the Applications | Shortlist switch and embeds the shortlist screen. */
    private fun setupViewSwitch(v: View) {
        val toggle = v.findViewById<MaterialButtonToggleGroup>(R.id.viewSwitch)
        val applicationsPanel = v.findViewById<View>(R.id.applicationsPanel)
        val shortlistContainer = v.findViewById<View>(R.id.shortlistContainer)

        if (childFragmentManager.findFragmentById(R.id.shortlistContainer) == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.shortlistContainer, ShortlistFragment())
                .commit()
        }

        fun showPanel(shortlist: Boolean) {
            showingShortlist = shortlist
            applicationsPanel.visibility = if (shortlist) View.GONE else View.VISIBLE
            shortlistContainer.visibility = if (shortlist) View.VISIBLE else View.GONE
        }

        toggle.check(if (showingShortlist) R.id.btnShortlistTab else R.id.btnApplicationsTab)
        showPanel(showingShortlist)

        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) showPanel(checkedId == R.id.btnShortlistTab)
        }
    }
}