package com.chommiehub.app.ui.student.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.RoomResponse

class RoomAdapter(
    private val items: List<RoomResponse>,
    private val onViewRoom: (RoomResponse) -> Unit
) : RecyclerView.Adapter<RoomAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.textRoomType)
        val capacity: TextView = view.findViewById(R.id.textRoomCapacity)
        val price: TextView = view.findViewById(R.id.textRoomPrice)
        val viewButton: Button = view.findViewById(R.id.buttonViewRoom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.type.text = item.roomType
        holder.capacity.text = "Capacity: ${item.capacity}"
        holder.price.text = if (item.availability) {
            "R${item.price.toInt()} / month"
        } else {
            "Unavailable"
        }
        holder.viewButton.setOnClickListener { onViewRoom(item) }
    }

    override fun getItemCount() = items.size
}