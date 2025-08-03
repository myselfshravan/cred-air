package com.credair.core.integration.airline.providers

import com.credair.core.integration.airline.AirlineReservationService
import com.credair.core.integration.airline.CancellationRequest
import com.credair.core.integration.airline.CancellationResponse
import com.credair.core.integration.airline.ReservationRequest
import com.credair.core.integration.airline.ReservationResponse
import com.credair.core.integration.airline.SeatAssignment
import com.google.inject.Singleton

@Singleton
class SpiceJetReservationService : AirlineReservationService {
    
    override suspend fun softReserve(request: ReservationRequest): ReservationResponse {
        return try {
            val confirmationCode = generateConfirmationCode("SG")
            val pnr = generatePNR()
            
            ReservationResponse(
                success = true,
                airlineConfirmationCode = confirmationCode,
                pnr = pnr,
                seatAssignments = generateSeatAssignments(request.passengers.size)
            )
        } catch (e: Exception) {
            ReservationResponse(
                success = false,
                error = "Failed to create soft reservation with SpiceJet: ${e.message}"
            )
        }
    }
    
    override suspend fun confirmReservation(airlineConfirmationCode: String, pnr: String): ReservationResponse {
        return try {
            ReservationResponse(
                success = true,
                airlineConfirmationCode = airlineConfirmationCode,
                pnr = pnr
            )
        } catch (e: Exception) {
            ReservationResponse(
                success = false,
                error = "Failed to confirm reservation with SpiceJet: ${e.message}"
            )
        }
    }
    
    override suspend fun cancelReservation(request: CancellationRequest): CancellationResponse {
        return try {
            CancellationResponse(
                success = true,
                refundAmount = "Refund applicable as per fare rules",
                cancellationFee = "₹2,000"
            )
        } catch (e: Exception) {
            CancellationResponse(
                success = false,
                error = "Failed to cancel reservation with SpiceJet: ${e.message}"
            )
        }
    }
    
    override suspend fun checkReservationStatus(airlineConfirmationCode: String, pnr: String): ReservationResponse {
        return ReservationResponse(
            success = true,
            airlineConfirmationCode = airlineConfirmationCode,
            pnr = pnr
        )
    }
    
    override suspend fun getAvailableSeats(flightId: String): List<String> {
        return listOf("18A", "18B", "18C", "19A", "19B", "19C", "20A", "20B", "20C")
    }

    private fun generateConfirmationCode(airlinePrefix: String): String {
        val randomSuffix = (1000..9999).random()
        return "$airlinePrefix$randomSuffix"
    }
    
    private fun generatePNR(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
    
    private fun generateSeatAssignments(passengerCount: Int): List<SeatAssignment> {
        return (1..passengerCount).map { index ->
            SeatAssignment(
                passengerName = "Passenger $index",
                seatNumber = "1${8 + index}A",
                seatClass = "Economy"
            )
        }
    }
}