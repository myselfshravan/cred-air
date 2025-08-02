package com.credair.flight.search.config

import com.credair.flight.search.resource.FlightSearchResource
import com.credair.core.config.CredAirCoreModule
import com.google.inject.AbstractModule
import com.google.inject.Singleton

class FlightSearchModule : AbstractModule() {

    override fun configure() {
        install(CredAirCoreModule())
        bind(FlightSearchResource::class.java).`in`(Singleton::class.java)
    }
}