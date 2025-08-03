package com.credair.core.repository

import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.dao.interfaces.FlightBookingDao
import com.credair.core.dao.interfaces.FlightPassengerDao
import com.credair.core.model.Booking
import com.credair.core.model.BookingStatus
import com.credair.core.model.FlightBooking
import com.credair.core.model.FlightPassenger
import com.credair.core.model.BookingRequestPayload
import com.credair.core.model.PassengerData
import com.credair.core.model.PaymentStatus
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import java.math.BigDecimal
import java.time.LocalDateTime

@Singleton
class BookingRepository @Inject constructor(
    private val jdbi: Jdbi,
    private val bookingDao: BookingDao,
    private val flightBookingDao: FlightBookingDao,
    private val flightPassengerDao: FlightPassengerDao
) {
    
    data class BookingData(
        val mainBooking: Booking,
        val flightBookings: List<FlightBooking>,
        val passengers: List<FlightPassenger>
    )
    
    fun createBookingInTransaction(payload: BookingRequestPayload): BookingData {
        return jdbi.inTransaction<BookingData, Exception> { handle ->
            val now = LocalDateTime.now()
            
            // 1. Create main booking
            val mainBooking = createMainBooking(payload, now)
            
            // 2. Create flight bookings
            val flightBookings = createFlightBookings(mainBooking.id!!, payload, now)
            
            // 3. Create passengers for each flight booking
            val passengers = createFlightPassengers(flightBookings, payload.passengerData, now)
            
            BookingData(mainBooking, flightBookings, passengers)
        }
    }
    
    private fun createMainBooking(payload: BookingRequestPayload, now: LocalDateTime): Booking {
        val booking = Booking(
            bookingReference = generateBookingReference(),
            totalPrice = payload.totalPrice,
            currency = payload.flightPrices.first().currency,
            passengerCount = payload.passengerCount,
            bookingStatus = BookingStatus.SOFT_RESERVED,
            paymentStatus = PaymentStatus.PENDING,
            createdAt = java.sql.Timestamp.valueOf(now),
            updatedAt = java.sql.Timestamp.valueOf(now)
        )
        
        return bookingDao.save(booking)
    }
    
    private fun createFlightBookings(
        bookingId: Long,
        payload: BookingRequestPayload,
        now: LocalDateTime
    ): List<FlightBooking> {
        return payload.flightIds.map { flightId ->
            val flightPrice = payload.flightPrices.find { it.flightId == flightId }
                ?: throw IllegalArgumentException("Price not found for flight $flightId")
            
            val flightBooking = FlightBooking(
                bookingId = bookingId,
                flightId = flightId,
                totalFlightPrice = flightPrice.price.multiply(BigDecimal(payload.passengerCount)),
                currency = flightPrice.currency,
                passengerCount = payload.passengerCount,
                status = "SOFT_RESERVED",
                createdAt = java.sql.Timestamp.valueOf(now),
                updatedAt = java.sql.Timestamp.valueOf(now)
            )
            
            flightBookingDao.save(flightBooking)
        }
    }
    
    private fun createFlightPassengers(
        flightBookings: List<FlightBooking>,
        passengerData: List<PassengerData>,
        now: LocalDateTime
    ): List<FlightPassenger> {
        val passengers = mutableListOf<FlightPassenger>()
        
        flightBookings.forEach { flightBooking ->
            val flightPrice = flightBooking.totalFlightPrice.divide(BigDecimal(flightBooking.passengerCount))
            
            passengerData.forEach { passenger ->
                val flightPassenger = FlightPassenger(
                    flightBookingId = flightBooking.id!!,
                    passengerExternalId = passenger.id,
                    title = passenger.title,
                    firstName = passenger.firstName,
                    lastName = passenger.lastName,
                    dateOfBirth = java.time.LocalDate.parse(passenger.dateOfBirth),
                    email = passenger.email,
                    phone = passenger.phone,
                    individualPrice = flightPrice,
                    createdAt = java.sql.Timestamp.valueOf(now),
                    updatedAt = java.sql.Timestamp.valueOf(now)
                )
                
                passengers.add(flightPassengerDao.save(flightPassenger))
            }
        }
        
        return passengers
    }
    
    private fun generateBookingReference(): String {
        return "BK${System.currentTimeMillis()}"
    }
}