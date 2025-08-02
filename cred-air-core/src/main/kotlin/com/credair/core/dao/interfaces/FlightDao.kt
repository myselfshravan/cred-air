package com.credair.core.dao.interfaces

import com.credair.core.model.Flight
import com.credair.core.model.SearchResult
import com.credair.common.dao.BaseDao
import java.time.LocalDateTime

interface FlightDao : BaseDao<Flight, Long> {
    fun findByAirlineIdAndFlightNumber(airlineId: Long, flightNumber: String): Flight?
    fun updateAvailableSeats(flightId: Long, availableSeats: Int): Boolean
    fun searchFlightsOptimized(
        srcAirportCode: String,
        destAirportCode: String,
        limit: Int = 100
    ): List<SearchResult>
}