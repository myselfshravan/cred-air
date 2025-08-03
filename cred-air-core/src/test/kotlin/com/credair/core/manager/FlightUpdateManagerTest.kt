package com.credair.core.manager

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.events.*
import com.credair.core.model.Flight
import com.credair.core.services.FlightsMaterializedViewManager
import com.credair.core.util.FlightChangeDetector
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FlightUpdateManagerTest {

    private lateinit var flightDao: FlightDao
    private lateinit var flightsMaterializedViewManager: FlightsMaterializedViewManager
    private lateinit var flightUpdateManager: FlightUpdateManager

    @BeforeEach
    fun setUp() {
        flightDao = mock()
        flightsMaterializedViewManager = mock()
        flightUpdateManager = FlightUpdateManager(flightDao, flightsMaterializedViewManager)
    }

    @Test
    fun `updateFlight should successfully update flight with schedule change`() {
        val oldFlight = createValidFlight()
        val newDepartureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(2))
        val updatedFlight = oldFlight.copy(departureTime = newDepartureTime)

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        val result = flightUpdateManager.updateFlight(updatedFlight)

        assertEquals(updatedFlight, result)
        verify(flightDao).findById(1L)
        verify(flightDao).update(updatedFlight)
        verify(flightsMaterializedViewManager).handleScheduleChange(any())
    }

    @Test
    fun `updateFlight should successfully update flight with seats change`() {
        val oldFlight = createValidFlight()
        val updatedFlight = oldFlight.copy(availableSeats = 80)

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        val result = flightUpdateManager.updateFlight(updatedFlight)

        assertEquals(updatedFlight, result)
        verify(flightDao).findById(1L)
        verify(flightDao).update(updatedFlight)
        verify(flightsMaterializedViewManager).handleSeatsChange(any())
    }

    @Test
    fun `updateFlight should successfully update flight with cancellation`() {
        val oldFlight = createValidFlight().copy(active = true)
        val updatedFlight = oldFlight.copy(active = false)

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        val result = flightUpdateManager.updateFlight(updatedFlight)

        assertEquals(updatedFlight, result)
        verify(flightDao).findById(1L)
        verify(flightDao).update(updatedFlight)
        verify(flightsMaterializedViewManager).handleFlightCancellation(any())
    }

    @Test
    fun `updateFlight should successfully update flight with reactivation`() {
        val oldFlight = createValidFlight().copy(active = false)
        val updatedFlight = oldFlight.copy(active = true)

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        val result = flightUpdateManager.updateFlight(updatedFlight)

        assertEquals(updatedFlight, result)
        verify(flightDao).findById(1L)
        verify(flightDao).update(updatedFlight)
        verify(flightsMaterializedViewManager).handleFlightReactivation(any())
    }

    @Test
    fun `updateFlight should not trigger materialized view update for no material change`() {
        val oldFlight = createValidFlight()
        val updatedFlight = oldFlight.copy(aircraftType = "Airbus A320") // Only metadata change

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        val result = flightUpdateManager.updateFlight(updatedFlight)

        assertEquals(updatedFlight, result)
        verify(flightDao).findById(1L)
        verify(flightDao).update(updatedFlight)
        verifyNoInteractions(flightsMaterializedViewManager)
    }

    @Test
    fun `updateFlight should handle materialized view update failure gracefully`() {
        val oldFlight = createValidFlight()
        val updatedFlight = oldFlight.copy(availableSeats = 80)

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)
        whenever(flightsMaterializedViewManager.handleSeatsChange(any())).thenThrow(RuntimeException("MV update failed"))

        // Should not throw exception - flight update should succeed even if MV update fails
        val result = flightUpdateManager.updateFlight(updatedFlight)

        assertEquals(updatedFlight, result)
        verify(flightDao).findById(1L)
        verify(flightDao).update(updatedFlight)
        verify(flightsMaterializedViewManager).handleSeatsChange(any())
    }

    @Test
    fun `updateFlight should propagate flight dao exceptions`() {
        val flightData = createValidFlight()

        whenever(flightDao.findById(1L)).thenThrow(RuntimeException("Database error"))

        val exception = assertThrows<RuntimeException> {
            flightUpdateManager.updateFlight(flightData)
        }
        assertEquals("Database error", exception.message)
    }

    @Test
    fun `updateFlight should handle multiple changes with priority`() {
        val oldFlight = createValidFlight().copy(active = true, availableSeats = 100)
        val updatedFlight = oldFlight.copy(
            active = false,  // Cancellation
            availableSeats = 80  // Seats change
        )

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        val result = flightUpdateManager.updateFlight(updatedFlight)

        assertEquals(updatedFlight, result)
        // Should handle cancellation (higher priority) not seats change
        verify(flightsMaterializedViewManager).handleFlightCancellation(any())
        verify(flightsMaterializedViewManager, never()).handleSeatsChange(any())
    }

    @Test
    fun `updateFlight should analyze schedule and seats changes together`() {
        val oldFlight = createValidFlight()
        val newDepartureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(2))
        val updatedFlight = oldFlight.copy(
            departureTime = newDepartureTime,
            availableSeats = 80
        )

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        val result = flightUpdateManager.updateFlight(updatedFlight)

        assertEquals(updatedFlight, result)
        // Schedule change has higher priority than seats change
        verify(flightsMaterializedViewManager).handleScheduleChange(any())
        verify(flightsMaterializedViewManager, never()).handleSeatsChange(any())
    }

    @Test
    fun `updateFlight should handle null old flight correctly`() {
        val newFlight = createValidFlight()

        whenever(flightDao.findById(1L)).thenReturn(null)
        whenever(flightDao.update(newFlight)).thenReturn(newFlight)

        val result = flightUpdateManager.updateFlight(newFlight)

        assertEquals(newFlight, result)
        verify(flightDao).findById(1L)
        verify(flightDao).update(newFlight)
        verify(flightsMaterializedViewManager).handleFlightCreation(any())
    }

    @Test
    fun `updateFlight should verify event creation with correct data`() {
        val oldFlight = createValidFlight().copy(availableSeats = 100)
        val updatedFlight = oldFlight.copy(availableSeats = 80)

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        flightUpdateManager.updateFlight(updatedFlight)

        argumentCaptor<FlightSeatsChangedEvent>().apply {
            verify(flightsMaterializedViewManager).handleSeatsChange(capture())
            assertEquals(1L, firstValue.flightId)
            assertEquals(100, firstValue.oldAvailableSeats)
            assertEquals(80, firstValue.newAvailableSeats)
            assertNotNull(firstValue.timestamp)
        }
    }

    @Test
    fun `updateFlight should verify schedule change event with correct timestamps`() {
        val oldFlight = createValidFlight()
        val newDepartureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(2))
        val newArrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(2).plusHours(3))
        val updatedFlight = oldFlight.copy(
            departureTime = newDepartureTime,
            arrivalTime = newArrivalTime
        )

        whenever(flightDao.findById(1L)).thenReturn(oldFlight)
        whenever(flightDao.update(updatedFlight)).thenReturn(updatedFlight)

        flightUpdateManager.updateFlight(updatedFlight)

        argumentCaptor<FlightScheduleChangedEvent>().apply {
            verify(flightsMaterializedViewManager).handleScheduleChange(capture())
            assertEquals(1L, firstValue.flightId)
            assertEquals(oldFlight.departureTime, firstValue.oldDepartureTime)
            assertEquals(newDepartureTime, firstValue.newDepartureTime)
            assertEquals(oldFlight.arrivalTime, firstValue.oldArrivalTime)
            assertEquals(newArrivalTime, firstValue.newArrivalTime)
        }
    }

    private fun createValidFlight() = Flight(
        flightId = 1L,
        flightNumber = "AI100",
        srcAirportCode = "DEL",
        destAirportCode = "BOM",
        departureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
        arrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)),
        sourceAirport = "Delhi",
        destinationAirport = "Mumbai",
        price = BigDecimal("100"),
        currency = "USD",
        totalSeats = 150,
        availableSeats = 100,
        aircraftType = "Boeing 737",
        airlineId = 1L,
        active = true
    )
}