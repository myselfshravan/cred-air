package com.credair.booking

import com.credair.booking.config.BookingModule
import com.credair.booking.resource.BookingResource
import com.credair.core.util.DatabaseConfig
import com.google.inject.Guice
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.Application

class BookingApplication : Application<BookingConfiguration>() {

    override fun getName(): String = "flight-booking-service"

    override fun initialize(bootstrap: Bootstrap<BookingConfiguration>) {
    }

    override fun run(configuration: BookingConfiguration, environment: Environment) {
        val injector = Guice.createInjector(BookingModule())
        val bookingResource = injector.getInstance(BookingResource::class.java)
        environment.jersey().register(bookingResource)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BookingApplication().run(*args)
        }
    }
}

class BookingConfiguration : Configuration() {
    val database = DatabaseConfiguration()
}

class DatabaseConfiguration : com.credair.core.util.DatabaseConfig {
    override var url: String = "jdbc:h2:mem:booking_db"
    override var driver: String = "org.h2.Driver"
    override var username: String = "sa"
    override var password: String = ""
}