package com.credair.flight.search.managers

import com.credair.core.dao.interfaces.FlightDao
import com.credair.flight.search.models.request.SearchCriteria
import com.credair.flight.search.models.request.SortCriteria
import com.credair.core.model.FlightSearchResult
import com.credair.flight.search.utils.validateSearchCriteria
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class FlightSearchManager @Inject constructor(
    private val flightDao: FlightDao
) {

    fun searchFlights(
        criteria: SearchCriteria,
        sortCriteria: SortCriteria = SortCriteria(),
        page: Int = 0,
        pageSize: Int = 10
    ): List<FlightSearchResult> {
        validateSearchCriteria(criteria)

        return flightDao.searchFlightsOptimized(
            criteria.sourceAirport,
            criteria.destinationAirport,
            criteria.departureDate,
            criteria.noOfSeats,
            sortCriteria.sortBy.name.lowercase(),
            sortCriteria.sortOrder.name,
            page,
            pageSize
        )
    }
}