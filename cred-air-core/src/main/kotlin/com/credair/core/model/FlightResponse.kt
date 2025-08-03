package com.credair.core.model

import java.math.BigDecimal
import java.time.Duration

/**
 * Represents a complete flight journey from origin to destination, which can be direct or have stops.
 * This is the top-level data class that would power a single row in the search results.
 */
data class FlightJourney(
    val totalDuration: Duration,
    val totalTimeInAir: Duration,
    val price: FlightPrice,
    val segments: List<FlightSegment>,
    val layovers: List<Layover>
) {
    val totalStops: Int
        get() = segments.size - 1

    val flightType: FlightType
        get() = when (totalStops) {
            0 -> FlightType.DIRECT
            1 -> FlightType.ONE_STOP
            else -> FlightType.MULTIPLE_STOPS
        }
}

/**
 * Represents a single leg of a flight journey (e.g., NYC -> DEN).
 */
data class FlightSegment(
    val airline: FlightAirline,
    val departure: FlightStop,
    val arrival: FlightStop,
    val segmentDuration: Duration,
    val price: FlightPrice,
    val id: Long
)

/**
 * Represents a layover between two flight segments.
 */
data class Layover(
    val airportCode: String,
    val airportName: String,
    val duration: Duration
) {
    val formattedLayover: String
        get() = "${duration.toMinutes()}m layover in $airportName"
}

data class FlightAirline(
    val name: String,
    val logoUrl: String // Assuming a URL or resource ID for the logo
)

data class FlightDetails(
    val flightNumber: String,
    val aircraftType: String
)

data class FlightStop(
    val departsAt: Long,
    val arrivesAt: Long,
    val airportCode: String,
    val city: String
)

data class FlightPrice(
    val amount: BigDecimal,
    val currency: String,
    val perPerson: Boolean = true
) {
    val formattedPrice: String
        get() = "$amount ${if (perPerson) "per person" else ""} in $currency"
}

enum class FlightType {
    DIRECT,
    ONE_STOP,
    MULTIPLE_STOPS
}