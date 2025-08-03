package com.credair.core.dao.interfaces

import com.credair.core.model.FlightBooking
import com.credair.common.dao.BaseDao

interface FlightBookingDao : BaseDao<FlightBooking, Long> {
    fun findByBookingId(bookingId: Long): List<FlightBooking>
    fun findByFlightId(flightId: String): List<FlightBooking>
    fun findByPnr(pnr: String): FlightBooking?
    fun findByBookingIdAndFlightId(bookingId: Long, flightId: String): FlightBooking?
    fun updateStatus(id: Long, status: String): Boolean
    fun updatePnr(id: Long, pnr: String): Boolean
}