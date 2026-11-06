package com.chommiehub.app.ui.student

import com.chommiehub.app.data.model.manager.AccommodationResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccommodationSearchEngineTest {

    private val residences = listOf(
        accommodation(
            id = "1",
            name = "Campus View",
            address = "10 Main Road",
            area = "IIE MSA",
            amenities = listOf("Wi-Fi", "Laundry", "Study Area")
        ),
        accommodation(
            id = "2",
            name = "Rosebank Student Living",
            address = "20 Oxford Road",
            area = "Rosebank",
            amenities = listOf("Wi-Fi", "Gym")
        ),
        accommodation(
            id = "3",
            name = "Budget Residence",
            address = "30 College Street",
            area = "IIE MSA",
            amenities = listOf("Laundry"),
            active = false
        )
    )

    @Test
    fun apply_SearchesNameAddressAreaDescriptionAndAmenities() {
        val result = AccommodationSearchEngine.apply(residences, "gym", "All areas", HomeFilter.ALL, HomeSort.NAME_ASCENDING)

        assertEquals(1, result.size)
        assertEquals("Rosebank Student Living", result.first().name)
    }

    @Test
    fun apply_FiltersBySelectedInstitutionArea() {
        val result = AccommodationSearchEngine.apply(residences, "", "IIE MSA", HomeFilter.ALL, HomeSort.NAME_ASCENDING)

        assertEquals(2, result.size)
        assertTrue(result.all { it.institutionArea == "IIE MSA" })
    }

    @Test
    fun apply_WifiFilterReturnsOnlyResidencesWithWifi() {
        val result = AccommodationSearchEngine.apply(residences, "", "All areas", HomeFilter.WIFI, HomeSort.NAME_ASCENDING)

        assertEquals(2, result.size)
        assertTrue(result.all { accommodation -> accommodation.amenities.any { it.contains("wi-fi", ignoreCase = true) || it.contains("wifi", ignoreCase = true) } })
    }

    @Test
    fun apply_LaundryFilterReturnsOnlyResidencesWithLaundry() {
        val result = AccommodationSearchEngine.apply(
            residences,
            "",
            "All areas",
            HomeFilter.LAUNDRY,
            HomeSort.NAME_ASCENDING
        )

        assertEquals(2, result.size)
        assertTrue(result.all { accommodation ->
            accommodation.amenities.any {
                it.contains("laundry", ignoreCase = true)
            }
        })
    }

    @Test
    fun apply_MostAmenitiesSortsLargestAmenityListFirst() {
        val result = AccommodationSearchEngine.apply(residences, "", "All areas", HomeFilter.ALL, HomeSort.MOST_AMENITIES)

        assertEquals("Campus View", result.first().name)
        assertEquals(3, result.first().amenities.size)
    }

    private fun accommodation(
        id: String,
        name: String,
        address: String,
        area: String,
        amenities: List<String>,
        active: Boolean = true
    ): AccommodationResponse {
        return AccommodationResponse(
            id = id,
            name = name,
            address = address,
            latitude = -26.0,
            longitude = 28.0,
            description = "$name student accommodation",
            phoneNumber = "0100000000",
            amenities = amenities,
            institutionArea = area,
            isActive = active
        )
    }
}