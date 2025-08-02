package com.credair.core.events

import java.time.Instant
import java.sql.Timestamp

sealed class FlightChangeEvent(
    open val flightId: Long,
    val timestamp: Instant = Instant.now()
)

data class FlightScheduleChangedEvent(
    override val flightId: Long,
    val oldDepartureTime: Timestamp,
    val newDepartureTime: Timestamp,
    val oldArrivalTime: Timestamp,
    val newArrivalTime: Timestamp
) : FlightChangeEvent(flightId)

data class FlightSeatsChangedEvent(
    override val flightId: Long,
    val oldAvailableSeats: Int,
    val newAvailableSeats: Int
) : FlightChangeEvent(flightId)

data class FlightCancelledEvent(
    override val flightId: Long
) : FlightChangeEvent(flightId)

data class FlightReactivatedEvent(
    override val flightId: Long
) : FlightChangeEvent(flightId)

data class FlightCreatedEvent(
    override val flightId: Long
) : FlightChangeEvent(flightId)

data class FlightChangeAnalysis(
    val changeType: FlightChangeType,
    val scheduleChanged: Boolean,
    val seatsChanged: Boolean,
    val statusChanged: Boolean,
    val oldFlight: com.credair.core.model.Flight?,
    val newFlight: com.credair.core.model.Flight
)

enum class FlightChangeType {
    SCHEDULE_CHANGE,    // departs_at or arrives_at changed
    SEATS_UPDATE,       // only available_seats changed  
    FLIGHT_CANCELLED,   // active: true → false
    FLIGHT_REACTIVATED, // active: false → true
    FLIGHT_CREATED,     // new flight creation
    NO_MATERIAL_CHANGE  // only metadata changed (airline_id, aircraft_type, etc.)
}