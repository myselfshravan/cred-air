package com.credair.core.manager

import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.Booking
import com.credair.core.model.BookingStatus
import com.credair.core.model.PaymentStatus
import com.credair.core.model.CheckInStatus
import com.google.inject.Inject
import com.google.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Singleton
class BookingManager @Inject constructor(
    private val bookingDao: BookingDao,
    private val flightDao: FlightDao
) {

    data class BookingRequest(
        val flightId: Long,
        val passengerName: String,
        val passengerEmail: String,
        val passengerPhone: String? = null,
        val numberOfSeats: Int = 1,
        val specialRequests: String? = null,
        val paymentMethod: String? = null
    )

    fun createBooking(request: BookingRequest): Booking {
        validateBookingRequest(request)
        
        val flight = flightDao.findById(request.flightId)
            ?: throw IllegalArgumentException("Flight with id ${request.flightId} not found")
        
        if (!flight.isAvailable) {
            throw IllegalStateException("Flight is not available for booking")
        }
        
        validateSeatAvailability(flight.flightId!!, request.numberOfSeats)
        
        val bookingReference = generateBookingReference()
        val totalPrice = calculateTotalPrice(flight.price, request.numberOfSeats)
        
        val booking = Booking(
            bookingReference = bookingReference,
            flightId = request.flightId,
            passengerName = request.passengerName,
            passengerEmail = request.passengerEmail,
            passengerPhone = request.passengerPhone,
            numberOfSeats = request.numberOfSeats,
            totalPrice = totalPrice,
            currency = flight.currency,
            specialRequests = request.specialRequests,
            paymentMethod = request.paymentMethod,
            bookingDate = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val savedBooking = bookingDao.save(booking)
        updateFlightAvailableSeats(flight.flightId!!, request.numberOfSeats)
        
        return savedBooking
    }

    fun getBookingById(id: Long): Booking {
        return bookingDao.findById(id)
            ?: throw IllegalArgumentException("Booking with id $id not found")
    }

    fun getBookingByReference(bookingReference: String): Booking {
        return bookingDao.findByBookingReference(bookingReference)
            ?: throw IllegalArgumentException("Booking with reference $bookingReference not found")
    }

    fun getBookingsByPassengerEmail(passengerEmail: String): List<Booking> {
        return bookingDao.findByPassengerEmail(passengerEmail)
    }

    fun getBookingsByFlightId(flightId: Long): List<Booking> {
        return bookingDao.findByFlightId(flightId)
    }

    fun confirmBooking(bookingId: Long): Booking {
        val booking = getBookingById(bookingId)
        
        if (booking.bookingStatus != BookingStatus.PENDING) {
            throw IllegalStateException("Only pending bookings can be confirmed")
        }
        
        bookingDao.updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
        return getBookingById(bookingId)
    }

    fun cancelBooking(bookingId: Long, reason: String? = null): Booking {
        val booking = getBookingById(bookingId)
        
        if (!booking.canBeCancelled) {
            throw IllegalStateException("Booking cannot be cancelled")
        }
        
        bookingDao.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
        
        val flight = flightDao.findById(booking.flightId)
        flight?.let {
            val newAvailableSeats = it.availableSeats + booking.numberOfSeats
            flightDao.updateAvailableSeats(it.flightId!!, newAvailableSeats)
        }
        
        if (booking.isPaid) {
            processRefund(bookingId)
        }
        
        return getBookingById(bookingId)
    }

    fun processPayment(bookingId: Long, paymentMethod: String): Booking {
        val booking = getBookingById(bookingId)
        
        if (booking.paymentStatus == PaymentStatus.PAID) {
            throw IllegalStateException("Booking is already paid")
        }
        
        if (booking.bookingStatus == BookingStatus.CANCELLED) {
            throw IllegalStateException("Cannot process payment for cancelled booking")
        }
        
        val paymentSuccessful = processPaymentExternal(booking, paymentMethod)
        
        if (paymentSuccessful) {
            bookingDao.updatePaymentStatus(bookingId, PaymentStatus.PAID)
            if (booking.bookingStatus == BookingStatus.PENDING) {
                bookingDao.updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
            }
        } else {
            bookingDao.updatePaymentStatus(bookingId, PaymentStatus.FAILED)
            throw IllegalStateException("Payment processing failed")
        }
        
        return getBookingById(bookingId)
    }

    fun checkInPassenger(bookingId: Long, seatNumber: String? = null): Booking {
        val booking = getBookingById(bookingId)
        
        if (booking.bookingStatus != BookingStatus.CONFIRMED) {
            throw IllegalStateException("Only confirmed bookings can be checked in")
        }
        
        if (!booking.isPaid) {
            throw IllegalStateException("Payment must be completed before check-in")
        }
        
        val flight = flightDao.findById(booking.flightId)
        flight?.let {
            if (it.departureTime.toLocalDateTime().isBefore(LocalDateTime.now().plusHours(2))) {
                throw IllegalStateException("Check-in window has closed")
            }
        }
        
        bookingDao.updateCheckInStatus(bookingId, CheckInStatus.CHECKED_IN)
        
        seatNumber?.let {
            val updatedBooking = booking.copy(
                seatNumber = it,
                updatedAt = LocalDateTime.now()
            )
            bookingDao.update(updatedBooking)
        }
        
        return getBookingById(bookingId)
    }

    fun getBookingStatistics(flightId: Long): Map<String, Any> {
        val totalBookings = bookingDao.countBookingsByFlightId(flightId)
        val totalSeatsBooked = bookingDao.getTotalSeatsBookedForFlight(flightId)
        
        val flight = flightDao.findById(flightId)
        val occupancyRate = flight?.let {
            (totalSeatsBooked.toDouble() / it.totalSeats.toDouble()) * 100
        } ?: 0.0
        
        return mapOf(
            "totalBookings" to totalBookings,
            "totalSeatsBooked" to totalSeatsBooked,
            "occupancyRate" to String.format("%.2f%%", occupancyRate),
            "availableSeats" to (flight?.availableSeats ?: 0)
        )
    }

    private fun validateBookingRequest(request: BookingRequest) {
        require(request.passengerName.isNotBlank()) { "Passenger name cannot be blank" }
        require(request.passengerEmail.isNotBlank()) { "Passenger email cannot be blank" }
        require(request.passengerEmail.contains("@")) { "Invalid email format" }
        require(request.numberOfSeats > 0) { "Number of seats must be greater than 0" }
        require(request.numberOfSeats <= 9) { "Maximum 9 seats per booking" }
    }

    private fun validateSeatAvailability(flightId: Long, requestedSeats: Int) {
        val flight = flightDao.findById(flightId)
            ?: throw IllegalArgumentException("Flight not found")
        
        if (flight.availableSeats < requestedSeats) {
            throw IllegalStateException(
                "Not enough seats available. Requested: $requestedSeats, Available: ${flight.availableSeats}"
            )
        }
    }

    private fun generateBookingReference(): String {
        val prefix = "CR"
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        val random = Random().nextInt(9999).toString().padStart(4, '0')
        return "$prefix$timestamp$random"
    }

    private fun calculateTotalPrice(unitPrice: BigDecimal, numberOfSeats: Int): BigDecimal {
        return unitPrice.multiply(BigDecimal(numberOfSeats))
    }

    private fun updateFlightAvailableSeats(flightId: Long, seatsBooked: Int) {
        val flight = flightDao.findById(flightId)
        flight?.let {
            val newAvailableSeats = it.availableSeats - seatsBooked
            flightDao.updateAvailableSeats(flightId, newAvailableSeats)
        }
    }

    private fun processPaymentExternal(booking: Booking, paymentMethod: String): Boolean {
        return true
    }

    private fun processRefund(bookingId: Long) {
        bookingDao.updatePaymentStatus(bookingId, PaymentStatus.REFUNDED)
    }
}