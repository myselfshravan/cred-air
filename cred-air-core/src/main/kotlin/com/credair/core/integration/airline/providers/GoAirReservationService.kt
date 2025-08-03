package com.credair.core.integration.airline.providers

import com.credair.core.integration.airline.AirlineReservationService
import com.credair.core.integration.airline.CancellationRequest
import com.credair.core.integration.airline.CancellationResponse
import com.credair.core.integration.airline.ReservationRequest
import com.credair.core.integration.airline.ReservationResponse
import com.credair.core.integration.airline.SeatAssignment
import com.google.inject.Singleton

@Singleton
class GoAirReservationService : AirlineReservationService {
    
    override suspend fun softReserve(request: ReservationRequest): ReservationResponse {
        return try {
            val confirmationCode = generateConfirmationCode("G8")
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
                error = "Failed to create soft reservation with GoAir: ${e.message}"
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
                error = "Failed to confirm reservation with GoAir: ${e.message}"
            )
        }
    }
    
    override suspend fun cancelReservation(request: CancellationRequest): CancellationResponse {
        return try {
            CancellationResponse(
                success = true,
                refundAmount = "Refund as per Go Club terms",
                cancellationFee = "₹2,500"
            )
        } catch (e: Exception) {
            CancellationResponse(
                success = false,
                error = "Failed to cancel reservation with GoAir: ${e.message}"
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
        return listOf("24A", "24B", "24C", "25A", "25B", "25C", "26A", "26B", "26C")
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
                seatNumber = "2${4 + index}A",
                seatClass = "Economy"
            )
        }
    }
}