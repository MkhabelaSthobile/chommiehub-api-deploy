package com.chommiehub.app.ui.student

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chommiehub.app.R

/** Student-facing view of Person A's existing application workflow. */
class ApplicationStatusFragment : Fragment(R.layout.fragment_application_status) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val status = requireArguments().getString("status").orEmpty()
        val roomId = requireArguments().getString("roomId").orEmpty()
        val residence = requireArguments().getString("residence").orEmpty()
        val notes = requireArguments().getString("notes").orEmpty()

        view.findViewById<TextView>(R.id.tvStatusResidence).text = residence
        view.findViewById<TextView>(R.id.tvStatusNotes).text =
            if (notes.isBlank()) "No manager notes yet." else notes

        val submitted = view.findViewById<LinearLayout>(R.id.stageSubmitted)
        val review = view.findViewById<LinearLayout>(R.id.stageReview)
        val decision = view.findViewById<LinearLayout>(R.id.stageDecision)
        val room = view.findViewById<LinearLayout>(R.id.stageRoom)

        fun done(box: LinearLayout, iconId: Int) {
            box.setBackgroundResource(R.drawable.bg_status_done)
            view.findViewById<TextView>(iconId).text = "✓"
        }
        fun current(box: LinearLayout, iconId: Int) {
            box.setBackgroundResource(R.drawable.bg_status_current)
            view.findViewById<TextView>(iconId).text = "●"
        }

        when (status.trim().lowercase()) {
            "draft" -> current(submitted, R.id.tvSubmittedIcon)
            "submitted" -> {
                done(submitted, R.id.tvSubmittedIcon)
                current(review, R.id.tvReviewIcon)
            }
            "under review", "under_review", "underreview" -> {
                done(submitted, R.id.tvSubmittedIcon)
                done(review, R.id.tvReviewIcon)
                current(decision, R.id.tvDecisionIcon)
            }
            "approved" -> {
                done(submitted, R.id.tvSubmittedIcon)
                done(review, R.id.tvReviewIcon)
                done(decision, R.id.tvDecisionIcon)
                view.findViewById<TextView>(R.id.tvDecision).text = "Application\nApproved"
                if (roomId.isBlank()) current(room, R.id.tvRoomIcon)
                else {
                    done(room, R.id.tvRoomIcon)
                    view.findViewById<TextView>(R.id.tvRoom).text = "Room\nAssigned"
                }
            }
            "declined" -> {
                done(submitted, R.id.tvSubmittedIcon)
                done(review, R.id.tvReviewIcon)
                decision.setBackgroundResource(R.drawable.bg_status_declined)
                view.findViewById<TextView>(R.id.tvDecisionIcon).text = "×"
                view.findViewById<TextView>(R.id.tvDecision).text = "Application\nDeclined"
            }
            else -> {
                done(submitted, R.id.tvSubmittedIcon)
                current(review, R.id.tvReviewIcon)
            }
        }
    }

    companion object {
        fun newInstance(status: String, roomId: String, residence: String, notes: String) =
            ApplicationStatusFragment().apply {
                arguments = Bundle().apply {
                    putString("status", status)
                    putString("roomId", roomId)
                    putString("residence", residence)
                    putString("notes", notes)
                }
            }
    }
}
