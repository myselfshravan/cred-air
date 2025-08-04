package com.credair.core.integration.airline

import com.credair.core.integration.airline.providers.*
import com.credair.core.model.Booking
import com.credair.core.model.BookingStatus
import com.credair.core.model.Flight
import com.credair.core.model.PaymentStatus
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AirlineReservationServiceTest {

    // Air India Tests
    @Test
    fun `AirIndiaReservationService softReserve should return successful response`() = runBlocking {
        val service = AirIndiaReservationService()
        val request = createValidReservationRequest()

        val response = service.softReserve(request)

        assertTrue(response.success)
        assertNotNull(response.airlineConfirmationCode)
        assertNotNull(response.pnr)
        assertNotNull(response.seatAssignments)
        assertNull(response.error)
        assertTrue(response.airlineConfirmationCode!!.startsWith("DL"))
        assertEquals(2, response.seatAssignments!!.size)
    }

    @Test
    fun `AirIndiaReservationService confirmReservation should return successful response`() = runBlocking {
        val service = AirIndiaReservationService()
        val confirmationCode = "DL1234"
        val pnr = "ABC123"

        val response = service.confirmReservation(confirmationCode, pnr)

        assertTrue(response.success)
        assertEquals(confirmationCode, response.airlineConfirmationCode)
        assertEquals(pnr, response.pnr)
        assertNull(response.error)
    }

    @Test
    fun `AirIndiaReservationService cancelReservation should return successful response`() = runBlocking {
        val service = AirIndiaReservationService()
        val request = CancellationRequest("DL1234", "ABC123", "Change of plans")

        val response = service.cancelReservation(request)

        assertTrue(response.success)
        assertNotNull(response.refundAmount)
        assertNotNull(response.cancellationFee)
        assertNull(response.error)
        assertEquals("$0.00", response.cancellationFee)
    }

    @Test
    fun `AirIndiaReservationService checkReservationStatus should return successful response`() = runBlocking {
        val service = AirIndiaReservationService()
        val confirmationCode = "DL1234"
        val pnr = "ABC123"

        val response = service.checkReservationStatus(confirmationCode, pnr)

        assertTrue(response.success)
        assertEquals(confirmationCode, response.airlineConfirmationCode)
        assertEquals(pnr, response.pnr)
    }

    @Test
    fun `AirIndiaReservationService getAvailableSeats should return seat list`() = runBlocking {
        val service = AirIndiaReservationService()
        val flightId = "AI100"

        val seats = service.getAvailableSeats(flightId)

        assertTrue(seats.isNotEmpty())
        assertTrue(seats.contains("15A"))
        assertTrue(seats.contains("16B"))
        assertTrue(seats.contains("17C"))
    }

    // Indigo Tests
    @Test
    fun `IndigoReservationService softReserve should return successful response`() = runBlocking {
        val service = IndigoReservationService()
        val request = createValidReservationRequest()

        val response = service.softReserve(request)

        assertTrue(response.success)
        assertNotNull(response.airlineConfirmationCode)
        assertNotNull(response.pnr)
        assertNotNull(response.seatAssignments)
        assertNull(response.error)
        assertTrue(response.airlineConfirmationCode!!.startsWith("AA"))
        assertEquals(2, response.seatAssignments!!.size)
    }

    @Test
    fun `IndigoReservationService confirmReservation should return successful response`() = runBlocking {
        val service = IndigoReservationService()
        val confirmationCode = "6E1234"
        val pnr = "XYZ789"

        val response = service.confirmReservation(confirmationCode, pnr)

        assertTrue(response.success)
        assertEquals(confirmationCode, response.airlineConfirmationCode)
        assertEquals(pnr, response.pnr)
        assertNull(response.error)
    }

    @Test
    fun `IndigoReservationService cancelReservation should return successful response`() = runBlocking {
        val service = IndigoReservationService()
        val request = CancellationRequest("6E1234", "XYZ789", "Emergency")

        val response = service.cancelReservation(request)

        assertTrue(response.success)
        assertNotNull(response.refundAmount)
        assertNotNull(response.cancellationFee)
        assertNull(response.error)
    }

    @Test
    fun `IndigoReservationService getAvailableSeats should return seat list`() = runBlocking {
        val service = IndigoReservationService()
        val flightId = "6E123"

        val seats = service.getAvailableSeats(flightId)

        assertTrue(seats.isNotEmpty())
        assertEquals(9, seats.size)
    }

    // SpiceJet Tests
    @Test
    fun `SpiceJetReservationService softReserve should return successful response`() = runBlocking {
        val service = SpiceJetReservationService()
        val request = createValidReservationRequest()

        val response = service.softReserve(request)

        assertTrue(response.success)
        assertNotNull(response.airlineConfirmationCode)
        assertNotNull(response.pnr)
        assertNotNull(response.seatAssignments)
        assertNull(response.error)
        assertTrue(response.airlineConfirmationCode!!.startsWith("SG"))
        assertEquals(2, response.seatAssignments!!.size)
    }

    @Test
    fun `SpiceJetReservationService confirmReservation should return successful response`() = runBlocking {
        val service = SpiceJetReservationService()
        val confirmationCode = "SG1234"
        val pnr = "PQR456"

        val response = service.confirmReservation(confirmationCode, pnr)

        assertTrue(response.success)
        assertEquals(confirmationCode, response.airlineConfirmationCode)
        assertEquals(pnr, response.pnr)
        assertNull(response.error)
    }

    @Test
    fun `SpiceJetReservationService cancelReservation should return successful response`() = runBlocking {
        val service = SpiceJetReservationService()
        val request = CancellationRequest("SG1234", "PQR456")

        val response = service.cancelReservation(request)

        assertTrue(response.success)
        assertNotNull(response.refundAmount)
        assertNotNull(response.cancellationFee)
        assertNull(response.error)
    }

    @Test
    fun `SpiceJetReservationService getAvailableSeats should return seat list`() = runBlocking {
        val service = SpiceJetReservationService()
        val flightId = "SG123"

        val seats = service.getAvailableSeats(flightId)

        assertTrue(seats.isNotEmpty())
        assertEquals(9, seats.size)
    }

    // Vistara Tests
    @Test
    fun `VistaraReservationService softReserve should return successful response`() = runBlocking {
        val service = VistaraReservationService()
        val request = createValidReservationRequest()

        val response = service.softReserve(request)

        assertTrue(response.success)
        assertNotNull(response.airlineConfirmationCode)
        assertNotNull(response.pnr)
        assertNotNull(response.seatAssignments)
        assertNull(response.error)
        assertTrue(response.airlineConfirmationCode!!.startsWith("UK"))
        assertEquals(2, response.seatAssignments!!.size)
    }

    @Test
    fun `VistaraReservationService confirmReservation should return successful response`() = runBlocking {
        val service = VistaraReservationService()
        val confirmationCode = "UK1234"
        val pnr = "LMN789"

        val response = service.confirmReservation(confirmationCode, pnr)

        assertTrue(response.success)
        assertEquals(confirmationCode, response.airlineConfirmationCode)
        assertEquals(pnr, response.pnr)
        assertNull(response.error)
    }

    @Test
    fun `VistaraReservationService cancelReservation should return successful response`() = runBlocking {
        val service = VistaraReservationService()
        val request = CancellationRequest("UK1234", "LMN789", "Flight rescheduled")

        val response = service.cancelReservation(request)

        assertTrue(response.success)
        assertNotNull(response.refundAmount)
        assertNotNull(response.cancellationFee)
        assertNull(response.error)
    }

    @Test
    fun `VistaraReservationService getAvailableSeats should return seat list`() = runBlocking {
        val service = VistaraReservationService()
        val flightId = "UK123"

        val seats = service.getAvailableSeats(flightId)

        assertTrue(seats.isNotEmpty())
        assertEquals(9, seats.size)
    }

    // GoAir Tests
    @Test
    fun `GoAirReservationService softReserve should return successful response`() = runBlocking {
        val service = GoAirReservationService()
        val request = createValidReservationRequest()

        val response = service.softReserve(request)

        assertTrue(response.success)
        assertNotNull(response.airlineConfirmationCode)
        assertNotNull(response.pnr)
        assertNotNull(response.seatAssignments)
        assertNull(response.error)
        assertTrue(response.airlineConfirmationCode!!.startsWith("G8"))
        assertEquals(2, response.seatAssignments!!.size)
    }

    @Test
    fun `GoAirReservationService confirmReservation should return successful response`() = runBlocking {
        val service = GoAirReservationService()
        val confirmationCode = "G81234"
        val pnr = "RST123"

        val response = service.confirmReservation(confirmationCode, pnr)

        assertTrue(response.success)
        assertEquals(confirmationCode, response.airlineConfirmationCode)
        assertEquals(pnr, response.pnr)
        assertNull(response.error)
    }

    @Test
    fun `GoAirReservationService cancelReservation should return successful response`() = runBlocking {
        val service = GoAirReservationService()
        val request = CancellationRequest("G81234", "RST123", "Medical emergency")

        val response = service.cancelReservation(request)

        assertTrue(response.success)
        assertNotNull(response.refundAmount)
        assertNotNull(response.cancellationFee)
        assertNull(response.error)
    }

    @Test
    fun `GoAirReservationService getAvailableSeats should return seat list`() = runBlocking {
        val service = GoAirReservationService()
        val flightId = "G8123"

        val seats = service.getAvailableSeats(flightId)

        assertTrue(seats.isNotEmpty())
        assertEquals(9, seats.size)
    }

    // Cross-Provider Tests
    @Test
    fun `all providers should generate unique confirmation codes`() = runBlocking {
        val services = listOf(
            AirIndiaReservationService(),
            IndigoReservationService(),
            SpiceJetReservationService(),
            VistaraReservationService(),
            GoAirReservationService()
        )
        val request = createValidReservationRequest()
        val confirmationCodes = mutableSetOf<String>()

        services.forEach { service ->
            val response = service.softReserve(request)
            assertTrue(response.success)
            assertNotNull(response.airlineConfirmationCode)
            confirmationCodes.add(response.airlineConfirmationCode!!)
        }

        // All codes should be unique (very high probability)
        assertEquals(services.size, confirmationCodes.size)
    }

    @Test
    fun `all providers should generate PNRs of expected length`() = runBlocking {
        val services = listOf(
            AirIndiaReservationService(),
            IndigoReservationService(),
            SpiceJetReservationService(),
            VistaraReservationService(),
            GoAirReservationService()
        )
        val request = createValidReservationRequest()

        services.forEach { service ->
            val response = service.softReserve(request)
            assertTrue(response.success)
            assertNotNull(response.pnr)
            assertEquals(6, response.pnr!!.length)
        }
    }

    @Test
    fun `all providers should generate seat assignments for all passengers`() = runBlocking {
        val services = listOf(
            AirIndiaReservationService(),
            IndigoReservationService(),
            SpiceJetReservationService(),
            VistaraReservationService(),
            GoAirReservationService()
        )
        val request = createValidReservationRequest()

        services.forEach { service ->
            val response = service.softReserve(request)
            assertTrue(response.success)
            assertNotNull(response.seatAssignments)
            assertEquals(request.passengers.size, response.seatAssignments!!.size)
            
            response.seatAssignments!!.forEach { assignment ->
                assertTrue(assignment.passengerName.isNotBlank())
                assertTrue(assignment.seatNumber.isNotBlank())
                assertTrue(assignment.seatClass.isNotBlank())
            }
        }
    }

    // Error Handling Tests
    @Test
    fun `all providers should handle empty passenger list gracefully`() = runBlocking {
        val services = listOf(
            AirIndiaReservationService(),
            IndigoReservationService(),
            SpiceJetReservationService(),
            VistaraReservationService(),
            GoAirReservationService()
        )
        val request = createValidReservationRequest().copy(passengers = emptyList())

        services.forEach { service ->
            val response = service.softReserve(request)
            // Should still succeed but with empty seat assignments
            assertTrue(response.success)
            assertNotNull(response.seatAssignments)
            assertTrue(response.seatAssignments!!.isEmpty())
        }
    }

    @Test
    fun `all providers should handle checkReservationStatus correctly`() = runBlocking {
        val services = listOf(
            AirIndiaReservationService(),
            IndigoReservationService(),
            SpiceJetReservationService(),
            VistaraReservationService(),
            GoAirReservationService()
        )

        services.forEach { service ->
            val response = service.checkReservationStatus("TEST123", "ABC123")
            assertTrue(response.success)
            assertEquals("TEST123", response.airlineConfirmationCode)
            assertEquals("ABC123", response.pnr)
        }
    }

    // Provider-Specific Prefix Tests
    @Test
    fun `providers should use correct airline prefixes`() = runBlocking {
        val expectedPrefixes = mapOf(
            AirIndiaReservationService() to "DL",
            IndigoReservationService() to "AA",
            SpiceJetReservationService() to "SG",
            VistaraReservationService() to "UK",
            GoAirReservationService() to "G8"
        )
        val request = createValidReservationRequest()

        expectedPrefixes.forEach { (service, expectedPrefix) ->
            val response = service.softReserve(request)
            assertTrue(response.success)
            assertNotNull(response.airlineConfirmationCode)
            assertTrue(response.airlineConfirmationCode!!.startsWith(expectedPrefix))
        }
    }

    // Performance Tests
    @Test
    fun `all providers should complete operations within reasonable time`() = runBlocking {
        val services = listOf(
            AirIndiaReservationService(),
            IndigoReservationService(),
            SpiceJetReservationService(),
            VistaraReservationService(),
            GoAirReservationService()
        )
        val request = createValidReservationRequest()

        services.forEach { service ->
            val startTime = System.currentTimeMillis()
            
            service.softReserve(request)
            service.confirmReservation("TEST123", "ABC123")
            service.checkReservationStatus("TEST123", "ABC123")
            service.getAvailableSeats("TEST123")
            service.cancelReservation(CancellationRequest("TEST123", "ABC123"))
            
            val duration = System.currentTimeMillis() - startTime
            
            // All operations should complete quickly (under 100ms)
            assertTrue(duration < 100, "Operations too slow for ${service::class.simpleName}: ${duration}ms")
        }
    }

    // Helper Methods
    private fun createValidReservationRequest(): ReservationRequest {
        return ReservationRequest(
            booking = createValidBooking(),
            flight = createValidFlight(),
            passengers = listOf(
                PassengerInfo(
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@example.com",
                    phone = "+1234567890",
                    dateOfBirth = "1990-01-01"
                ),
                PassengerInfo(
                    firstName = "Jane",
                    lastName = "Smith",
                    email = "jane.smith@example.com",
                    phone = "+1234567891",
                    dateOfBirth = "1985-06-15"
                )
            )
        )
    }

    private fun createValidBooking(): Booking {
        return Booking(
            id = 1L,
            bookingReference = "CRED123456",
            totalPrice = BigDecimal("200.00"),
            currency = "USD",
            passengerCount = 2,
            bookingStatus = BookingStatus.PENDING,
            paymentStatus = PaymentStatus.PENDING
        )
    }

    private fun createValidFlight(): Flight {
        return Flight(
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
    }
}