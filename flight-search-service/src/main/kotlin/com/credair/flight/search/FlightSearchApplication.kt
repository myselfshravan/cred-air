package com.credair.flight.search

import com.credair.flight.search.config.FlightSearchModule
import com.credair.flight.search.resource.FlightSearchResource
import com.credair.core.util.DatabaseConfig
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
        val flightSearchResource = injector.getInstance(FlightSearchResource::class.java)
        environment.jersey().register(flightSearchResource)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            FlightSearchApplication().run(*args)
        }
    }
}

class FlightSearchConfiguration : Configuration() {
    val database = DatabaseConfiguration()
}

class DatabaseConfiguration : com.credair.core.util.DatabaseConfig {
    override var url: String = "jdbc:h2:mem:flight_search_db"
    override var driver: String = "org.h2.Driver"
    override var username: String = "sa"
    override var password: String = ""
}