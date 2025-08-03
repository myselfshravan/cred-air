package com.credair.flight.search.managers

import com.credair.core.dao.interfaces.FlightDao
import com.credair.flight.search.models.request.SearchCriteria
import com.credair.flight.search.models.request.SortCriteria
import com.credair.core.model.FlightSearchResult
import com.credair.core.model.FlightJourney
import com.credair.flight.search.utils.validateSearchCriteria
import com.credair.flight.search.utils.RouteValidationUtils
import com.github.benmanes.caffeine.cache.Cache
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class FlightSearchManager @Inject constructor(
    private val flightDao: FlightDao,
    private val searchCache: Cache<String, List<FlightSearchResult>>
) {

    fun searchFlights(
        criteria: SearchCriteria,
        sortCriteria: SortCriteria = SortCriteria(),
        page: Int = 0,
        pageSize: Int = 10
    ): List<FlightSearchResult> {
        validateSearchCriteria(criteria)

        val cacheKey = generateCacheKey(criteria, sortCriteria, page, pageSize)
        
        val cachedResults = searchCache.getIfPresent(cacheKey)
        if (cachedResults != null) {
            return cachedResults
        }

        val searchResults = flightDao.searchFlightsOptimized(
            criteria.sourceAirport,
            criteria.destinationAirport,
            criteria.departureDate,
            criteria.noOfSeats,
            sortCriteria.sortBy.name.lowercase(),
            sortCriteria.sortOrder.name,
            page,
            pageSize
        )

        val filteredResults = searchResults.filter { RouteValidationUtils.isEfficientRoute(it) }
        searchCache.put(cacheKey, filteredResults)
        
        return filteredResults
    }

    private fun generateCacheKey(
        criteria: SearchCriteria,
        sortCriteria: SortCriteria,
        page: Int,
        pageSize: Int
    ): String {
        return "${criteria.sourceAirport}-${criteria.destinationAirport}-${criteria.departureDate}-${criteria.noOfSeats}-${sortCriteria.sortBy.name}-${sortCriteria.sortOrder.name}-$page-$pageSize"
    }

    fun getFlightJourney(flightIds: List<Long>): FlightJourney? {
        return flightDao.getFlightJourney(flightIds)
    }
}