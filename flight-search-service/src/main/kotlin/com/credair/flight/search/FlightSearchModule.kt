package com.credair.flight.search

import com.credair.core.config.CredAirCoreModule
import com.credair.flight.search.config.CacheConfig
import com.google.inject.AbstractModule

class FlightSearchModule : AbstractModule() {

    override fun configure() {
        install(CredAirCoreModule())
        install(CacheConfig())
    }
}