package com.credair.core.integration.airline

interface AirlineReservationService {
    
    suspend fun softReserve(request: ReservationRequest): ReservationResponse
    
    suspend fun confirmReservation(airlineConfirmationCode: String, pnr: String): ReservationResponse
    
    suspend fun cancelReservation(request: CancellationRequest): CancellationResponse
    
    suspend fun checkReservationStatus(airlineConfirmationCode: String, pnr: String): ReservationResponse
    
    suspend fun getAvailableSeats(flightId: String): List<String>
    
}