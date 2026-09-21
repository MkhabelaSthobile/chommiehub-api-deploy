package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R

class StudentHomeFragment : Fragment(R.layout.fragment_student_home) {

    data class DummyAccommodation(
        val name: String,
        val location: String,
        val price: String
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerAccommodations)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = AccommodationAdapter(dummyAccommodations())
    }

    private fun dummyAccommodations() = listOf(
        DummyAccommodation("Riverside student residence", "Braamfontein, Johannesburg", "From R4,500 / month"),
        DummyAccommodation("Yale village", "Hatfield, Pretoria", "From R3,200 / month"),
        DummyAccommodation("The Waterford residence", "Auckland Park, Johannesburg", "From R5,800 / month")
    )
}

private class AccommodationAdapter(
    private val items: List<StudentHomeFragment.DummyAccommodation>
) : RecyclerView.Adapter<AccommodationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textName)
        val location: TextView = view.findViewById(R.id.textLocation)
        val price: TextView = view.findViewById(R.id.textPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_accommodation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.location.text = item.location
        holder.price.text = item.price
    }

    override fun getItemCount() = items.size
}