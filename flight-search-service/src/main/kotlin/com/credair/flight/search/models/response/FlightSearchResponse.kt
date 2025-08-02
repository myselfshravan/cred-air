package com.credair.flight.search.models.response

import com.credair.core.model.FlightSearchResult

data class FlightSearchResponse(
    val results: List<FlightSearchResult>
)