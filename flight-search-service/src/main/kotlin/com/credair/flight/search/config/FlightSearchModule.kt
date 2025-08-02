package com.credair.flight.search.config

import com.credair.core.config.CredAirCoreModule
import com.google.inject.AbstractModule

class FlightSearchModule : AbstractModule() {

    override fun configure() {
        install(CredAirCoreModule())
    }
}