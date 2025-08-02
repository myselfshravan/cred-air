package com.credair.core.model

import java.math.BigDecimal
import java.time.LocalTime

data class FlightSearchResult(
    val airlineName: String,
    val airlineLogoUrl: String,
    val departureTime: LocalTime,
    val arrivalTime: LocalTime,
    val departureAirport: String,
    val arrivalAirport: String,
    val totalDurationMinutes: Int,
    val stopCount: Int,
    val stopAirports: List<String>,
    val priceAmount: BigDecimal,
    val priceCurrency: String
)