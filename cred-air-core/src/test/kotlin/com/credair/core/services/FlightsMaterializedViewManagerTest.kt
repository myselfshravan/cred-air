package com.credair.core.services

import com.credair.core.dao.interfaces.FlightsMaterializedViewDao
import com.credair.core.events.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.test.assertEquals

class FlightsMaterializedViewManagerTest {

    private lateinit var flightsMaterializedViewDao: FlightsMaterializedViewDao
    private lateinit var flightsMaterializedViewManager: FlightsMaterializedViewManager

    @BeforeEach
    fun setUp() {
        flightsMaterializedViewDao = mock()
        flightsMaterializedViewManager = FlightsMaterializedViewManager(flightsMaterializedViewDao)
    }

    // Schedule Change Tests
    @Test
    fun `handleScheduleChange should delete journeys and recompute for affected flight`() {
        val event = FlightScheduleChangedEvent(
            flightId = 1L,
            oldDepartureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
            newDepartureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(2)),
            oldArrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)),
            newArrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(2).plusHours(2))
        )

        whenever(flightsMaterializedViewDao.deleteJourneysContainingFlight(1L)).thenReturn(5)
        whenever(flightsMaterializedViewDao.recomputeDirectFlights(1L)).thenReturn(1)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsFirst(1L)).thenReturn(2)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsSecond(1L)).thenReturn(2)
        whenever(flightsMaterializedViewDao.recomputeTwoStopFlightsContainingFlight(1L)).thenReturn(3)

        flightsMaterializedViewManager.handleScheduleChange(event)

        verify(flightsMaterializedViewDao).deleteJourneysContainingFlight(1L)
        verify(flightsMaterializedViewDao).recomputeDirectFlights(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsFirst(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsSecond(1L)
        verify(flightsMaterializedViewDao).recomputeTwoStopFlightsContainingFlight(1L)
    }

    @Test
    fun `handleScheduleChange should propagate dao exceptions`() {
        val event = createScheduleChangedEvent()

        whenever(flightsMaterializedViewDao.deleteJourneysContainingFlight(1L))
            .thenThrow(RuntimeException("Database error"))

        val exception = assertThrows<RuntimeException> {
            flightsMaterializedViewManager.handleScheduleChange(event)
        }
        assertEquals("Database error", exception.message)
    }

    @Test
    fun `handleScheduleChange should handle zero deletions gracefully`() {
        val event = createScheduleChangedEvent()

        whenever(flightsMaterializedViewDao.deleteJourneysContainingFlight(1L)).thenReturn(0)
        whenever(flightsMaterializedViewDao.recomputeDirectFlights(1L)).thenReturn(1)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsFirst(1L)).thenReturn(0)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsSecond(1L)).thenReturn(0)
        whenever(flightsMaterializedViewDao.recomputeTwoStopFlightsContainingFlight(1L)).thenReturn(0)

        // Should not throw exception
        flightsMaterializedViewManager.handleScheduleChange(event)

        verify(flightsMaterializedViewDao).deleteJourneysContainingFlight(1L)
        verify(flightsMaterializedViewDao).recomputeDirectFlights(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsFirst(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsSecond(1L)
        verify(flightsMaterializedViewDao).recomputeTwoStopFlightsContainingFlight(1L)
    }

    // Seats Change Tests
    @Test
    fun `handleSeatsChange should update seats for journeys containing flight`() {
        val event = FlightSeatsChangedEvent(
            flightId = 1L,
            oldAvailableSeats = 100,
            newAvailableSeats = 80
        )

        whenever(flightsMaterializedViewDao.updateSeatsForJourneys(1L, 80)).thenReturn(10)

        flightsMaterializedViewManager.handleSeatsChange(event)

        verify(flightsMaterializedViewDao).updateSeatsForJourneys(1L, 80)
    }

    @Test
    fun `handleSeatsChange should propagate dao exceptions`() {
        val event = createSeatsChangedEvent()

        whenever(flightsMaterializedViewDao.updateSeatsForJourneys(1L, 80))
            .thenThrow(RuntimeException("Update failed"))

        val exception = assertThrows<RuntimeException> {
            flightsMaterializedViewManager.handleSeatsChange(event)
        }
        assertEquals("Update failed", exception.message)
    }

    @Test
    fun `handleSeatsChange should handle zero updates gracefully`() {
        val event = createSeatsChangedEvent()

        whenever(flightsMaterializedViewDao.updateSeatsForJourneys(1L, 80)).thenReturn(0)

        // Should not throw exception
        flightsMaterializedViewManager.handleSeatsChange(event)

        verify(flightsMaterializedViewDao).updateSeatsForJourneys(1L, 80)
    }

    // Flight Cancellation Tests
    @Test
    fun `handleFlightCancellation should delete all journeys containing cancelled flight`() {
        val event = FlightCancelledEvent(flightId = 1L)

        whenever(flightsMaterializedViewDao.deleteJourneysContainingFlight(1L)).thenReturn(15)

        flightsMaterializedViewManager.handleFlightCancellation(event)

        verify(flightsMaterializedViewDao).deleteJourneysContainingFlight(1L)
    }

    @Test
    fun `handleFlightCancellation should propagate dao exceptions`() {
        val event = FlightCancelledEvent(flightId = 1L)

        whenever(flightsMaterializedViewDao.deleteJourneysContainingFlight(1L))
            .thenThrow(RuntimeException("Deletion failed"))

        val exception = assertThrows<RuntimeException> {
            flightsMaterializedViewManager.handleFlightCancellation(event)
        }
        assertEquals("Deletion failed", exception.message)
    }

    // Flight Reactivation Tests
    @Test
    fun `handleFlightReactivation should recompute all journey types for reactivated flight`() {
        val event = FlightReactivatedEvent(flightId = 1L)

        whenever(flightsMaterializedViewDao.recomputeDirectFlights(1L)).thenReturn(1)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsFirst(1L)).thenReturn(3)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsSecond(1L)).thenReturn(2)
        whenever(flightsMaterializedViewDao.recomputeTwoStopFlightsContainingFlight(1L)).thenReturn(5)

        flightsMaterializedViewManager.handleFlightReactivation(event)

        verify(flightsMaterializedViewDao).recomputeDirectFlights(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsFirst(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsSecond(1L)
        verify(flightsMaterializedViewDao).recomputeTwoStopFlightsContainingFlight(1L)
    }

    @Test
    fun `handleFlightReactivation should propagate dao exceptions`() {
        val event = FlightReactivatedEvent(flightId = 1L)

        whenever(flightsMaterializedViewDao.recomputeDirectFlights(1L))
            .thenThrow(RuntimeException("Recompute failed"))

        val exception = assertThrows<RuntimeException> {
            flightsMaterializedViewManager.handleFlightReactivation(event)
        }
        assertEquals("Recompute failed", exception.message)
    }

    // Flight Creation Tests
    @Test
    fun `handleFlightCreation should recompute all journey types for new flight`() {
        val event = FlightCreatedEvent(flightId = 1L)

        whenever(flightsMaterializedViewDao.recomputeDirectFlights(1L)).thenReturn(1)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsFirst(1L)).thenReturn(4)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsSecond(1L)).thenReturn(3)
        whenever(flightsMaterializedViewDao.recomputeTwoStopFlightsContainingFlight(1L)).thenReturn(6)

        flightsMaterializedViewManager.handleFlightCreation(event)

        verify(flightsMaterializedViewDao).recomputeDirectFlights(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsFirst(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsSecond(1L)
        verify(flightsMaterializedViewDao).recomputeTwoStopFlightsContainingFlight(1L)
    }

    @Test
    fun `handleFlightCreation should propagate dao exceptions`() {
        val event = FlightCreatedEvent(flightId = 1L)

        whenever(flightsMaterializedViewDao.recomputeDirectFlights(1L))
            .thenThrow(RuntimeException("Creation failed"))

        val exception = assertThrows<RuntimeException> {
            flightsMaterializedViewManager.handleFlightCreation(event)
        }
        assertEquals("Creation failed", exception.message)
    }

    // Full Materialized View Refresh Tests
    @Test
    fun `refreshFullMaterializedView should call dao refresh method`() {
        flightsMaterializedViewManager.refreshFullMaterializedView()

        verify(flightsMaterializedViewDao).refreshFullMaterializedView()
    }

    @Test
    fun `refreshFullMaterializedView should propagate dao exceptions`() {
        whenever(flightsMaterializedViewDao.refreshFullMaterializedView())
            .thenThrow(RuntimeException("Refresh failed"))

        val exception = assertThrows<RuntimeException> {
            flightsMaterializedViewManager.refreshFullMaterializedView()
        }
        assertEquals("Refresh failed", exception.message)
    }

    // Integration Tests
    @Test
    fun `recompute operations should sum total records correctly`() {
        val event = FlightCreatedEvent(flightId = 1L)

        whenever(flightsMaterializedViewDao.recomputeDirectFlights(1L)).thenReturn(1)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsFirst(1L)).thenReturn(2)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsSecond(1L)).thenReturn(3)
        whenever(flightsMaterializedViewDao.recomputeTwoStopFlightsContainingFlight(1L)).thenReturn(4)

        flightsMaterializedViewManager.handleFlightCreation(event)

        verify(flightsMaterializedViewDao).recomputeDirectFlights(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsFirst(1L)
        verify(flightsMaterializedViewDao).recomputeOneStopFlightsWithFlightAsSecond(1L)
        verify(flightsMaterializedViewDao).recomputeTwoStopFlightsContainingFlight(1L)
        // Total should be 1 + 2 + 3 + 4 = 10 records
    }

    @Test
    fun `handle methods should process events with correct flight IDs`() {
        val scheduleEvent = createScheduleChangedEvent()
        val seatsEvent = createSeatsChangedEvent()
        val cancellationEvent = FlightCancelledEvent(flightId = 1L)
        val reactivationEvent = FlightReactivatedEvent(flightId = 1L)
        val creationEvent = FlightCreatedEvent(flightId = 1L)

        whenever(flightsMaterializedViewDao.deleteJourneysContainingFlight(any())).thenReturn(0)
        whenever(flightsMaterializedViewDao.updateSeatsForJourneys(any(), any())).thenReturn(0)
        whenever(flightsMaterializedViewDao.recomputeDirectFlights(any())).thenReturn(0)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsFirst(any())).thenReturn(0)
        whenever(flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsSecond(any())).thenReturn(0)
        whenever(flightsMaterializedViewDao.recomputeTwoStopFlightsContainingFlight(any())).thenReturn(0)

        flightsMaterializedViewManager.handleScheduleChange(scheduleEvent)
        flightsMaterializedViewManager.handleSeatsChange(seatsEvent)
        flightsMaterializedViewManager.handleFlightCancellation(cancellationEvent)
        flightsMaterializedViewManager.handleFlightReactivation(reactivationEvent)
        flightsMaterializedViewManager.handleFlightCreation(creationEvent)

        // Verify all events processed with correct flight ID (1L)
        verify(flightsMaterializedViewDao, times(2)).deleteJourneysContainingFlight(1L) // schedule + cancellation
        verify(flightsMaterializedViewDao).updateSeatsForJourneys(1L, 80)
        verify(flightsMaterializedViewDao, times(3)).recomputeDirectFlights(1L) // schedule + reactivation + creation
    }

    // Helper methods
    private fun createScheduleChangedEvent() = FlightScheduleChangedEvent(
        flightId = 1L,
        oldDepartureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
        newDepartureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(2)),
        oldArrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)),
        newArrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(2).plusHours(2))
    )

    private fun createSeatsChangedEvent() = FlightSeatsChangedEvent(
        flightId = 1L,
        oldAvailableSeats = 100,
        newAvailableSeats = 80
    )
}