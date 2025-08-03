package com.credair.core.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BookingRequestPayload @JsonCreator constructor(
    @JsonProperty("flightIds") val flightIds: List<String>,
    @JsonProperty("passengerData") val passengerData: List<PassengerData>,
    @JsonProperty("flightPrices") val flightPrices: List<FlightPricePayload>,
    @JsonProperty("totalPrice") val totalPrice: BigDecimal,
    @JsonProperty("passengerCount") val passengerCount: Int
)

data class FlightPricePayload @JsonCreator constructor(
    @JsonProperty("flightId") val flightId: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: String
)

data class PassengerData @JsonCreator constructor(
    @JsonProperty("id") val id: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("dateOfBirth") val dateOfBirth: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("phone") val phone: String
)

data class BookingResult(
    val mainBooking: Booking,
    val flightBookings: List<FlightBooking>,
    val passengers: List<FlightPassenger>,
    val paymentIntent: com.credair.core.payment.PaymentProvider.PaymentIntent
)