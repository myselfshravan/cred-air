package com.credair.flight.search.managers

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.FlightJourney
import com.credair.core.model.FlightSegment
import com.credair.flight.search.models.request.SearchCriteria
import com.credair.flight.search.models.request.SortCriteria
import com.credair.flight.search.utils.calculateLayovers
import com.credair.flight.search.utils.validateSearchCriteria
import com.google.inject.Inject
import com.google.inject.Singleton
import java.time.Duration

@Singleton
class FlightSearchManager @Inject constructor(
    private val flightDao: FlightDao
) {

    fun searchFlightJourneys(
        criteria: SearchCriteria,
        sortCriteria: SortCriteria = SortCriteria(),
        page: Int = 0,
        pageSize: Int = 10
    ): List<FlightJourney> {
        validateSearchCriteria(criteria)

        // Use optimized search that returns FlightSegments directly
        val flightSegments = flightDao.searchFlightsOptimized(
            criteria.sourceAirport,
            criteria.destinationAirport,
            criteria.departureDate,
            criteria.noOfSeats,
            sortCriteria.sortBy.name.lowercase(),
            sortCriteria.sortOrder.name,
            page,
            pageSize
        )

        // Convert FlightSegments to FlightJourneys
        return flightSegments.map { segment -> convertFlightSegmentToJourney(segment) }
    }

    private fun convertFlightSegmentToJourney(segment: FlightSegment): FlightJourney {
        val segments = listOf(segment)
        val layovers = calculateLayovers(segments)
        val totalDuration = segment.segmentDuration

        // Calculate total time in air (excluding layovers)
        val totalLayoverTime = layovers.fold(Duration.ZERO) { acc, layover -> acc.plus(layover.duration) }
        val totalTimeInAir = totalDuration.minus(totalLayoverTime)

        return FlightJourney(
            totalDuration = totalDuration,
            totalTimeInAir = totalTimeInAir,
            price = segment.price,
            seatsLeft = segment.availableSeats,
            segments = segments,
            layovers = layovers
        )
    }
}