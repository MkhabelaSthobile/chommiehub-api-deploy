
package com.chommiehub.app.ui.student

import com.chommiehub.app.data.model.manager.AccommodationResponse
import com.chommiehub.app.data.model.manager.RoomResponse

enum class HomeFilter {
    ALL,
    WIFI,
    LAUNDRY
}

enum class HomeSort {
    NAME_ASCENDING,
    NAME_DESCENDING,
    AREA_ASCENDING,
    MOST_AMENITIES
}

object AccommodationSearchEngine {

    fun apply(
        accommodations: List<AccommodationResponse>,
        query: String,
        selectedArea: String,
        filter: HomeFilter,
        sort: HomeSort,
        roomsByAccommodation: Map<String, List<RoomResponse>> = emptyMap(),
        maxPrice: Double? = null
    ): List<AccommodationResponse> {
        val normalisedQuery = query.trim()
        val normalisedArea = selectedArea.trim()

        val filtered = accommodations
            .filter { matchesQuery(it, normalisedQuery) }
            .filter { matchesArea(it, normalisedArea) }
            .filter { matchesFilter(it, filter) }
            .filter { accommodation ->
                if (maxPrice == null) {
                    true
                } else {
                    roomsByAccommodation[accommodation.id]
                        .orEmpty()
                        .any { room ->
                            room.availability &&
                                    room.price >= 0.0 &&
                                    room.price <= maxPrice
                        }
                }
            }

        return when (sort) {
            HomeSort.NAME_ASCENDING ->
                filtered.sortedBy { it.name.lowercase() }

            HomeSort.NAME_DESCENDING ->
                filtered.sortedByDescending { it.name.lowercase() }

            HomeSort.AREA_ASCENDING ->
                filtered.sortedWith(
                    compareBy(
                        { it.institutionArea.lowercase() },
                        { it.name.lowercase() }
                    )
                )

            HomeSort.MOST_AMENITIES ->
                filtered.sortedWith(
                    compareByDescending<AccommodationResponse> {
                        it.amenities.size
                    }.thenBy { it.name.lowercase() }
                )
        }
    }

    private fun matchesQuery(
        accommodation: AccommodationResponse,
        query: String
    ): Boolean {
        if (query.isBlank()) return true

        return accommodation.name.contains(query, ignoreCase = true) ||
                accommodation.address.contains(query, ignoreCase = true) ||
                accommodation.institutionArea.contains(query, ignoreCase = true) ||
                accommodation.description.contains(query, ignoreCase = true) ||
                accommodation.amenities.any {
                    it.contains(query, ignoreCase = true)
                }
    }

    private fun matchesArea(
        accommodation: AccommodationResponse,
        selectedArea: String
    ): Boolean {
        if (
            selectedArea.isBlank() ||
            selectedArea.equals("All areas", ignoreCase = true)
        ) {
            return true
        }

        return accommodation.institutionArea.equals(
            selectedArea,
            ignoreCase = true
        )
    }

    private fun matchesFilter(
        accommodation: AccommodationResponse,
        filter: HomeFilter
    ): Boolean {
        return when (filter) {
            HomeFilter.ALL -> true

            HomeFilter.WIFI -> accommodation.amenities.any {
                it.contains("wi-fi", ignoreCase = true) ||
                        it.contains("wifi", ignoreCase = true)
            }

            HomeFilter.LAUNDRY -> accommodation.amenities.any {
                it.contains("laundry", ignoreCase = true)
            }
        }
    }
}
