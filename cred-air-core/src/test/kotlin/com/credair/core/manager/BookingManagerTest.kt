package com.credair.core.manager

import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.dao.interfaces.FlightBookingDao
import com.credair.core.dao.interfaces.FlightPassengerDao
import com.credair.core.integration.airline.AirlineIntegrationManager
import com.credair.core.integration.airline.AirlineReservationService
import com.credair.core.integration.airline.ReservationResponse
import com.credair.core.model.*
import com.credair.core.payment.PaymentProvider
import com.credair.core.repository.BookingRepository
import org.jdbi.v3.core.Jdbi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Disabled
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Disabled("BookingRepository final class mocking issues - needs proper implementation")
class BookingManagerTest {

    private lateinit var bookingDao: BookingDao
    private lateinit var flightDao: FlightDao
    private lateinit var airlineDao: AirlineDao
    private lateinit var flightBookingDao: FlightBookingDao
    private lateinit var flightPassengerDao: FlightPassengerDao
    private lateinit var jdbi: Jdbi
    private lateinit var bookingRepository: BookingRepository
    private lateinit var paymentProvider: PaymentProvider
    private lateinit var airlineIntegrationManager: AirlineIntegrationManager
    private lateinit var bookingManager: BookingManager

    @BeforeEach
    fun setUp() {
        bookingDao = mock()
        flightDao = mock()
        airlineDao = mock()
        flightBookingDao = mock()
        flightPassengerDao = mock()
        bookingRepository = mock() // TODO: Replace with real instance when Jdbi mocking is fixed
        paymentProvider = mock()
        airlineIntegrationManager = mock()
        bookingManager = BookingManager(
            bookingDao,
            flightDao,
            airlineDao,
            bookingRepository,
            paymentProvider,
            airlineIntegrationManager
        )
    }

    // Validation Tests
    @Test
    fun `createBookingFromPayload should throw exception when no flight IDs provided`() {
        val payload = createValidBookingPayload().copy(flightIds = emptyList())

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("At least one flight ID must be provided", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when no passenger data provided`() {
        val payload = createValidBookingPayload().copy(passengerData = emptyList())

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("At least one passenger must be provided", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when passenger count is zero`() {
        val payload = createValidBookingPayload().copy(passengerCount = 0)

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Passenger count must be greater than 0", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when passenger count exceeds limit`() {
        val payload = createValidBookingPayload().copy(passengerCount = 10)

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Maximum 9 passengers per booking", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when total price is zero`() {
        val payload = createValidBookingPayload().copy(totalPrice = BigDecimal.ZERO)

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Total price must be greater than 0", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when passenger first name is blank`() {
        val invalidPassenger = createValidPassengerData().copy(firstName = "")
        val payload = createValidBookingPayload().copy(passengerData = listOf(invalidPassenger))

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Passenger first name cannot be blank", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when passenger email is invalid`() {
        val invalidPassenger = createValidPassengerData().copy(email = "invalid-email")
        val payload = createValidBookingPayload().copy(passengerData = listOf(invalidPassenger))

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Invalid email format for passenger John Doe", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when flight prices don't match flight IDs`() {
        val payload = createValidBookingPayload().copy(
            flightIds = listOf("1", "2"),
            flightPrices = listOf(createValidFlightPrice("1"))
        )

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Flight prices must be provided for all requested flights", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when total price doesn't match sum`() {
        val flightPrice = createValidFlightPrice("1", BigDecimal("100"))
        val payload = createValidBookingPayload().copy(
            flightPrices = listOf(flightPrice),
            totalPrice = BigDecimal("150"), // Should be 200 (100 * 2 passengers)
            passengerCount = 2
        )

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Total price 150 does not match sum of flight prices 200", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when flight not found`() {
        val payload = createValidBookingPayload()

        whenever(flightDao.findById(1L)).thenReturn(null)

        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Flight with id 1 not found", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when flight not available`() {
        val flight = createValidFlight().copy(active = false)
        val payload = createValidBookingPayload()

        whenever(flightDao.findById(1L)).thenReturn(flight)

        val exception = assertThrows<IllegalStateException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Flight 1 is not available for booking", exception.message)
    }

    @Test
    fun `createBookingFromPayload should throw exception when not enough seats available`() {
        val flight = createValidFlight().copy(availableSeats = 1)
        val payload = createValidBookingPayload().copy(passengerCount = 2)

        whenever(flightDao.findById(1L)).thenReturn(flight)

        val exception = assertThrows<IllegalStateException> {
            runBlocking { bookingManager.createBookingFromPayload(payload) }
        }
        assertEquals("Not enough seats available. Requested: 2, Available: 1", exception.message)
    }

    @Test
    fun `createBookingFromPayload should successfully create booking`() = runBlocking {
        val payload = createValidBookingPayload()
        val flight = createValidFlight()
        val airline = createValidAirline()
        val bookingData = createValidBookingData()
        val paymentIntent = createValidPaymentIntent()
        val reservationService = mock<AirlineReservationService>()
        val reservationResponse = ReservationResponse(true, "ABC123", null)

        // Setup mocks
        whenever(flightDao.findById(1L)).thenReturn(flight)
        whenever(bookingRepository.createBookingInTransaction(payload)).thenReturn(bookingData)
        whenever(airlineDao.findById(flight.airlineId)).thenReturn(airline)
        whenever(airlineIntegrationManager.getReservationService("AI")).thenReturn(reservationService)
        whenever(reservationService.softReserve(any())).thenReturn(reservationResponse)
        whenever(paymentProvider.createPaymentIntent(any())).thenReturn(paymentIntent)
        whenever(bookingDao.update(any())).thenReturn(bookingData.mainBooking)

        val result = bookingManager.createBookingFromPayload(payload)

        assertNotNull(result)
        assertEquals(bookingData.mainBooking, result.mainBooking)
        assertEquals(bookingData.flightBookings, result.flightBookings)
        assertEquals(bookingData.passengers, result.passengers)
        assertEquals(paymentIntent, result.paymentIntent)

        verify(flightDao).findById(1L)
        verify(bookingRepository).createBookingInTransaction(payload)
        verify(paymentProvider).createPaymentIntent(any())
        verify(bookingDao).update(any())
    }

    // Booking retrieval tests
    @Test
    fun `getBookingById should return booking when found`() {
        val booking = createValidBooking()

        whenever(bookingDao.findById(1L)).thenReturn(booking)

        val result = bookingManager.getBookingById(1L)

        assertEquals(booking, result)
        verify(bookingDao).findById(1L)
    }

    @Test
    fun `getBookingById should throw exception when not found`() {
        whenever(bookingDao.findById(999L)).thenReturn(null)

        val exception = assertThrows<IllegalArgumentException> {
            bookingManager.getBookingById(999L)
        }
        assertEquals("Booking with id 999 not found", exception.message)
    }

    // Booking confirmation tests
    @Test
    fun `confirmBooking should successfully confirm pending booking`() {
        val pendingBooking = createValidBooking().copy(bookingStatus = BookingStatus.PENDING)
        val confirmedBooking = pendingBooking.copy(bookingStatus = BookingStatus.CONFIRMED)

        whenever(bookingDao.findById(1L)).thenReturn(pendingBooking).thenReturn(confirmedBooking)

        val result = bookingManager.confirmBooking(1L)

        assertEquals(BookingStatus.CONFIRMED, result.bookingStatus)
        verify(bookingDao).updateBookingStatus(1L, BookingStatus.CONFIRMED)
    }

    @Test
    fun `confirmBooking should successfully confirm soft reserved booking`() {
        val softReservedBooking = createValidBooking().copy(bookingStatus = BookingStatus.SOFT_RESERVED)
        val confirmedBooking = softReservedBooking.copy(bookingStatus = BookingStatus.CONFIRMED)

        whenever(bookingDao.findById(1L)).thenReturn(softReservedBooking).thenReturn(confirmedBooking)

        val result = bookingManager.confirmBooking(1L)

        assertEquals(BookingStatus.CONFIRMED, result.bookingStatus)
        verify(bookingDao).updateBookingStatus(1L, BookingStatus.CONFIRMED)
    }

    @Test
    fun `confirmBooking should throw exception for confirmed booking`() {
        val confirmedBooking = createValidBooking().copy(bookingStatus = BookingStatus.CONFIRMED)

        whenever(bookingDao.findById(1L)).thenReturn(confirmedBooking)

        val exception = assertThrows<IllegalStateException> {
            bookingManager.confirmBooking(1L)
        }
        assertEquals("Only pending or soft reserved bookings can be confirmed", exception.message)
    }

    // Booking cancellation tests
    @Test
    fun `cancelBooking should successfully cancel booking`() {
        val booking = createValidBooking()
        val cancelledBooking = booking.copy(bookingStatus = BookingStatus.CANCELLED)

        whenever(bookingDao.findById(1L)).thenReturn(booking).thenReturn(cancelledBooking)

        val result = bookingManager.cancelBooking(1L)

        assertEquals(BookingStatus.CANCELLED, result.bookingStatus)
        verify(bookingDao).updateBookingStatus(1L, BookingStatus.CANCELLED)
    }

    // Payment processing tests
    @Test
    fun `processPayment should successfully process payment for pending booking`() {
        val pendingBooking = createValidBooking().copy(
            bookingStatus = BookingStatus.PENDING,
            paymentStatus = PaymentStatus.PENDING
        )
        val paidBooking = pendingBooking.copy(
            paymentStatus = PaymentStatus.PAID,
            bookingStatus = BookingStatus.CONFIRMED
        )

        whenever(bookingDao.findById(1L)).thenReturn(pendingBooking).thenReturn(paidBooking)

        val result = bookingManager.processPayment(1L, "card")

        assertEquals(PaymentStatus.PAID, result.paymentStatus)
        assertEquals(BookingStatus.CONFIRMED, result.bookingStatus)
        verify(bookingDao).updatePaymentStatus(1L, PaymentStatus.PAID)
        verify(bookingDao).updateBookingStatus(1L, BookingStatus.CONFIRMED)
    }

    @Test
    fun `processPayment should throw exception for already paid booking`() {
        val paidBooking = createValidBooking().copy(paymentStatus = PaymentStatus.PAID)

        whenever(bookingDao.findById(1L)).thenReturn(paidBooking)

        val exception = assertThrows<IllegalStateException> {
            bookingManager.processPayment(1L, "card")
        }
        assertEquals("Booking is already paid", exception.message)
    }

    @Test
    fun `processPayment should throw exception for cancelled booking`() {
        val cancelledBooking = createValidBooking().copy(bookingStatus = BookingStatus.CANCELLED)

        whenever(bookingDao.findById(1L)).thenReturn(cancelledBooking)

        val exception = assertThrows<IllegalStateException> {
            bookingManager.processPayment(1L, "card")
        }
        assertEquals("Cannot process payment for cancelled booking", exception.message)
    }

    @Test
    fun `getBookingConfirmation should return confirmation message`() {
        val result = bookingManager.getBookingConfirmation(123L)
        assertEquals("Booking confirmation details for booking ID: 123", result)
    }

    // Helper methods
    private fun createValidBookingPayload() = BookingRequestPayload(
        flightIds = listOf("1"),
        passengerData = listOf(createValidPassengerData()),
        flightPrices = listOf(createValidFlightPrice("1")),
        totalPrice = BigDecimal("200"), // 100 * 2 passengers
        passengerCount = 2
    )

    private fun createValidPassengerData() = PassengerData(
        id = "1",
        title = "Mr",
        firstName = "John",
        lastName = "Doe",
        dateOfBirth = "1990-01-01",
        email = "john.doe@example.com",
        phone = "+1234567890"
    )

    private fun createValidFlightPrice(flightId: String, price: BigDecimal = BigDecimal("100")) = FlightPricePayload(
        flightId = flightId,
        price = price,
        currency = "USD"
    )

    private fun createValidFlight() = Flight(
        flightId = 1L,
        externalFlightId = "AI100-DEL-BOM-20240804",
        flightNumber = "AI100",
        srcAirportCode = "DEL",
        destAirportCode = "BOM",
        departureTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
        arrivalTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)),
        sourceAirport = "Delhi",
        destinationAirport = "Mumbai",
        price = BigDecimal("100"),
        currency = "USD",
        totalSeats = 150,
        availableSeats = 100,
        aircraftType = "Boeing 737",
        airlineId = 1L,
        active = true
    )

    private fun createValidAirline() = Airline(
        id = 1L,
        name = "Air India",
        code = "AI",
        country = "India",
        active = true
    )

    private fun createValidBooking() = Booking(
        id = 1L,
        bookingReference = "CRED123456",
        totalPrice = BigDecimal("200"),
        currency = "USD",
        passengerCount = 2,
        bookingStatus = BookingStatus.PENDING,
        paymentStatus = PaymentStatus.PENDING
    )

    private fun createValidFlightBooking() = FlightBooking(
        id = 1L,
        bookingId = 1L,
        flightId = "1",
        totalFlightPrice = BigDecimal("200"),
        currency = "USD",
        passengerCount = 2,
        status = "PENDING"
    )

    private fun createValidFlightPassenger() = FlightPassenger(
        id = 1L,
        flightBookingId = 1L,
        firstName = "John",
        lastName = "Doe",
        dateOfBirth = java.time.LocalDate.of(1990, 1, 1),
        email = "john.doe@example.com",
        phone = "+1234567890",
        individualPrice = BigDecimal("100")
    )

    private fun createValidBookingData() = BookingRepository.BookingData(
        mainBooking = createValidBooking(),
        flightBookings = listOf(createValidFlightBooking()),
        passengers = listOf(createValidFlightPassenger())
    )

    private fun createValidPaymentIntent() = PaymentProvider.PaymentIntent(
        id = "pi_123456",
        clientSecret = "pi_123456_secret",
        status = PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD
    )
}