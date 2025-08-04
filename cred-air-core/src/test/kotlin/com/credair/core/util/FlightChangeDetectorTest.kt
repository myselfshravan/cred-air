package com.credair.core.util

import com.credair.core.events.*
import com.credair.core.model.Flight
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FlightChangeDetectorTest {

    // Flight Creation Tests
    @Test
    fun `analyzeChanges should detect new flight creation when oldFlight is null`() {
        val newFlight = createValidFlight()

        val analysis = FlightChangeDetector.analyzeChanges(null, newFlight)

        assertEquals(FlightChangeType.FLIGHT_CREATED, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
        assertFalse(analysis.seatsChanged)
        assertFalse(analysis.statusChanged)
        assertNull(analysis.oldFlight)
        assertEquals(newFlight, analysis.newFlight)
    }

    @Test
    fun `createEvent should return FlightCreatedEvent for new flight`() {
        val newFlight = createValidFlight()
        val analysis = FlightChangeDetector.analyzeChanges(null, newFlight)

        val event = FlightChangeDetector.createEvent(analysis)

        assertNotNull(event)
        assertTrue(event is FlightCreatedEvent)
        assertEquals(newFlight.flightId, (event as FlightCreatedEvent).flightId)
    }

    // Schedule Change Tests
    @Test
    fun `analyzeChanges should detect departure time change`() {
        val oldFlight = createValidFlight()
        val newFlight = oldFlight.copy(
            departureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1))
        )

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.SCHEDULE_CHANGE, analysis.changeType)
        assertTrue(analysis.scheduleChanged)
        assertFalse(analysis.seatsChanged)
        assertFalse(analysis.statusChanged)
    }

    @Test
    fun `analyzeChanges should detect arrival time change`() {
        val oldFlight = createValidFlight()
        val newFlight = oldFlight.copy(
            arrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(3))
        )

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.SCHEDULE_CHANGE, analysis.changeType)
        assertTrue(analysis.scheduleChanged)
        assertFalse(analysis.seatsChanged)
        assertFalse(analysis.statusChanged)
    }

    @Test
    fun `analyzeChanges should detect both departure and arrival time changes`() {
        val oldFlight = createValidFlight()
        val newFlight = oldFlight.copy(
            departureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1)),
            arrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(4))
        )

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.SCHEDULE_CHANGE, analysis.changeType)
        assertTrue(analysis.scheduleChanged)
        assertFalse(analysis.seatsChanged)
        assertFalse(analysis.statusChanged)
    }

    @Test
    fun `createEvent should return FlightScheduleChangedEvent for schedule change`() {
        val oldFlight = createValidFlight()
        val newDepartureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1))
        val newArrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(3))
        val newFlight = oldFlight.copy(
            departureTime = newDepartureTime,
            arrivalTime = newArrivalTime
        )
        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        val event = FlightChangeDetector.createEvent(analysis)

        assertNotNull(event)
        assertTrue(event is FlightScheduleChangedEvent)
        val scheduleEvent = event as FlightScheduleChangedEvent
        assertEquals(newFlight.flightId, scheduleEvent.flightId)
        assertEquals(oldFlight.departureTime, scheduleEvent.oldDepartureTime)
        assertEquals(newDepartureTime, scheduleEvent.newDepartureTime)
        assertEquals(oldFlight.arrivalTime, scheduleEvent.oldArrivalTime)
        assertEquals(newArrivalTime, scheduleEvent.newArrivalTime)
    }

    // Seat Change Tests
    @Test
    fun `analyzeChanges should detect available seats increase`() {
        val oldFlight = createValidFlight().copy(availableSeats = 100)
        val newFlight = oldFlight.copy(availableSeats = 120)

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.SEATS_UPDATE, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
        assertTrue(analysis.seatsChanged)
        assertFalse(analysis.statusChanged)
    }

    @Test
    fun `analyzeChanges should detect available seats decrease`() {
        val oldFlight = createValidFlight().copy(availableSeats = 100)
        val newFlight = oldFlight.copy(availableSeats = 80)

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.SEATS_UPDATE, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
        assertTrue(analysis.seatsChanged)
        assertFalse(analysis.statusChanged)
    }

    @Test
    fun `analyzeChanges should detect seats going to zero`() {
        val oldFlight = createValidFlight().copy(availableSeats = 5)
        val newFlight = oldFlight.copy(availableSeats = 0)

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.SEATS_UPDATE, analysis.changeType)
        assertTrue(analysis.seatsChanged)
    }

    @Test
    fun `createEvent should return FlightSeatsChangedEvent for seat change`() {
        val oldFlight = createValidFlight().copy(availableSeats = 100)
        val newFlight = oldFlight.copy(availableSeats = 85)
        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        val event = FlightChangeDetector.createEvent(analysis)

        assertNotNull(event)
        assertTrue(event is FlightSeatsChangedEvent)
        val seatsEvent = event as FlightSeatsChangedEvent
        assertEquals(newFlight.flightId, seatsEvent.flightId)
        assertEquals(100, seatsEvent.oldAvailableSeats)
        assertEquals(85, seatsEvent.newAvailableSeats)
    }

    // Status Change Tests
    @Test
    fun `analyzeChanges should detect flight cancellation`() {
        val oldFlight = createValidFlight().copy(active = true)
        val newFlight = oldFlight.copy(active = false)

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.FLIGHT_CANCELLED, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
        assertFalse(analysis.seatsChanged)
        assertTrue(analysis.statusChanged)
    }

    @Test
    fun `analyzeChanges should detect flight reactivation`() {
        val oldFlight = createValidFlight().copy(active = false)
        val newFlight = oldFlight.copy(active = true)

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.FLIGHT_REACTIVATED, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
        assertFalse(analysis.seatsChanged)
        assertTrue(analysis.statusChanged)
    }

    @Test
    fun `createEvent should return FlightCancelledEvent for cancellation`() {
        val oldFlight = createValidFlight().copy(active = true)
        val newFlight = oldFlight.copy(active = false)
        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        val event = FlightChangeDetector.createEvent(analysis)

        assertNotNull(event)
        assertTrue(event is FlightCancelledEvent)
        assertEquals(newFlight.flightId, (event as FlightCancelledEvent).flightId)
    }

    @Test
    fun `createEvent should return FlightReactivatedEvent for reactivation`() {
        val oldFlight = createValidFlight().copy(active = false)
        val newFlight = oldFlight.copy(active = true)
        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        val event = FlightChangeDetector.createEvent(analysis)

        assertNotNull(event)
        assertTrue(event is FlightReactivatedEvent)
        assertEquals(newFlight.flightId, (event as FlightReactivatedEvent).flightId)
    }

    // No Change Tests
    @Test
    fun `analyzeChanges should detect no material change when flights are identical`() {
        val oldFlight = createValidFlight()
        val newFlight = oldFlight.copy() // Identical copy

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.NO_MATERIAL_CHANGE, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
        assertFalse(analysis.seatsChanged)
        assertFalse(analysis.statusChanged)
    }

    @Test
    fun `analyzeChanges should detect no material change when only non-tracked fields change`() {
        val oldFlight = createValidFlight()
        val newFlight = oldFlight.copy(
            flightNumber = "DIFFERENT123", // Non-tracked field
            aircraftType = "Different Aircraft" // Non-tracked field
        )

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.NO_MATERIAL_CHANGE, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
        assertFalse(analysis.seatsChanged)
        assertFalse(analysis.statusChanged)
    }

    @Test
    fun `createEvent should return null for no material change`() {
        val oldFlight = createValidFlight()
        val newFlight = oldFlight.copy()
        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        val event = FlightChangeDetector.createEvent(analysis)

        assertNull(event)
    }

    // Priority Tests
    @Test
    fun `analyzeChanges should prioritize status change over schedule change`() {
        val oldFlight = createValidFlight().copy(active = true)
        val newFlight = oldFlight.copy(
            active = false, // Status change
            departureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1)) // Schedule change
        )

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.FLIGHT_CANCELLED, analysis.changeType)
        assertTrue(analysis.scheduleChanged)
        assertTrue(analysis.statusChanged)
    }

    @Test
    fun `analyzeChanges should prioritize status change over seat change`() {
        val oldFlight = createValidFlight().copy(active = true, availableSeats = 100)
        val newFlight = oldFlight.copy(
            active = false, // Status change
            availableSeats = 80 // Seat change
        )

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.FLIGHT_CANCELLED, analysis.changeType)
        assertTrue(analysis.seatsChanged)
        assertTrue(analysis.statusChanged)
    }

    @Test
    fun `analyzeChanges should prioritize schedule change over seat change`() {
        val oldFlight = createValidFlight().copy(availableSeats = 100)
        val newFlight = oldFlight.copy(
            departureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1)), // Schedule change
            availableSeats = 80 // Seat change
        )

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.SCHEDULE_CHANGE, analysis.changeType)
        assertTrue(analysis.scheduleChanged)
        assertTrue(analysis.seatsChanged)
    }

    // Multiple Changes Tests
    @Test
    fun `analyzeChanges should detect all change types when multiple changes occur`() {
        val oldFlight = createValidFlight().copy(
            active = true,
            availableSeats = 100
        )
        val newFlight = oldFlight.copy(
            departureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(1)),
            arrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(3)),
            availableSeats = 80,
            active = false
        )

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        // Should prioritize cancellation but detect all changes
        assertEquals(FlightChangeType.FLIGHT_CANCELLED, analysis.changeType)
        assertTrue(analysis.scheduleChanged)
        assertTrue(analysis.seatsChanged)
        assertTrue(analysis.statusChanged)
    }

    // Edge Cases
    @Test
    fun `analyzeChanges should handle same timestamp objects correctly`() {
        val timestamp = Timestamp.valueOf(LocalDateTime.now())
        val oldFlight = createValidFlight().copy(departureTime = timestamp)
        val newFlight = oldFlight.copy(departureTime = timestamp) // Same object

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.NO_MATERIAL_CHANGE, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
    }

    @Test
    fun `analyzeChanges should handle equivalent but different timestamp objects`() {
        val time = LocalDateTime.now()
        val oldFlight = createValidFlight().copy(departureTime = Timestamp.valueOf(time))
        val newFlight = oldFlight.copy(departureTime = Timestamp.valueOf(time)) // Different object, same value

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.NO_MATERIAL_CHANGE, analysis.changeType)
        assertFalse(analysis.scheduleChanged)
    }

    @Test
    fun `analyzeChanges should handle flights with different IDs`() {
        val oldFlight = createValidFlight().copy(flightId = 1L)
        val newFlight = oldFlight.copy(flightId = 2L, availableSeats = 80)

        val analysis = FlightChangeDetector.analyzeChanges(oldFlight, newFlight)

        assertEquals(FlightChangeType.SEATS_UPDATE, analysis.changeType)
        assertTrue(analysis.seatsChanged)
    }

    // Helper Methods
    private fun createValidFlight() = Flight(
        flightId = 1L,
        externalFlightId = "AI100-DEL-BOM-20240804",
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