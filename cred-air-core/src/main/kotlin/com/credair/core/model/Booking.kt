package com.credair.core.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class Booking(
    @JsonProperty("id")
    val id: Long? = null,

    @JsonProperty("booking_reference")
    val bookingReference: String,

    @JsonProperty("flight_id")
    val flightId: Long,

    @JsonProperty("passenger_name")
    val passengerName: String,

    @JsonProperty("passenger_email")
    val passengerEmail: String,

    @JsonProperty("passenger_phone")
    val passengerPhone: String? = null,

    @JsonProperty("seat_number")
    val seatNumber: String? = null,

    @JsonProperty("number_of_seats")
    val numberOfSeats: Int = 1,

    @JsonProperty("total_price")
    val totalPrice: BigDecimal,

    @JsonProperty("currency")
    val currency: String = "USD",

    @JsonProperty("booking_status")
    val bookingStatus: BookingStatus = BookingStatus.PENDING,

    @JsonProperty("payment_status")
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @JsonProperty("booking_date")
    val bookingDate: LocalDateTime = LocalDateTime.now(),

    @JsonProperty("payment_method")
    val paymentMethod: String? = null,

    @JsonProperty("special_requests")
    val specialRequests: String? = null,

    @JsonProperty("check_in_status")
    val checkInStatus: CheckInStatus = CheckInStatus.NOT_CHECKED_IN,

    @JsonProperty("created_at")
    val createdAt: LocalDateTime? = null,

    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime? = null
) {

    val isActive: Boolean
        get() = bookingStatus in listOf(BookingStatus.CONFIRMED, BookingStatus.PENDING)

    val canBeCancelled: Boolean
        get() = bookingStatus in listOf(BookingStatus.CONFIRMED, BookingStatus.PENDING) &&
                bookingDate.isBefore(LocalDateTime.now().minusHours(24))

    val isPaid: Boolean
        get() = paymentStatus == PaymentStatus.PAID
}

enum class BookingStatus {
    @JsonProperty("pending")
    PENDING,

    @JsonProperty("confirmed")
    CONFIRMED,

    @JsonProperty("cancelled")
    CANCELLED,

    @JsonProperty("completed")
    COMPLETED,

    @JsonProperty("no_show")
    NO_SHOW
}

enum class PaymentStatus {
    @JsonProperty("pending")
    PENDING,

    @JsonProperty("paid")
    PAID,

    @JsonProperty("failed")
    FAILED,

    @JsonProperty("refunded")
    REFUNDED,

    @JsonProperty("partially_refunded")
    PARTIALLY_REFUNDED
}

enum class CheckInStatus {
    @JsonProperty("not_checked_in")
    NOT_CHECKED_IN,

    @JsonProperty("checked_in")
    CHECKED_IN,

    @JsonProperty("boarding_pass_issued")
    BOARDING_PASS_ISSUED
}