package com.chommiehub.app.ui.student.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.AccommodationResponse

class AccommodationAdapter(
    private val items: List<AccommodationResponse>,
    private val onViewDetails: (AccommodationResponse) -> Unit,
    private val onToggleShortlist: (AccommodationResponse) -> Unit
) : RecyclerView.Adapter<AccommodationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textName)
        val location: TextView = view.findViewById(R.id.textLocation)
        val viewDetailsButton: Button = view.findViewById(R.id.buttonViewDetails)
        val shortlistButton: ImageButton = view.findViewById(R.id.buttonShortlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_accommodation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.location.text = "${item.address} \u2022 ${item.institutionArea}"
        holder.viewDetailsButton.setOnClickListener { onViewDetails(item) }
        holder.shortlistButton.setOnClickListener { onToggleShortlist(item) }
    }

    override fun getItemCount() = items.size
}