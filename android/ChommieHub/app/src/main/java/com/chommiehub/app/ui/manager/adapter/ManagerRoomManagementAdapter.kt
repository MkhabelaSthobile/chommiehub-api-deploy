package com.chommiehub.app.ui.manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.RoomResponse

/**
 * Displays the full room inventory and opens a selected room for editing
 */
class ManagerRoomManagementAdapter(private val onRoomClick: (RoomResponse) -> Unit) : RecyclerView.Adapter<ManagerRoomManagementAdapter.RoomViewHolder>()
{
    private val rooms = mutableListOf<RoomResponse>()

    fun submitList(newRooms: List<RoomResponse>)
    {
        rooms.clear()
        rooms.addAll(newRooms)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_manager_room_management,
                    parent,
                    false
                )

        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RoomViewHolder,
        position: Int
    ) {
        holder.bind(
            rooms[position]
        )
    }

    override fun getItemCount(): Int = rooms.size

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private val tvType: TextView = itemView.findViewById(R.id.tvManagedRoomType)

        private val tvPrice: TextView = itemView.findViewById(R.id.tvManagedRoomPrice)

        private val tvCapacity: TextView = itemView.findViewById(R.id.tvManagedRoomCapacity)

        private val tvAvailability: TextView = itemView.findViewById(R.id.tvManagedRoomAvailability)

        fun bind(room: RoomResponse)
        {
            val context = itemView.context

            tvType.text = room.roomType

            tvPrice.text = context.getString(
                    R.string.room_price,
                    room.price
                )

            tvCapacity.text = context.getString(
                    R.string.room_capacity,
                    room.capacity
                )

            tvAvailability.text =
                if (room.availability) {
                    "Available"
                } else {
                    "Unavailable"
                }

            itemView.setOnClickListener {
                onRoomClick(room)
            }
        }
    }
}