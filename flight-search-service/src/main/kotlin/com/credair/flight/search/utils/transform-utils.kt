package com.credair.flight.search.utils

import com.credair.core.model.FlightSegment
import com.credair.core.model.Layover
import java.time.Duration

fun calculateLayovers(segments: List<FlightSegment>): List<Layover> {
    val layovers = mutableListOf<Layover>()

    for (i in 0 until segments.size - 1) {
        val currentSegment = segments[i]
        val nextSegment = segments[i + 1]

        // Calculate layover duration between segments
        val layoverDuration = Duration.between(
            currentSegment.arrival.time,
            nextSegment.departure.time
        )

        layovers.add(Layover(
            airportCode = currentSegment.arrival.airportCode,
            airportName = currentSegment.arrival.city,
            duration = layoverDuration
        ))
    }

    return layovers
}

