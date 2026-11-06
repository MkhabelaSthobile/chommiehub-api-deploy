package com.chommiehub.app.ui.community

import com.chommiehub.app.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.data.model.community.*
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.ui.manager.CurrentManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Resident incident reporting; scoped residence/platform announcements; and
 * manager/administrator aggregate reporting. The server enforces role and
 * accommodation ownership on every request, regardless of this Activity's mode.
 */
class CommunityActivity : AppCompatActivity() {
    companion object {
        const val MODE = "community_mode"
        const val INCIDENTS = "incidents"
        const val ANNOUNCEMENTS = "announcements"
        const val REPORTS = "reports"
    }

    private lateinit var panel: LinearLayout
    private val api get() = ApiClient.api
    private var isAdmin = false
    private var managerAccommodation = ""
    private var isResident = false
    private var chosenPhoto: Uri? = null
    private val choosePhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        chosenPhoto = uri
        if (uri != null) toast("Photo selected for this incident.")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp(18)
            setPadding(pad, pad, pad, pad)
        }
        val scroll = ScrollView(this).apply {
            fillViewport = true
            addView(panel)
        }
        setContentView(scroll)
        heading("Loading secure ChommieHub services…")
        lifecycleScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                    ?: error("Sign in first.")
                isAdmin = user.getIdToken(true).await().claims["role"] == "admin"
                if (!isAdmin) {
                    try {
                        val manager = api.getManagerByFirebaseUid(user.uid)
                        if (manager.firebaseUid == user.uid) {
                            managerAccommodation = manager.accommodationId
                        }
                    } catch (_: retrofit2.HttpException) { /* An ordinary student has no manager profile. */ }
                    try {
                        val resident = api.getResidentByStudentId(user.uid)
                        isResident = resident.id.isNotBlank()
                    } catch (_: retrofit2.HttpException) { /* A student is not always a resident. */ }
                }
                showMode()
            } catch (error: Exception) {
                panel.removeAllViews()
                heading("Could not verify access")
                text(error.message ?: "Check your internet connection and sign in again.")
                button("Retry") { recreate() }
            }
        }
    }

    private fun showMode() = when (intent.getStringExtra(MODE)) {
        INCIDENTS -> showIncidents()
        ANNOUNCEMENTS -> showAnnouncements()
        REPORTS -> showReports()
        else -> { panel.removeAllViews(); heading("Unknown service") }
    }

    private fun showIncidents() {
        panel.removeAllViews()
        heading(getString(R.string.community_incidents))
        text(getString(R.string.community_incident_intro))
        button("Refresh incidents") { showIncidents() }
        if (isResident && !isAdmin && managerAccommodation.isBlank()) {
            button(getString(R.string.community_report_incident)) { incidentDialog() }
        }
        lifecycleScope.launch {
            try {
                val records = if (isAdmin || managerAccommodation.isNotBlank())
                    api.getManagedIncidents() else api.getMyIncidents()
                if (records.isEmpty()) text(getString(R.string.community_incident_none))
                records.forEach { item ->
                    val card = button("${item.category} — ${item.status}\n${item.location}\n${item.description.take(160)}") {
                        if (isAdmin || managerAccommodation.isNotBlank()) reviewDialog(item)
                        else AlertDialog.Builder(this@CommunityActivity)
                            .setTitle(item.category)
                            .setMessage("Status: ${item.status}\n${item.resolutionNote}")
                            .setPositiveButton("Close", null).show()
                    }
                    if (item.photoUrl.isNotBlank()) button("View photo for ${item.category} incident") {
                        viewIncidentPhoto(item.id)
                    }
                    card.contentDescription = "Incident ${item.category}, ${item.status}"
                }
            } catch (error: Exception) { showError(error) }
        }
    }

    private fun incidentDialog() {
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(10), dp(18), 0) }
        val categories = arrayOf("Safety", "Security", "Damage", "Noise", "Other")
        val category = Spinner(this)
        category.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        form.addView(category)
        val location = field(form, "Where did it happen? (e.g. main entrance)")
        val description = field(form, "Describe what happened", true)
        chosenPhoto = null
        form.addView(Button(this).apply {
            text = getString(R.string.community_choose_photo)
            setOnClickListener { choosePhoto.launch("image/*") }
        })
        val dialog = AlertDialog.Builder(this).setTitle("Report an incident").setView(form)
            .setNegativeButton("Cancel", null).setPositiveButton("Submit", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val where = location.text.toString().trim()
                val details = description.text.toString().trim()
                if (where.isEmpty() || where.length > 200 || details.length !in 8..2000) {
                    toast("Enter a location and a description (8–2000 characters).")
                } else {
                    dialog.dismiss()
                    lifecycleScope.launch {
                        try {
                            val created = api.createIncident(CreateIncidentRequest(category.selectedItem.toString(), details, where))
                            val photo = chosenPhoto
                            if (photo != null) {
                                try { uploadPhoto(created.id, photo) }
                                catch (uploadError: Exception) {
                                    toast("Incident saved, but photo upload failed: ${uploadError.message}")
                                }
                            }
                            chosenPhoto = null
                            toast("Incident submitted")
                            showIncidents()
                        } catch (error: Exception) { showError(error) }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun reviewDialog(item: IncidentReport) {
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(10), dp(18), 0) }
        val statuses = arrayOf("Submitted", "Under Review", "Completed")
        val status = Spinner(this).apply {
            adapter = ArrayAdapter(this@CommunityActivity, android.R.layout.simple_spinner_dropdown_item, statuses)
            setSelection(statuses.indexOf(item.status).coerceAtLeast(0))
        }
        form.addView(status)
        val note = field(form, "Resolution note (required to complete)", true)
        note.setText(item.resolutionNote)
        val dialog = AlertDialog.Builder(this).setTitle("Review ${item.category} incident")
            .setMessage(item.description).setView(form)
            .setNegativeButton("Cancel", null).setPositiveButton("Save", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newStatus = status.selectedItem.toString()
                val memo = note.text.toString().trim()
                if ((newStatus == "Completed" && memo.isBlank()) || memo.length > 1000) {
                    toast("A resolution note is required (up to 1000 characters).")
                } else {
                    dialog.dismiss()
                    lifecycleScope.launch {
                        try {
                            api.reviewIncident(item.id, ReviewIncidentRequest(newStatus, memo))
                            toast("Incident status updated")
                            showIncidents()
                        } catch (error: Exception) { showError(error) }
                    }
                }
            }
        }
        dialog.show()
    }

    private suspend fun uploadPhoto(incidentId: String, uri: Uri) {
        val stream = contentResolver.openInputStream(uri) ?: error("Unable to open photo")
        // Reject oversized photos before allocating an unbounded byte buffer.
        val output = java.io.ByteArrayOutputStream()
        stream.use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                output.write(buffer, 0, read)
                if (output.size() > 5 * 1024 * 1024) error("Photo exceeds 5 MB")
            }
        }
        val mime = contentResolver.getType(uri) ?: "application/octet-stream"
        val body = output.toByteArray().toRequestBody(mime.toMediaType())
        val part = MultipartBody.Part.createFormData("image", "incident-photo", body)
        val result = api.uploadIncidentPhoto(incidentId, part)
        if (!result.isSuccessful) error("Server rejected the photo (${result.code()})")
    }

    private fun viewIncidentPhoto(incidentId: String) {
        lifecycleScope.launch {
            try {
                val bytes = api.getIncidentPhoto(incidentId).bytes()
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: error("The uploaded image could not be decoded.")
                val imageView = ImageView(this@CommunityActivity).apply {
                    setImageBitmap(bitmap)
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                AlertDialog.Builder(this@CommunityActivity)
                    .setTitle("Incident photo").setView(imageView)
                    .setPositiveButton("Close", null).show()
            } catch (error: Exception) { showError(error) }
        }
    }

    private fun showAnnouncements() {
        panel.removeAllViews()
        heading(getString(R.string.community_announcements))
        text(getString(R.string.community_announcements_intro))
        button("Refresh announcements") { showAnnouncements() }
        if (isAdmin || managerAccommodation.isNotBlank()) {
            button(getString(R.string.community_create_announcement)) { announcementDialog() }
        }
        lifecycleScope.launch {
            try {
                val notices = api.getAnnouncements()
                if (notices.isEmpty()) text(getString(R.string.community_notice_none))
                notices.forEach { notice ->
                    heading(notice.title, 17)
                    text("${notice.type} • ${if (notice.accommodationId.isBlank()) "Platform-wide" else "Residence"}\n${notice.description}\n${notice.createdAtUtc.take(10)}")
                }
            } catch (error: Exception) { showError(error) }
        }
    }

    private fun announcementDialog() {
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(10), dp(18), 0) }
        val types = arrayOf("General", "Maintenance", "Safety", "Meeting", "Interruption", "Weather")
        val type = Spinner(this)
        type.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        form.addView(type)
        val title = field(form, "Notice title")
        val description = field(form, "Notice details", true)
        // Admin defaults to a global notice; managers can post only to their own residence.
        val accommodation = if (isAdmin) field(form, "Accommodation ID (blank = all users)") else null
        val dialog = AlertDialog.Builder(this).setTitle("Publish announcement").setView(form)
            .setNegativeButton("Cancel", null).setPositiveButton("Publish", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val heading = title.text.toString().trim()
                val body = description.text.toString().trim()
                if (heading.length !in 3..120 || body.length !in 4..2000) {
                    toast("A title and description are required.")
                } else {
                    dialog.dismiss()
                    lifecycleScope.launch {
                        try {
                            api.createAnnouncement(CreateAnnouncementRequest(
                                if (isAdmin) accommodation?.text?.toString()?.trim().orEmpty() else managerAccommodation,
                                type.selectedItem.toString(), heading, body))
                            toast("Announcement published")
                            showAnnouncements()
                        } catch (error: Exception) { showError(error) }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showReports() {
        panel.removeAllViews()
        heading(getString(R.string.community_reports))
        text(getString(R.string.community_reports_intro))
        button("Refresh report") { showReports() }
        lifecycleScope.launch {
            try {
                val report = api.getOperationalReport()
                val display = "Scope: ${report.scope}\n" +
                    "Listings: ${report.listings}\nPending listings: ${report.pendingListings}\n" +
                    "Approved listings: ${report.approvedListings}\n" +
                    "Active residents: ${report.activeResidents}\n" +
                    "Applications: ${report.applications}\nOpen applications: ${report.openApplications}\n" +
                    "Incidents: ${report.incidents}\nOpen incidents: ${report.openIncidents}\n" +
                    "Announcements: ${report.announcements}\nGenerated: ${report.generatedAtUtc}"
                text(display)
                button(getString(R.string.community_copy_summary)) {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("ChommieHub report", display))
                    toast("Summary copied")
                }
            } catch (error: Exception) { showError(error) }
        }
    }

    private fun heading(value: String, size: Int = 24) {
        panel.addView(TextView(this).apply {
            text = value; textSize = size.toFloat(); setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(10))
        })
    }
    private fun text(value: String) {
        panel.addView(TextView(this).apply {
            text = value; textSize = 15f; setPadding(0, dp(6), 0, dp(12))
        })
    }
    private fun button(value: String, action: () -> Unit): Button = Button(this).apply {
        text = value; isAllCaps = false; minHeight = dp(48)
        setOnClickListener { action() }
        panel.addView(this, LinearLayout.LayoutParams(-1, -2))
    }
    private fun field(parent: LinearLayout, hint: String, multiline: Boolean = false): EditText =
        EditText(this).apply {
            this.hint = hint; if (multiline) { minLines = 3; maxLines = 6 }
            parent.addView(this, LinearLayout.LayoutParams(-1, -2))
        }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun toast(value: String) = Toast.makeText(this, value, Toast.LENGTH_LONG).show()
    private fun showError(error: Exception) = toast(error.message ?: "Unable to load data. Try again.")
}
