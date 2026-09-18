package com.chommiehub.app.data.repository

import com.chommiehub.app.data.model.manager.ManagerApplicationDetailResponse
import com.chommiehub.app.data.model.manager.ManagerDashboardResponse
import com.chommiehub.app.data.model.manager.ReviewApplicationRequest
import com.chommiehub.app.data.model.manager.RoomResponse
import com.chommiehub.app.data.remote.ChommieHubApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests the Android manager repository without requiring a live REST API.
 */
class ManagerRepositoryTest {

    @Test
    fun getAvailableRooms_returnsOnlyAvailableRooms() {
        runBlocking {

            // Arrange
            val api =
                mock<ChommieHubApi>()

            val rooms =
                listOf(
                    RoomResponse(
                        id = "room-1",
                        accommodationId =
                            "accommodation-1",
                        roomType = "Single Room",
                        price = 5500.0,
                        capacity = 1,
                        availability = true
                    ),
                    RoomResponse(
                        id = "room-2",
                        accommodationId =
                            "accommodation-1",
                        roomType = "Double Room",
                        price = 4200.0,
                        capacity = 2,
                        availability = false
                    ),
                    RoomResponse(
                        id = "room-3",
                        accommodationId =
                            "accommodation-1",
                        roomType = "Studio",
                        price = 6500.0,
                        capacity = 1,
                        availability = true
                    )
                )

            whenever(
                api.getRooms(
                    "accommodation-1"
                )
            ).thenReturn(
                rooms
            )

            val repository =
                ManagerRepository(api)

            // Act
            val result =
                repository.getAvailableRooms(
                    "accommodation-1"
                )

            // Assert
            assertEquals(
                2,
                result.size
            )

            assertTrue(
                result.all {
                    it.availability
                }
            )
        }
    }

    @Test
    fun getDashboard_returnsResponseFromApi() {
        runBlocking {

            // Arrange
            val api =
                mock<ChommieHubApi>()

            val expected =
                ManagerDashboardResponse(
                    accommodationId =
                        "accommodation-1",
                    totalApplications = 4,
                    pendingApplications = 2,
                    approvedApplications = 1,
                    declinedApplications = 1,
                    activeResidents = 3,
                    totalRooms = 5,
                    availableRooms = 2,
                    unavailableRooms = 3
                )

            whenever(
                api.getManagerDashboard(
                    "accommodation-1"
                )
            ).thenReturn(
                expected
            )

            val repository =
                ManagerRepository(api)

            // Act
            val result =
                repository.getDashboard(
                    "accommodation-1"
                )

            // Assert
            assertEquals(
                expected,
                result
            )

            verify(api)
                .getManagerDashboard(
                    "accommodation-1"
                )
        }
    }

    @Test
    fun reviewApplication_sendsManagerDecisionAndNotes() {
        runBlocking {

            // Arrange
            val api =
                mock<ChommieHubApi>()

            val response =
                ManagerApplicationDetailResponse(
                    id = "application-1",
                    studentId = "student-1",
                    accommodationId =
                        "accommodation-1",
                    roomTypePreference =
                        "Single Room",
                    rentalPeriod =
                        "2027 Academic Year",
                    supportingInfo =
                        "Test application",
                    status = "Approved",
                    assignedRoomId = "",
                    reviewNotes =
                        "Verified by manager.",
                    dateSubmittedUtc =
                        "2026-09-18T00:00:00Z",
                    dateReviewedUtc =
                        "2026-09-18T01:00:00Z"
                )

            whenever(
                api.reviewApplication(
                    eq("application-1"),
                    any()
                )
            ).thenReturn(
                response
            )

            val repository =
                ManagerRepository(api)

            // Act
            val result =
                repository.reviewApplication(
                    applicationId =
                        "application-1",
                    decision =
                        "Approved",
                    reviewNotes =
                        "Verified by manager."
                )

            // Assert
            assertEquals(
                "Approved",
                result.status
            )

            val requestCaptor =
                argumentCaptor<
                        ReviewApplicationRequest
                        >()

            verify(api)
                .reviewApplication(
                    eq("application-1"),
                    requestCaptor.capture()
                )

            assertEquals(
                "Approved",
                requestCaptor.firstValue
                    .decision
            )

            assertEquals(
                "Verified by manager.",
                requestCaptor.firstValue
                    .reviewNotes
            )
        }
    }
}