package com.credair.core.util

import com.credair.core.events.*
import com.credair.core.model.Flight

object FlightChangeDetector {

    fun analyzeChanges(oldFlight: Flight?, newFlight: Flight): FlightChangeAnalysis {
        if (oldFlight == null) {
            // New flight creation - triggers materialized view updates for new journey possibilities
            return FlightChangeAnalysis(
                changeType = FlightChangeType.FLIGHT_CREATED,
                scheduleChanged = false,
                seatsChanged = false,
                statusChanged = false,
                oldFlight = null,
                newFlight = newFlight
            )
        }

        val scheduleChanged = hasScheduleChanged(oldFlight, newFlight)
        val seatsChanged = hasSeatsChanged(oldFlight, newFlight)
        val statusChanged = hasStatusChanged(oldFlight, newFlight)

        val changeType = determineChangeType(scheduleChanged, seatsChanged, statusChanged, oldFlight, newFlight)

        return FlightChangeAnalysis(
            changeType = changeType,
            scheduleChanged = scheduleChanged,
            seatsChanged = seatsChanged,
            statusChanged = statusChanged,
            oldFlight = oldFlight,
            newFlight = newFlight
        )
    }

    fun createEvent(analysis: FlightChangeAnalysis): FlightChangeEvent? {
        return when (analysis.changeType) {
            FlightChangeType.SCHEDULE_CHANGE -> FlightScheduleChangedEvent(
                flightId = analysis.newFlight.flightId!!,
                oldDepartureTime = analysis.oldFlight!!.departureTime,
                newDepartureTime = analysis.newFlight.departureTime,
                oldArrivalTime = analysis.oldFlight.arrivalTime,
                newArrivalTime = analysis.newFlight.arrivalTime
            )
            FlightChangeType.SEATS_UPDATE -> FlightSeatsChangedEvent(
                flightId = analysis.newFlight.flightId!!,
                oldAvailableSeats = analysis.oldFlight!!.availableSeats,
                newAvailableSeats = analysis.newFlight.availableSeats
            )
            FlightChangeType.FLIGHT_CANCELLED -> FlightCancelledEvent(
                flightId = analysis.newFlight.flightId!!
            )
            FlightChangeType.FLIGHT_REACTIVATED -> FlightReactivatedEvent(
                flightId = analysis.newFlight.flightId!!
            )
            FlightChangeType.FLIGHT_CREATED -> FlightCreatedEvent(
                flightId = analysis.newFlight.flightId!!
            )
            FlightChangeType.NO_MATERIAL_CHANGE -> null
        }
    }

    private fun hasScheduleChanged(oldFlight: Flight, newFlight: Flight): Boolean {
        return oldFlight.departureTime != newFlight.departureTime ||
               oldFlight.arrivalTime != newFlight.arrivalTime
    }

    private fun hasSeatsChanged(oldFlight: Flight, newFlight: Flight): Boolean {
        return oldFlight.availableSeats != newFlight.availableSeats
    }

    private fun hasStatusChanged(oldFlight: Flight, newFlight: Flight): Boolean {
        return oldFlight.active != newFlight.active
    }

    private fun determineChangeType(
        scheduleChanged: Boolean,
        seatsChanged: Boolean,
        statusChanged: Boolean,
        oldFlight: Flight,
        newFlight: Flight
    ): FlightChangeType {
        // Priority order: cancellation > reactivation > schedule > seats > no change
        if (statusChanged) {
            return if (oldFlight.active && !newFlight.active) {
                FlightChangeType.FLIGHT_CANCELLED
            } else {
                FlightChangeType.FLIGHT_REACTIVATED
            }
        }

        if (scheduleChanged) {
            return FlightChangeType.SCHEDULE_CHANGE
        }

        if (seatsChanged) {
            return FlightChangeType.SEATS_UPDATE
        }

        return FlightChangeType.NO_MATERIAL_CHANGE
    }
}