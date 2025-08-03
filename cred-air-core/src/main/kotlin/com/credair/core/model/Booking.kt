package com.credair.core.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDate

data class Booking(
    @JsonProperty("id")
    val id: Long? = null,
    
    @JsonProperty("booking_reference")
    val bookingReference: String,
    
    @JsonProperty("total_price")
    val totalPrice: BigDecimal,
    
    @JsonProperty("currency")
    val currency: String,
    
    @JsonProperty("passenger_count")
    val passengerCount: Int,
    
    @JsonProperty("booking_status")
    val bookingStatus: String,
    
    @JsonProperty("payment_status")
    val paymentStatus: String,
    
    @JsonProperty("payment_method")
    val paymentMethod: String? = null,
    
    @JsonProperty("payment_transaction_id")
    val paymentTransactionId: String? = null,
    
    @JsonProperty("created_at")
    val createdAt: Timestamp? = null,
    
    @JsonProperty("updated_at")
    val updatedAt: Timestamp? = null
)

data class FlightBooking(
    @JsonProperty("id")
    val id: Long? = null,
    
    @JsonProperty("booking_id")
    val bookingId: Long,
    
    @JsonProperty("flight_id")
    val flightId: String,
    
    @JsonProperty("pnr")
    val pnr: String? = null,
    
    @JsonProperty("total_flight_price")
    val totalFlightPrice: BigDecimal,
    
    @JsonProperty("currency")
    val currency: String,
    
    @JsonProperty("passenger_count")
    val passengerCount: Int,
    
    @JsonProperty("booking_class")
    val bookingClass: String? = null,
    
    @JsonProperty("status")
    val status: String,
    
    @JsonProperty("created_at")
    val createdAt: Timestamp? = null,
    
    @JsonProperty("updated_at")
    val updatedAt: Timestamp? = null
)

data class FlightPassenger(
    @JsonProperty("id")
    val id: Long? = null,
    
    @JsonProperty("flight_booking_id")
    val flightBookingId: Long,
    
    @JsonProperty("passenger_external_id")
    val passengerExternalId: String? = null,
    
    @JsonProperty("title")
    val title: String? = null,
    
    @JsonProperty("first_name")
    val firstName: String,
    
    @JsonProperty("last_name")
    val lastName: String,
    
    @JsonProperty("date_of_birth")
    val dateOfBirth: LocalDate,
    
    @JsonProperty("email")
    val email: String,
    
    @JsonProperty("phone")
    val phone: String? = null,
    
    @JsonProperty("seat_number")
    val seatNumber: String? = null,
    
    @JsonProperty("ticket_number")
    val ticketNumber: String? = null,
    
    @JsonProperty("individual_price")
    val individualPrice: BigDecimal,
    
    @JsonProperty("created_at")
    val createdAt: Timestamp? = null,
    
    @JsonProperty("updated_at")
    val updatedAt: Timestamp? = null
)