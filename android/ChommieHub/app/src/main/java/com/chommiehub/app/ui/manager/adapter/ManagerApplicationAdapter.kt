package com.chommiehub.app.ui.manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.model.manager.ManagerApplicationSummaryResponse

/**
 * Displays accommodation applications in the residence manager's queue
 */
class ManagerApplicationAdapter : RecyclerView.Adapter<ManagerApplicationAdapter.ApplicationViewHolder>() {

    private val applications = mutableListOf<ManagerApplicationSummaryResponse>()

    /**
     * Replaces the currently displayed application collection
     */
    fun submitList(newApplications: List<ManagerApplicationSummaryResponse>
    ) {
        applications.clear()
        applications.addAll(newApplications)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_manager_application,
                parent,
                false
            )

        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int
    ) {
        holder.bind(applications[position])
    }

    override fun getItemCount(): Int = applications.size

    class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvStudent: TextView = itemView.findViewById(R.id.tvApplicationStudent
        )

        private val tvStatus: TextView = itemView.findViewById(R.id.tvApplicationStatus
        )

        private val tvRoomPreference: TextView = itemView.findViewById(R.id.tvApplicationRoomPreference
        )

        private val tvSubmitted: TextView = itemView.findViewById(R.id.tvApplicationSubmitted
        )

        fun bind(application: ManagerApplicationSummaryResponse
        ) {
            val context = itemView.context

            tvStudent.text = context.getString(
                R.string.manager_application_student,
                application.studentId
            )

            tvStatus.text = context.getString(
                R.string.manager_application_status,
                application.status
            )

            tvRoomPreference.text = context.getString(
                R.string.manager_application_room,
                application.roomTypePreference
            )

            val date = application.dateSubmittedUtc.take(10)

            tvSubmitted.text = context.getString(
                R.string.manager_application_submitted,
                date
            )
        }
    }
}