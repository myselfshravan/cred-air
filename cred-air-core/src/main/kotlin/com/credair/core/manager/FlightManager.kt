package com.credair.core.manager

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.Flight
import com.google.inject.Inject
import com.google.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class FlightManager @Inject constructor(
    private val flightDao: FlightDao,
    private val flightUpdateManager: FlightUpdateManager
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(FlightManager::class.java)
    }

    fun createOrUpdateFlight(flight: Flight): Flight {
        return if (flight.flightId > 0) {
            logger.info("Updating existing flight with ID: ${flight.flightId}")
            flightUpdateManager.updateFlight(flight)
        } else {
            logger.info("Creating new flight: ${flight.flightNumber}")
            validateFlightForCreation(flight)
            val createdFlight = flightDao.save(flight)
            logger.info("Successfully created flight with ID: ${createdFlight.flightId}")
            createdFlight
        }
    }

    private fun validateFlightForCreation(flight: Flight) {
        require(flight.flightNumber.isNotBlank()) { "Flight number cannot be blank" }
        require(flight.srcAirportCode.isNotBlank()) { "Source airport code cannot be blank" }
        require(flight.destAirportCode.isNotBlank()) { "Destination airport code cannot be blank" }
        require(flight.srcAirportCode != flight.destAirportCode) { "Source and destination airports cannot be the same" }
        require(flight.departureTime.before(flight.arrivalTime)) { "Departure time must be before arrival time" }
        require(flight.totalSeats > 0) { "Total seats must be greater than 0" }
        require(flight.availableSeats >= 0) { "Available seats cannot be negative" }
        require(flight.availableSeats <= flight.totalSeats) { "Available seats cannot exceed total seats" }
        require(flight.price.signum() >= 0) { "Price cannot be negative" }
        require(flight.airlineId > 0) { "Airline ID must be provided" }
        
        val existingFlight = flightDao.findByAirlineIdAndFlightNumber(flight.airlineId, flight.flightNumber)
        require(existingFlight == null) { "Flight with number ${flight.flightNumber} already exists for airline ${flight.airlineId}" }
    }
}