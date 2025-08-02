package com.credair.flight.search.utils

import com.credair.flight.search.models.request.SearchCriteria

fun validateSearchCriteria(criteria: SearchCriteria) {
    require(criteria.sourceAirport.isNotBlank()) { "Source airport cannot be blank" }
    require(criteria.destinationAirport.isNotBlank()) { "Destination airport cannot be blank" }
    require(criteria.sourceAirport != criteria.destinationAirport) {
        "Source and destination airports cannot be the same"
    }
    require(criteria.noOfSeats > 0) { "Minimum seats must be greater than 0" }
}