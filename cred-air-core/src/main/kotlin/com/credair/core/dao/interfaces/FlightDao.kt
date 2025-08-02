package com.credair.core.dao.interfaces

import com.credair.core.model.Flight
import com.credair.common.dao.BaseDao
import java.time.LocalDateTime

interface FlightDao : BaseDao<Flight, Long> {
    fun findByAirlineId(airlineId: Long): List<Flight>
    fun findByFlightNumber(flightNumber: String): List<Flight>
    fun findByRoute(sourceAirport: String, destinationAirport: String): List<Flight>
    fun findByRouteAndDate(
        sourceAirport: String, 
        destinationAirport: String, 
        departureDate: LocalDateTime
    ): List<Flight>
    fun findAvailableFlights(): List<Flight>
    fun findByAirlineIdAndFlightNumber(airlineId: Long, flightNumber: String): Flight?
    fun updateAvailableSeats(flightId: Long, availableSeats: Int): Boolean
}