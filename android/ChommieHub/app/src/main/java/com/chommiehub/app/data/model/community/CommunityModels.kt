package com.chommiehub.app.data.model.community

/** Fields entered by residents; server derives the resident's identity and accommodation. */
data class CreateIncidentRequest(val category: String, val description: String, val location: String)

/** Review is restricted to the accommodation manager or a verified administrator. */
data class ReviewIncidentRequest(val status: String, val resolutionNote: String)

data class IncidentReport(
    val id: String, val studentId: String, val accommodationId: String,
    val category: String, val description: String, val location: String,
    val status: String, val resolutionNote: String = "",
    val photoUrl: String = "", val createdAtUtc: String = ""
)

/** The API determines whether a notice targets one residence or the entire platform. */
data class CreateAnnouncementRequest(
    val accommodationId: String, val type: String, val title: String, val description: String
)

data class Announcement(
    val id: String, val accommodationId: String, val type: String,
    val title: String, val description: String, val createdAtUtc: String = ""
)

/** Aggregate counts, intentionally excluding personal information. */
data class OperationalReport(
    val scope: String, val generatedAtUtc: String,
    val listings: Int, val pendingListings: Int, val approvedListings: Int,
    val activeResidents: Int, val applications: Int, val openApplications: Int,
    val incidents: Int, val openIncidents: Int, val announcements: Int
)
