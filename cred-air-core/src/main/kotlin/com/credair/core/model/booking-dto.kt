package com.credair.core.model

import java.math.BigDecimal

data class BookingRequestPayload(
    val flightIds: List<String>,
    val passengerData: List<PassengerData>,
    val flightPrices: List<FlightPricePayload>,
    val totalPrice: BigDecimal,
    val passengerCount: Int
)

data class FlightPricePayload(
    val flightId: String,
    val price: BigDecimal,
    val currency: String
)

data class PassengerData(
    val id: String,
    val title: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val email: String,
    val phone: String
)

data class BookingResult(
    val mainBooking: Booking,
    val flightBookings: List<FlightBooking>,
    val passengers: List<FlightPassenger>,
    val paymentIntent: com.credair.core.payment.PaymentProvider.PaymentIntent
)