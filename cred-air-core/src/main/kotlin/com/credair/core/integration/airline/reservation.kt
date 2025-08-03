package com.credair.core.integration.airline

import com.credair.core.model.Booking
import com.credair.core.model.Flight

data class ReservationRequest(
    val booking: Booking,
    val flight: Flight,
    val passengers: List<PassengerInfo>
)

data class PassengerInfo(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val passportNumber: String? = null
)

data class ReservationResponse(
    val success: Boolean,
    val airlineConfirmationCode: String? = null,
    val pnr: String? = null,
    val seatAssignments: List<SeatAssignment>? = null,
    val error: String? = null
)

data class SeatAssignment(
    val passengerName: String,
    val seatNumber: String,
    val seatClass: String
)

data class CancellationRequest(
    val airlineConfirmationCode: String,
    val pnr: String,
    val reason: String? = null
)

data class CancellationResponse(
    val success: Boolean,
    val refundAmount: String? = null,
    val cancellationFee: String? = null,
    val error: String? = null
)