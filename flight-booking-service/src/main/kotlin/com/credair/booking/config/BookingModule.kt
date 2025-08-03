package com.credair.booking.config

import com.credair.core.config.CredAirCoreModule
import com.google.inject.AbstractModule

class BookingModule : AbstractModule() {

    override fun configure() {
        install(CredAirCoreModule())
    }
}