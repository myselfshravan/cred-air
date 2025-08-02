package com.credair.core.dao.interfaces

interface MaterializedViewDao {
    
    fun deleteJourneysContainingFlight(flightId: Long): Int
    
    fun updateSeatsForJourneys(flightId: Long, newAvailableSeats: Int): Int
    
    fun recomputeDirectFlights(flightId: Long): Int
    
    fun recomputeOneStopFlightsWithFlightAsFirst(flightId: Long): Int
    
    fun recomputeOneStopFlightsWithFlightAsSecond(flightId: Long): Int
    
    fun recomputeTwoStopFlightsContainingFlight(flightId: Long): Int
    
    fun refreshFullMaterializedView()
}