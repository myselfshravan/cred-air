package com.credair.core.dao.interfaces

import com.credair.core.model.Booking
import com.credair.common.dao.BaseDao
import java.time.LocalDateTime

interface BookingDao : BaseDao<Booking, Long> {
    fun findByBookingReference(bookingReference: String): Booking?
    fun findByBookingStatus(status: String): List<Booking>
    fun findByPaymentStatus(status: String): List<Booking>
    fun findByCreatedDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Booking>
    fun updateBookingStatus(bookingId: Long, status: String): Boolean
    fun updatePaymentStatus(bookingId: Long, status: String): Boolean
    fun updatePaymentTransactionId(bookingId: Long, transactionId: String): Boolean
}