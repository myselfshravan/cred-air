package com.credair.airline.config

import com.credair.airline.resource.AirlineResource
import com.credair.core.config.CredAirCoreModule
import com.google.inject.AbstractModule
import com.google.inject.Singleton

class AirlineModule : AbstractModule() {

    override fun configure() {
        install(CredAirCoreModule())
        bind(AirlineResource::class.java).`in`(Singleton::class.java)
    }
}