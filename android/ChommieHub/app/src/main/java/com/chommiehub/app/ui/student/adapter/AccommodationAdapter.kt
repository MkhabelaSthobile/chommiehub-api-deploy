package com.chommiehub.app.ui.student.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.chommiehub.app.R
import com.chommiehub.app.data.local.entity.SyncState
import com.chommiehub.app.data.model.manager.AccommodationResponse

class AccommodationAdapter(
    private val items: List<AccommodationResponse>,
    private val onViewDetails: (AccommodationResponse) -> Unit,
    private val onToggleShortlist: (AccommodationResponse) -> Unit,
    private val showAsShortlisted: Boolean = false
) : RecyclerView.Adapter<AccommodationAdapter.ViewHolder>() {

    // Sync status per residence id. Empty means no labels are shown.
    private var statuses: Map<String, SyncState> = emptyMap()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textName)
        val location: TextView = view.findViewById(R.id.textLocation)
        val syncStatus: TextView = view.findViewById(R.id.textSyncStatus)
        val viewDetailsButton: Button = view.findViewById(R.id.buttonViewDetails)
        val shortlistButton: ImageButton = view.findViewById(R.id.buttonShortlist)
    }

    fun updateStatuses(newStatuses: Map<String, SyncState>) {
        statuses = newStatuses
        notifyDataSetChanged()
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
        holder.viewDetailsButton.text =
            holder.itemView.context.getString(R.string.view_details)
        holder.viewDetailsButton.text =
            holder.itemView.context.getString(R.string.view_details)
        holder.viewDetailsButton.setOnClickListener { onViewDetails(item) }
        holder.shortlistButton.setOnClickListener { onToggleShortlist(item) }

        if (showAsShortlisted) {
            holder.shortlistButton.setImageResource(android.R.drawable.btn_star_big_on)
            holder.shortlistButton.contentDescription =
                holder.itemView.context.getString(R.string.remove_from_shortlist)
        }

        bindStatus(holder, statuses[item.id])
    }

    private fun bindStatus(holder: ViewHolder, state: SyncState?) {
        when (state) {
            SyncState.PENDING -> showStatus(holder, R.string.sync_status_pending, R.color.chommie_gold_dark)
            SyncState.SYNCED -> showStatus(holder, R.string.sync_status_synced, R.color.chommie_success)
            SyncState.FAILED -> showStatus(holder, R.string.sync_status_failed, R.color.chommie_danger)
            null -> holder.syncStatus.visibility = View.GONE
        }
    }

    private fun showStatus(holder: ViewHolder, textRes: Int, colorRes: Int) {
        val context = holder.itemView.context
        holder.syncStatus.text = context.getString(textRes)
        holder.syncStatus.setTextColor(ContextCompat.getColor(context, colorRes))
        holder.syncStatus.visibility = View.VISIBLE
    }

    override fun getItemCount() = items.size
}