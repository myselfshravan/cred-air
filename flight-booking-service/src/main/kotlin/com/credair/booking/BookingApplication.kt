package com.credair.booking

import com.credair.booking.config.BookingModule
import com.credair.booking.resource.BookingResource
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
        resources().forEach { resource ->
            environment.jersey()
                .register(injector.getInstance(resource))
        }
    }

    private fun resources(): List<Class<*>> {
        return listOf(
            BookingResource::class.java
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BookingApplication().run(*args)
        }
    }
}

class BookingConfiguration : Configuration()