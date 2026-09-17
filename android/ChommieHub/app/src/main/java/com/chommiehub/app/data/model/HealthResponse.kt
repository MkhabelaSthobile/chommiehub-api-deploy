package com.chommiehub.app.data.model

/**
 * Represents the response returned by the backend health endpoint
 */
data class HealthResponse(
    val status: String,
    val service: String,
    val timestampUtc: String
)