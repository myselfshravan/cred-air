package com.credair.core.dao.interfaces

import com.credair.core.model.Booking
import com.credair.core.model.BookingStatus
import com.credair.core.model.PaymentStatus
import com.credair.common.dao.BaseDao
import java.time.LocalDateTime

interface BookingDao : BaseDao<Booking, Long> {
    fun findByBookingReference(bookingReference: String): Booking?
    fun findByBookingStatus(status: BookingStatus): List<Booking>
    fun findByPaymentStatus(status: PaymentStatus): List<Booking>
    fun findByCreatedDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Booking>
    fun updateBookingStatus(bookingId: Long, status: BookingStatus): Boolean
    fun updatePaymentStatus(bookingId: Long, status: PaymentStatus): Boolean
    fun updatePaymentTransactionId(bookingId: Long, transactionId: String): Boolean
}