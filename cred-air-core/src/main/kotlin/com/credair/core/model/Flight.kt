package com.credair.core.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime

data class Flight(
    @JsonProperty("flight_id")
    val flightId: Long,
    
    @JsonProperty("flight_number")
    val flightNumber: String,
    
    @JsonProperty("src_airport_code")
    val srcAirportCode: String,
    
    @JsonProperty("dest_airport_code")
    val destAirportCode: String,
    
    @JsonProperty("departs_at")
    val departureTime: Timestamp,
    
    @JsonProperty("arrives_at")
    val arrivalTime: Timestamp,
    
    @JsonProperty("source_airport")
    val sourceAirport: String,
    
    @JsonProperty("destination_airport")
    val destinationAirport: String,
    
    @JsonProperty("price")
    val price: BigDecimal,
    
    @JsonProperty("currency")
    val currency: String = "USD",
    
    @JsonProperty("total_seats")
    val totalSeats: Int,
    
    @JsonProperty("available_seats")
    val availableSeats: Int,
    
    @JsonProperty("aircraft_type")
    val aircraftType: String? = null,
    
    @JsonProperty("airline_id")
    val airlineId: Long,
    
    @JsonProperty("active")
    val active: Boolean = true,
    
    @JsonProperty("created_at")
    val createdAt: Timestamp? = null,
    
    @JsonProperty("updated_at")
    val updatedAt: Timestamp? = null
) {
    
    val duration: Long
        get() = java.time.Duration.between(departureTime.toLocalDateTime(), arrivalTime.toLocalDateTime()).toMinutes()
    
    val isAvailable: Boolean
        get() = active && availableSeats > 0 && departureTime.toLocalDateTime().isAfter(LocalDateTime.now())
}