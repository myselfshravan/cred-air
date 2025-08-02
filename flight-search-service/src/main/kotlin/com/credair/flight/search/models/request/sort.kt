package com.credair.flight.search.models.request

data class SortCriteria(
    val sortBy: SortBy = SortBy.DURATION,
    val sortOrder: SortOrder = SortOrder.ASC
)

enum class SortBy {
    DEPARTURE_TIME, ARRIVAL_TIME, PRICE, DURATION
}

enum class SortOrder {
    ASC, DESC
}