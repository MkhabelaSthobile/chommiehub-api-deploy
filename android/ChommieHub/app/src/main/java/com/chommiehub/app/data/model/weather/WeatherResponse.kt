package com.chommiehub.app.data.model.weather

/**
 * Weather information returned by the ChommieHub REST API.
 *
 * Android does not call Open-Meteo directly. The custom ASP.NET Core API
 * performs that integration and returns only the information required by
 * ChommieHub.
 */
data class WeatherResponse(
    val accommodationId: String,
    val accommodationName: String,
    val temperatureC: Double,
    val precipitationMm: Double,
    val windSpeedKph: Double,
    val weatherCode: Int,
    val summary: String,
    val outdoorMaintenanceAdvisory: String,
    val retrievedAtUtc: String
)