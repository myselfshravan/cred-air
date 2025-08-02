package com.credair.core.model

import com.credair.common.model.BaseEntity
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class Flight(
    @JsonProperty("id")
    val id: Long? = null,
    
    @JsonProperty("airline_id")
    val airlineId: Long,
    
    @JsonProperty("flight_number")
    val flightNumber: String,
    
    @JsonProperty("source_airport")
    val sourceAirport: String,
    
    @JsonProperty("destination_airport")
    val destinationAirport: String,
    
    @JsonProperty("departure_time")
    val departureTime: LocalDateTime,
    
    @JsonProperty("arrival_time")
    val arrivalTime: LocalDateTime,
    
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
    
    @JsonProperty("active")
    val active: Boolean = true,
    
    @JsonProperty("created_at")
    val createdAt: LocalDateTime? = null,
    
    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime? = null
) {
    
    val duration: Long
        get() = java.time.Duration.between(departureTime, arrivalTime).toMinutes()
    
    val isAvailable: Boolean
        get() = active && availableSeats > 0 && departureTime.isAfter(LocalDateTime.now())
}