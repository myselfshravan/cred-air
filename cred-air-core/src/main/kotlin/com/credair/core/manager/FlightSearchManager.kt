package com.credair.core.manager

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.Flight
import com.google.inject.Inject
import java.math.BigDecimal
import java.time.LocalDateTime

class FlightSearchManager @Inject constructor(private val flightDao: FlightDao) {

    data class SearchCriteria(
        val sourceAirport: String,
        val destinationAirport: String,
        val departureDate: LocalDateTime? = null,
        val maxPrice: BigDecimal? = null,
        val minPrice: BigDecimal? = null,
        val airlineId: Long? = null,
        val minSeats: Int = 1
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
        
        val flights = when {
            criteria.departureDate != null -> {
                flightDao.findByRouteAndDate(
                    criteria.sourceAirport,
                    criteria.destinationAirport,
                    criteria.departureDate
                )
            }
            else -> {
                flightDao.findByRoute(
                    criteria.sourceAirport,
                    criteria.destinationAirport
                )
            }
        }
        
        return flights
            .filter { flight -> applyFilters(flight, criteria) }
            .sortedWith(getSortComparator(sortCriteria))
    }

    fun getAvailableFlights(): List<Flight> {
        return flightDao.findAvailableFlights()
            .filter { it.isAvailable }
            .sortedBy { it.departureTime }
    }

    fun getFlightsByAirline(airlineId: Long): List<Flight> {
        return flightDao.findByAirlineId(airlineId)
            .filter { it.active }
            .sortedBy { it.departureTime }
    }

    fun getFlightByNumber(flightNumber: String): List<Flight> {
        return flightDao.findByFlightNumber(flightNumber)
            .filter { it.active }
            .sortedBy { it.departureTime }
    }

    fun findCheapestFlights(
        sourceAirport: String,
        destinationAirport: String,
        limit: Int = 5
    ): List<Flight> {
        return flightDao.findByRoute(sourceAirport, destinationAirport)
            .filter { it.isAvailable }
            .sortedBy { it.price }
            .take(limit)
    }

    fun findFastestFlights(
        sourceAirport: String,
        destinationAirport: String,
        limit: Int = 5
    ): List<Flight> {
        return flightDao.findByRoute(sourceAirport, destinationAirport)
            .filter { it.isAvailable }
            .sortedBy { it.duration }
            .take(limit)
    }

    fun getFlightRecommendations(
        sourceAirport: String,
        destinationAirport: String
    ): Map<String, List<Flight>> {
        val allFlights = flightDao.findByRoute(sourceAirport, destinationAirport)
            .filter { it.isAvailable }

        return mapOf(
            "cheapest" to allFlights.sortedBy { it.price }.take(3),
            "fastest" to allFlights.sortedBy { it.duration }.take(3),
            "best_value" to allFlights.sortedBy { calculateValueScore(it) }.take(3)
        )
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

    private fun applyFilters(flight: Flight, criteria: SearchCriteria): Boolean {
        if (!flight.isAvailable) return false
        if (flight.availableSeats < criteria.minSeats) return false
        
        criteria.minPrice?.let { minPrice ->
            if (flight.price < minPrice) return false
        }
        
        criteria.maxPrice?.let { maxPrice ->
            if (flight.price > maxPrice) return false
        }
        
        criteria.airlineId?.let { airlineId ->
            if (flight.airlineId != airlineId) return false
        }
        
        return true
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

    private fun calculateValueScore(flight: Flight): Double {
        val priceWeight = 0.6
        val durationWeight = 0.4
        
        val normalizedPrice = flight.price.toDouble()
        val normalizedDuration = flight.duration.toDouble()
        
        return (normalizedPrice * priceWeight) + (normalizedDuration * durationWeight)
    }
}