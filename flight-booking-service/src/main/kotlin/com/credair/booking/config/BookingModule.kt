package com.credair.booking.config

import com.credair.booking.resource.BookingResource
import com.credair.core.config.CredAirCoreModule
import com.google.inject.AbstractModule
import com.google.inject.Singleton

class BookingModule : AbstractModule() {

    override fun configure() {
        install(CredAirCoreModule())
        bind(BookingResource::class.java).`in`(Singleton::class.java)
    }
}