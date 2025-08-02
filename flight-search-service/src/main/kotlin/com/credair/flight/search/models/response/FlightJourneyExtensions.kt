package com.credair.flight.search.models.response

import com.credair.core.model.FlightJourney

fun FlightJourney.toSearchResult(): FlightSearchResult {
    // Get the first segment for departure info and airline
    val firstSegment = segments.first()
    val lastSegment = segments.last()
    
    // Extract stop airports from intermediate segments
    val stopAirports = if (segments.size > 1) {
        segments.dropLast(1).map { it.arrival.airportCode }
    } else {
        emptyList()
    }
    
    return FlightSearchResult(
        airlineName = firstSegment.airline.name,
        airlineLogoUrl = firstSegment.airline.logoUrl,
        departureTime = firstSegment.departure.time,
        arrivalTime = lastSegment.arrival.time,
        departureAirport = firstSegment.departure.airportCode,
        arrivalAirport = lastSegment.arrival.airportCode,
        totalDurationMinutes = totalDuration.toMinutes().toInt(),
        stopCount = totalStops,
        stopAirports = stopAirports,
        priceAmount = price.amount,
        priceCurrency = price.currency
    )
}