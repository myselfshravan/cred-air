package com.credair.core.manager

import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.integration.airline.AirlineReservationService
import com.credair.core.integration.airline.PassengerInfo
import com.credair.core.integration.airline.ReservationRequest
import com.credair.core.model.Booking
import com.credair.core.model.BookingRequestPayload
import com.credair.core.model.BookingResult
import com.credair.core.model.BookingStatus
import com.credair.core.model.Flight
import com.credair.core.model.FlightBooking
import com.credair.core.model.FlightPricePayload
import com.credair.core.model.PassengerData
import com.credair.core.model.PaymentStatus
import com.credair.core.payment.PaymentProvider
import com.credair.core.repository.BookingRepository
import com.google.inject.Inject
import com.google.inject.Singleton
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Singleton
class BookingManager @Inject constructor(
    private val bookingDao: BookingDao,
    private val flightDao: FlightDao,
    private val bookingRepository: BookingRepository,
    private val paymentProvider: PaymentProvider,
    private val airlineReservationService: AirlineReservationService
) {

    suspend fun createBookingFromPayload(payload: BookingRequestPayload): BookingResult {
        validateBookingPayload(payload)
        validateFlightsAvailability(payload)
        
        // 1. Create all booking data in a single transaction
        val bookingData = bookingRepository.createBookingInTransaction(payload)
        
        // 2. Process airline reservations (external service calls)
        val reservations = processAirlineReservations(bookingData.flightBookings, payload.passengerData)
        
        // 3. Create payment session
        val paymentIntent = createPaymentSession(bookingData.mainBooking, payload.totalPrice)
        
        // 4. Update main booking with payment intent ID
        val updatedMainBooking = bookingData.mainBooking.copy(
            paymentTransactionId = paymentIntent.id
        )
        bookingDao.update(updatedMainBooking)
        
        return BookingResult(
            updatedMainBooking,
            bookingData.flightBookings,
            bookingData.passengers,
            paymentIntent
        )
    }

    fun getBookingById(id: Long): Booking {
        return bookingDao.findById(id)
            ?: throw IllegalArgumentException("Booking with id $id not found")
    }

    fun confirmBooking(bookingId: Long): Booking {
        val booking = getBookingById(bookingId)
        
        if (booking.bookingStatus !in listOf(BookingStatus.PENDING, BookingStatus.SOFT_RESERVED)) {
            throw IllegalStateException("Only pending or soft reserved bookings can be confirmed")
        }
        
        bookingDao.updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
        return getBookingById(bookingId)
    }

    fun cancelBooking(bookingId: Long): Booking {
        val booking = getBookingById(bookingId)
        bookingDao.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
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

    private fun validateSeatAvailability(flight: Flight, requestedSeats: Int) {
        if (flight.availableSeats < requestedSeats) {
            throw IllegalStateException(
                "Not enough seats available. Requested: $requestedSeats, Available: ${flight.availableSeats}"
            )
        }
    }

    private fun processPaymentExternal(booking: Booking, paymentMethod: String): Boolean {
        return true
    }

    private fun validateFlightsAvailability(payload: BookingRequestPayload) {
        payload.flightIds.forEach { flightId ->
            val flightIdLong = flightId.toLongOrNull()
                ?: throw IllegalArgumentException("Invalid flight ID: $flightId")
                
            val flight = flightDao.findById(flightIdLong)
                ?: throw IllegalArgumentException("Flight with id $flightId not found")
            
            if (!flight.isAvailable) {
                throw IllegalStateException("Flight $flightId is not available for booking")
            }
            
            validateSeatAvailability(flight, payload.passengerCount)
            
            val flightPrice = payload.flightPrices.find { it.flightId == flightId }
                ?: throw IllegalArgumentException("Price not found for flight $flightId")
            
            validateFlightPrice(flight, flightPrice)
        }
    }

    private fun validateBookingPayload(payload: BookingRequestPayload) {
        require(payload.flightIds.isNotEmpty()) { "At least one flight ID must be provided" }
        require(payload.passengerData.isNotEmpty()) { "At least one passenger must be provided" }
        require(payload.passengerCount > 0) { "Passenger count must be greater than 0" }
        require(payload.passengerCount <= 9) { "Maximum 9 passengers per booking" }
        require(payload.totalPrice > BigDecimal.ZERO) { "Total price must be greater than 0" }
        
        payload.passengerData.forEach { passenger ->
            validatePassengerData(passenger)
        }
        
        payload.flightPrices.forEach { flightPrice ->
            require(flightPrice.price > BigDecimal.ZERO) { "Flight price must be greater than 0" }
            require(flightPrice.currency.isNotBlank()) { "Currency cannot be blank" }
        }
        
        val providedFlightIds = payload.flightPrices.map { it.flightId }.toSet()
        val requestedFlightIds = payload.flightIds.toSet()
        require(providedFlightIds == requestedFlightIds) { "Flight prices must be provided for all requested flights" }
        
        val expectedTotal = payload.flightPrices.sumOf { it.price.multiply(BigDecimal(payload.passengerCount)) }
        require(payload.totalPrice.compareTo(expectedTotal) == 0) { 
            "Total price ${payload.totalPrice} does not match sum of flight prices $expectedTotal" 
        }
    }

    private fun validatePassengerData(passenger: PassengerData) {
        require(passenger.firstName.isNotBlank()) { "Passenger first name cannot be blank" }
        require(passenger.lastName.isNotBlank()) { "Passenger last name cannot be blank" }
        require(passenger.email.isNotBlank()) { "Passenger email cannot be blank" }
        require(passenger.email.contains("@")) { "Invalid email format for passenger ${passenger.firstName} ${passenger.lastName}" }
        require(passenger.phone.isNotBlank()) { "Passenger phone cannot be blank" }
        require(passenger.dateOfBirth.isNotBlank()) { "Passenger date of birth cannot be blank" }
        
        try {
            val birthDate = LocalDate.parse(passenger.dateOfBirth, DateTimeFormatter.ISO_LOCAL_DATE)
            val age = LocalDate.now().year - birthDate.year
            require(age >= 0 && age <= 120) { "Invalid age for passenger ${passenger.firstName} ${passenger.lastName}" }
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid date format for passenger ${passenger.firstName} ${passenger.lastName}")
        }
    }

    private fun validateFlightPrice(flight: Flight, flightPrice: FlightPricePayload) {
        require(flight.currency == flightPrice.currency) { 
            "Currency mismatch: flight currency ${flight.currency}, provided currency ${flightPrice.currency}" 
        }
        
        val priceDifference = flight.price.subtract(flightPrice.price).abs()
        val tolerance = flight.price.multiply(BigDecimal("0.05"))
        
        require(priceDifference <= tolerance) {
            "Price validation failed: flight price ${flight.price}, provided price ${flightPrice.price}" 
        }
    }


    fun getBookingConfirmation(bookingId: Long): String {
        return "Booking confirmation details for booking ID: $bookingId"
    }

    private suspend fun processAirlineReservations(
        flightBookings: List<FlightBooking>,
        passengerData: List<PassengerData>
    ): Map<String, String> {
        val reservationResults = mutableMapOf<String, String>()
        
        flightBookings.forEach { flightBooking ->
            val passengers = passengerData.map { passenger ->
                PassengerInfo(
                    firstName = passenger.firstName,
                    lastName = passenger.lastName,
                    email = passenger.email,
                    phone = passenger.phone,
                    dateOfBirth = passenger.dateOfBirth
                )
            }
            
            val flight = flightDao.findById(flightBooking.flightId.toLong())
                ?: throw IllegalArgumentException("Flight with id ${flightBooking.flightId} not found")
            
            val reservationRequest = ReservationRequest(
                booking = Booking(
                    bookingReference = "TEMP-${System.currentTimeMillis()}",
                    totalPrice = flightBooking.totalFlightPrice,
                    currency = flightBooking.currency,
                    passengerCount = flightBooking.passengerCount,
                    bookingStatus = BookingStatus.PENDING,
                    paymentStatus = PaymentStatus.PENDING
                ),
                flight = flight,
                passengers = passengers
            )
            
            val reservationResponse = airlineReservationService.softReserve(reservationRequest)
            
            if (!reservationResponse.success) {
                throw IllegalStateException("Failed to reserve flight ${flightBooking.flightId}: ${reservationResponse.error}")
            }
            
            reservationResults[flightBooking.flightId] = reservationResponse.airlineConfirmationCode ?: ""
        }
        
        return reservationResults
    }
    
    private fun createPaymentSession(
        mainBooking: Booking,
        totalPrice: BigDecimal
    ): PaymentProvider.PaymentIntent {
        val bookingForPayment = mainBooking.copy(totalPrice = totalPrice)
        return paymentProvider.createPaymentIntent(bookingForPayment)
    }
}