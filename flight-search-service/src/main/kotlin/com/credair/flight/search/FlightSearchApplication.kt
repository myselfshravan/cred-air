package com.credair.flight.search

import com.credair.flight.search.resource.FlightSearchResource
import com.google.inject.Guice
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.Application

class FlightSearchApplication : Application<FlightSearchConfiguration>() {

    override fun getName(): String = "flight-search-service"

    override fun initialize(bootstrap: Bootstrap<FlightSearchConfiguration>) {
    }

    override fun run(configuration: FlightSearchConfiguration, environment: Environment) {
        val injector = Guice.createInjector(FlightSearchModule())
        resources().forEach { resource ->
            environment.jersey()
                .register(injector.getInstance(resource))
        }
    }

    private fun resources(): List<Class<*>> {
        return listOf(
            FlightSearchResource::class.java
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            FlightSearchApplication().run(*args)
        }
    }
}

class FlightSearchConfiguration : Configuration()