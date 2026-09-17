package com.chommiehub.app.ui.manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.ResidentResponse

/**
 * Displays students currently assigned to the manager's residence.
 */
class ManagerResidentAdapter : RecyclerView.Adapter<ManagerResidentAdapter.ResidentViewHolder>()
{
    private val residents = mutableListOf<ResidentResponse>()

    fun submitList(
        newResidents:
        List<ResidentResponse>
    ) {
        residents.clear()
        residents.addAll(newResidents)

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidentViewHolder
    {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_manager_resident,
                    parent,
                    false
                )

        return ResidentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ResidentViewHolder,
        position: Int
    ) {
        holder.bind(residents[position])
    }

    override fun getItemCount(): Int = residents.size

    class ResidentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private val tvStudent: TextView = itemView.findViewById(R.id.tvResidentStudent)

        private val tvRoom: TextView = itemView.findViewById(R.id.tvResidentRoom)

        private val tvStatus: TextView = itemView.findViewById(R.id.tvResidentStatus)

        fun bind(
            resident:
            ResidentResponse
        ) {
            val context = itemView.context

            tvStudent.text = context.getString(
                R.string.resident_student,
                resident.studentId
            )

            tvRoom.text = context.getString(
                R.string.resident_room,
                resident.roomId
            )

            tvStatus.text =
                if (
                    resident.isActive
                ) {
                    context.getString(R.string.resident_active)
                } else {
                    context.getString(R.string.resident_inactive)
                }
        }
    }
}