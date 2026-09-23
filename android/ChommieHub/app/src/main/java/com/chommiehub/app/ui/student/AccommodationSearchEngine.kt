package com.chommiehub.app.ui.student

import com.chommiehub.app.data.model.manager.AccommodationResponse

/**
 * Available accommodation filters on the Student discovery screen.
 */
enum class HomeFilter {
    ALL,
    NEAR_CAMPUS,
    WIFI,
    LAUNDRY,
    VERIFIED
}

/**
 * Available sorting strategies for Student accommodation discovery.
 */
enum class HomeSort {
    NAME_ASCENDING,
    NAME_DESCENDING,
    AREA_ASCENDING,
    MOST_AMENITIES
}

/**
 * Applies Student accommodation search, location filtering, facility filtering and sorting.
 *
 * The filtering logic is kept outside the Fragment and ViewModel so it can be unit tested
 * without requiring Android instrumentation.
 */
object AccommodationSearchEngine {

    fun apply(
        accommodations: List<AccommodationResponse>,
        query: String,
        selectedArea: String,
        filter: HomeFilter,
        sort: HomeSort
    ): List<AccommodationResponse> {
        val normalisedQuery = query.trim()
        val normalisedArea = selectedArea.trim()

        val filtered = accommodations
            .filter { accommodation -> matchesQuery(accommodation, normalisedQuery) }
            .filter { accommodation -> matchesArea(accommodation, normalisedArea) }
            .filter { accommodation -> matchesFilter(accommodation, filter, normalisedArea) }

        return when (sort) {
            HomeSort.NAME_ASCENDING -> filtered.sortedBy { it.name.lowercase() }
            HomeSort.NAME_DESCENDING -> filtered.sortedByDescending { it.name.lowercase() }
            HomeSort.AREA_ASCENDING -> filtered.sortedWith(compareBy({ it.institutionArea.lowercase() }, { it.name.lowercase() }))
            HomeSort.MOST_AMENITIES -> filtered.sortedWith(compareByDescending<AccommodationResponse> { it.amenities.size }.thenBy { it.name.lowercase() })
        }
    }

    private fun matchesQuery(accommodation: AccommodationResponse, query: String): Boolean {
        if (query.isBlank()) {
            return true
        }

        return accommodation.name.contains(query, ignoreCase = true) ||
                accommodation.address.contains(query, ignoreCase = true) ||
                accommodation.institutionArea.contains(query, ignoreCase = true) ||
                accommodation.description.contains(query, ignoreCase = true) ||
                accommodation.amenities.any { amenity -> amenity.contains(query, ignoreCase = true) }
    }

    private fun matchesArea(accommodation: AccommodationResponse, selectedArea: String): Boolean {
        if (selectedArea.isBlank() || selectedArea.equals("All areas", ignoreCase = true)) {
            return true
        }

        return accommodation.institutionArea.equals(selectedArea, ignoreCase = true)
    }

    private fun matchesFilter(accommodation: AccommodationResponse, filter: HomeFilter, selectedArea: String): Boolean {
        return when (filter) {
            HomeFilter.ALL -> true
            HomeFilter.NEAR_CAMPUS -> if (selectedArea.isBlank() || selectedArea.equals("All areas", ignoreCase = true)) accommodation.institutionArea.isNotBlank() else accommodation.institutionArea.equals(selectedArea, ignoreCase = true)
            HomeFilter.WIFI -> accommodation.amenities.any { amenity -> amenity.contains("wi-fi", ignoreCase = true) || amenity.contains("wifi", ignoreCase = true) }
            HomeFilter.LAUNDRY -> accommodation.amenities.any { amenity -> amenity.contains("laundry", ignoreCase = true) }
            HomeFilter.VERIFIED -> accommodation.isActive
        }
    }
}