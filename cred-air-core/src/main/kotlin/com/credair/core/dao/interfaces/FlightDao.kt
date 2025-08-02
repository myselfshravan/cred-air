package com.credair.core.dao.interfaces

import com.credair.core.model.Flight
import com.credair.common.dao.BaseDao
import java.time.LocalDateTime

interface FlightDao : BaseDao<Flight, Long> {
    fun findByAirlineIdAndFlightNumber(airlineId: Long, flightNumber: String): Flight?
    fun updateAvailableSeats(flightId: Long, availableSeats: Int): Boolean
    fun searchFlightsOptimized(
        srcAirportCode: String,
        destAirportCode: String,
        departureDate: LocalDateTime? = null,
        noOfSeats: Int = 1,
        sortBy: String = "departure_time",
        sortOrder: String = "ASC",
        page: Int = 0,
        pageSize: Int = 10
    ): List<com.credair.core.model.FlightSearchResult>
}