package com.credair.core.integration.airline

import com.credair.core.model.Booking
import com.credair.core.model.Flight

interface AirlineReservationService {
    
    fun softReserve(request: ReservationRequest): ReservationResponse
    
    fun confirmReservation(airlineConfirmationCode: String, pnr: String): ReservationResponse
    
    fun cancelReservation(request: CancellationRequest): CancellationResponse
    
    fun checkReservationStatus(airlineConfirmationCode: String, pnr: String): ReservationResponse
    
    fun getAvailableSeats(flightId: String): List<String>
    
    fun getSupportedAirline(): String
}