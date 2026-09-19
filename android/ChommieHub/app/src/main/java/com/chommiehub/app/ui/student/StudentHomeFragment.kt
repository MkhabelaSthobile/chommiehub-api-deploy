package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository
import kotlinx.coroutines.launch

/**
 * Lets a student browse the accommodations added by residence managers, and search by
 * name, address or institution area before starting an application.
 */
class StudentHomeFragment : Fragment(R.layout.fragment_student_home) {

    private var accommodations: List<AccommodationResponse> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<LinearLayout>(R.id.accommodationList)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val btnSearch = view.findViewById<android.widget.Button>(R.id.btnSearch)

        fun render(items: List<AccommodationResponse>) {
            list.removeAllViews()
            items.forEach { accommodation ->
                val item = layoutInflater.inflate(R.layout.item_student_accommodation, list, false)
                item.findViewById<TextView>(R.id.tvLocation).text = accommodation.institutionArea
                item.findViewById<TextView>(R.id.tvName).text = accommodation.name
                item.findViewById<TextView>(R.id.tvDescription).text = accommodation.description
                item.findViewById<TextView>(R.id.tvAmenities).text = accommodation.amenities.joinToString(" • ")
                item.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.studentContainer, AccommodationDetailFragment.newInstance(accommodation.id))
                        .addToBackStack(null)
                        .commit()
                }
                list.addView(item)
            }
        }

        etSearch.setOnKeyListener { _, _, _ ->
            val query = etSearch.text.toString().trim().lowercase()
            render(
                if (query.isBlank()) accommodations
                else accommodations.filter {
                    it.name.lowercase().contains(query) ||
                        it.address.lowercase().contains(query) ||
                        it.institutionArea.lowercase().contains(query)
                }
            )
            false
        }

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim().lowercase()
            render(
                if (query.isBlank()) accommodations
                else accommodations.filter {
                    it.name.lowercase().contains(query) ||
                        it.address.lowercase().contains(query) ||
                        it.institutionArea.lowercase().contains(query)
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                accommodations = StudentRepository(ApiClient.api).getAccommodations()
                render(accommodations)
            } catch (_: Exception) {
                // Leave the list empty; the student can pull to refresh by re-opening the tab.
            }
        }
    }
}
