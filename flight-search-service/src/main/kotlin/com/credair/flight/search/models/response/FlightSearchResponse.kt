package com.credair.flight.search.models.response

import com.credair.core.model.FlightSearchResult

data class FlightSearchResponse(
    val results: List<FlightSearchResult>,
    val nextStartIndex: Int = 0,
    val hasMore: Boolean = false,
    val pageSize: Int = 10
)