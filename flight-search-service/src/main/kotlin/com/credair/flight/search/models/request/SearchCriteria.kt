package com.credair.flight.search.models.request

import java.time.LocalDateTime

data class SearchCriteria(
    val sourceAirport: String,
    val destinationAirport: String,
    val departureDate: LocalDateTime? = null,
    val noOfSeats: Int = 1,
)
