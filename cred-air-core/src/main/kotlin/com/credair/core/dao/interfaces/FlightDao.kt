package com.credair.core.dao.interfaces

import com.credair.core.model.Flight
import com.credair.core.model.SearchResult
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
    fun findBySourceAirport(sourceAirport: String): List<Flight>
    fun updateAvailableSeats(flightId: Long, availableSeats: Int): Boolean
    
    // High-performance search using materialized views
    fun searchFlightsOptimized(
        srcAirportCode: String,
        destAirportCode: String,
        limit: Int = 100
    ): List<SearchResult>
}