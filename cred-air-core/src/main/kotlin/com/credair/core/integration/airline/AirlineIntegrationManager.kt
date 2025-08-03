package com.credair.core.integration.airline

import com.credair.core.integration.airline.providers.IndigoReservationService
import com.credair.core.integration.airline.providers.AirIndiaReservationService
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class AirlineIntegrationManager @Inject constructor(
    private val indigoService: IndigoReservationService,
    private val airIndiaService: AirIndiaReservationService,
) {
    
    private val airlineProviders = mapOf(
        "6E" to indigoService,  // IndiGo
        "AI" to airIndiaService  // Air India
    )
    
    fun getReservationService(airlineCode: String): AirlineReservationService? {
        return airlineProviders[airlineCode.uppercase()]
    }
    
    fun getSupportedAirlines(): List<String> {
        return airlineProviders.keys.toList()
    }
    
    fun getAllProviders(): Map<String, AirlineReservationService> {
        return airlineProviders
    }
}