package com.credair.core.dao.interfaces

import com.credair.core.model.Booking
import com.credair.core.model.BookingStatus
import com.credair.core.model.PaymentStatus
import com.credair.common.dao.BaseDao
import java.time.LocalDateTime

interface BookingDao : BaseDao<Booking, Long> {
    fun findByBookingReference(bookingReference: String): Booking?
    fun findByFlightId(flightId: Long): List<Booking>
    fun findByPassengerEmail(passengerEmail: String): List<Booking>
    fun findByPassengerEmailAndFlightId(passengerEmail: String, flightId: Long): List<Booking>
    fun findByBookingStatus(status: BookingStatus): List<Booking>
    fun findByPaymentStatus(status: PaymentStatus): List<Booking>
    fun findByBookingDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Booking>
    fun findActiveBookings(): List<Booking>
    fun updateBookingStatus(bookingId: Long, status: BookingStatus): Boolean
    fun updatePaymentStatus(bookingId: Long, status: PaymentStatus): Boolean
    fun updateCheckInStatus(bookingId: Long, checkInStatus: com.credair.core.model.CheckInStatus): Boolean
    fun countBookingsByFlightId(flightId: Long): Int
    fun getTotalSeatsBookedForFlight(flightId: Long): Int
}