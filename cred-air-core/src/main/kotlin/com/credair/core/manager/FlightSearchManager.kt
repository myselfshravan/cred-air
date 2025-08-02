package com.credair.core.manager

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.Flight
import com.google.inject.Inject
import com.google.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDateTime

@Singleton
class FlightSearchManager @Inject constructor(private val flightDao: FlightDao) {

    data class SearchCriteria(
        val sourceAirport: String,
        val destinationAirport: String,
        val departureDate: LocalDateTime? = null,
        val maxPrice: BigDecimal? = null,
        val minPrice: BigDecimal? = null,
        val airlineId: String? = null,
        val minSeats: Int = 1,
        val directOnly: Boolean = false,
        val maxStops: Int = 2,
        val minLayoverMinutes: Long = 60,
        val maxLayoverMinutes: Long = 720,
        val maxTotalDurationHours: Long = 24
    )

    data class SortCriteria(
        val sortBy: SortBy = SortBy.DEPARTURE_TIME,
        val sortOrder: SortOrder = SortOrder.ASC
    )

    enum class SortBy {
        DEPARTURE_TIME, ARRIVAL_TIME, PRICE, DURATION, AVAILABLE_SEATS
    }

    enum class SortOrder {
        ASC, DESC
    }

    fun searchFlights(
        criteria: SearchCriteria,
        sortCriteria: SortCriteria = SortCriteria()
    ): List<Flight> {
        validateSearchCriteria(criteria)
        
        // Use optimized materialized view search for better performance
        val searchResults = flightDao.searchFlightsOptimized(
            criteria.sourceAirport,
            criteria.destinationAirport,
            100
        )
        
        // Convert SearchResult to Flight for backward compatibility
        return searchResults
            .filter { result -> applySearchResultFilters(result, criteria) }
            .map { result -> convertSearchResultToFlight(result) }
            .sortedWith(getSortComparator(sortCriteria))
    }

    private fun validateSearchCriteria(criteria: SearchCriteria) {
        require(criteria.sourceAirport.isNotBlank()) { "Source airport cannot be blank" }
        require(criteria.destinationAirport.isNotBlank()) { "Destination airport cannot be blank" }
        require(criteria.sourceAirport != criteria.destinationAirport) { 
            "Source and destination airports cannot be the same" 
        }
        require(criteria.minSeats > 0) { "Minimum seats must be greater than 0" }
        
        if (criteria.minPrice != null && criteria.maxPrice != null) {
            require(criteria.minPrice <= criteria.maxPrice) { 
                "Minimum price cannot be greater than maximum price" 
            }
        }
    }

    private fun getSortComparator(sortCriteria: SortCriteria): Comparator<Flight> {
        val comparator = when (sortCriteria.sortBy) {
            SortBy.DEPARTURE_TIME -> compareBy<Flight> { it.departureTime }
            SortBy.ARRIVAL_TIME -> compareBy { it.arrivalTime }
            SortBy.PRICE -> compareBy { it.price }
            SortBy.DURATION -> compareBy { it.duration }
            SortBy.AVAILABLE_SEATS -> compareBy { it.availableSeats }
        }
        
        return when (sortCriteria.sortOrder) {
            SortOrder.ASC -> comparator
            SortOrder.DESC -> comparator.reversed()
        }
    }

    private fun applySearchResultFilters(result: com.credair.core.model.SearchResult, criteria: SearchCriteria): Boolean {
        criteria.minPrice?.let { minPrice ->
            if (result.totalCost < minPrice) return false
        }
        
        criteria.maxPrice?.let { maxPrice ->
            if (result.totalCost > maxPrice) return false
        }
        
        if (criteria.directOnly && result.stops > 0) return false
        if (result.stops > criteria.maxStops) return false
        
        return true
    }
    
    private fun convertSearchResultToFlight(result: com.credair.core.model.SearchResult): Flight {
        // For multi-stop flights, create a representative flight object
        return Flight(
            flightId = result.path[0], // Use first flight ID
            flightNumber = "MULTI-${result.path.joinToString("-")}",
            srcAirportCode = result.srcAirportCode,
            destAirportCode = result.destAirportCode,
            departureTime = java.sql.Timestamp.valueOf(result.departureTime),
            arrivalTime = java.sql.Timestamp.valueOf(result.arrivalTime),
            price = result.totalCost,
            totalSeats = 100, // Default value
            availableSeats = 50 // Default value
        )
    }
}