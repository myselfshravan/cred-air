package com.credair.core.integration.airline

import com.credair.core.integration.airline.providers.IndigoReservationService
import com.credair.core.integration.airline.providers.AirIndiaReservationService
import com.credair.core.integration.airline.providers.SpiceJetReservationService
import com.credair.core.integration.airline.providers.VistaraReservationService
import com.credair.core.integration.airline.providers.GoAirReservationService
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class AirlineIntegrationManager @Inject constructor(
    private val indigoService: IndigoReservationService,
    private val airIndiaService: AirIndiaReservationService,
    private val spiceJetService: SpiceJetReservationService,
    private val vistaraService: VistaraReservationService,
    private val goAirService: GoAirReservationService,
) {
    
    private val airlineProviders = mapOf(
        "6E" to indigoService,  // IndiGo
        "AI" to airIndiaService,  // Air India
        "SG" to spiceJetService,  // SpiceJet
        "UK" to vistaraService,  // Vistara
        "G8" to goAirService  // GoAir
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