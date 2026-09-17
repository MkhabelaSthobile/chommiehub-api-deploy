package com.chommiehub.app.ui.manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.RoomResponse

/**
 * Displays available rooms and records the room selected by the manager
 */
class ManagerRoomAdapter(private val onRoomSelected: (RoomResponse) -> Unit) : RecyclerView.Adapter<ManagerRoomAdapter.RoomViewHolder>()
{
    private val rooms = mutableListOf<RoomResponse>()

    private var selectedRoomId: String? = null

    fun submitList(newRooms: List<RoomResponse>)
    {
        rooms.clear()
        rooms.addAll(newRooms)
        selectedRoomId = null
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder
    {
        val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_manager_room,
                    parent,
                    false
                )

        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int)
    {
        holder.bind(
            rooms[position]
        )
    }

    override fun getItemCount(): Int = rooms.size

    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private val radioRoom: RadioButton = itemView.findViewById(R.id.radioRoom)

        private val tvRoomType: TextView = itemView.findViewById(R.id.tvRoomType)

        private val tvRoomPrice: TextView = itemView.findViewById(R.id.tvRoomPrice)

        private val tvRoomCapacity: TextView = itemView.findViewById(R.id.tvRoomCapacity)

        fun bind(room: RoomResponse)
        {
            val context = itemView.context

            radioRoom.isChecked = selectedRoomId == room.id

            tvRoomType.text = room.roomType

            tvRoomPrice.text = context.getString(
                R.string.room_price,
                room.price
            )

            tvRoomCapacity.text = context.getString(
                R.string.room_capacity,
                room.capacity
            )

            itemView.setOnClickListener {
                selectRoom(room)
            }

            radioRoom.setOnClickListener {
                selectRoom(room)
            }
        }

        private fun selectRoom(room: RoomResponse)
        {
            selectedRoomId = room.id

            notifyDataSetChanged()

            onRoomSelected(
                room
            )
        }
    }
}