package com.credair.core.dao.interfaces

import com.credair.core.model.FlightPassenger
import com.credair.common.dao.BaseDao

interface FlightPassengerDao : BaseDao<FlightPassenger, Long> {
    fun findByFlightBookingId(flightBookingId: Long): List<FlightPassenger>
    fun findByEmail(email: String): List<FlightPassenger>
    fun findByExternalId(externalId: String): FlightPassenger?
    fun findByFlightBookingIdAndExternalId(flightBookingId: Long, externalId: String): FlightPassenger?
    fun updateSeatNumber(id: Long, seatNumber: String): Boolean
    fun updateTicketNumber(id: Long, ticketNumber: String): Boolean
}