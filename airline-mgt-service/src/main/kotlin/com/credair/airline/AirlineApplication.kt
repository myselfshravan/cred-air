package com.credair.airline

import com.credair.airline.config.AirlineModule
import com.credair.airline.resource.AirlineResource
import com.credair.core.util.DatabaseConfig
import com.google.inject.Guice
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.Application

class AirlineApplication : Application<AirlineConfiguration>() {

    override fun getName(): String = "airline-management-service"

    override fun initialize(bootstrap: Bootstrap<AirlineConfiguration>) {
    }

    override fun run(configuration: AirlineConfiguration, environment: Environment) {
        val injector = Guice.createInjector(AirlineModule())
        val airlineResource = injector.getInstance(AirlineResource::class.java)
        environment.jersey().register(airlineResource)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            AirlineApplication().run(*args)
        }
    }
}

class AirlineConfiguration : Configuration() {
    val database = DatabaseConfiguration()
}

class DatabaseConfiguration : DatabaseConfig {
    override var url: String = "jdbc:h2:mem:airline_db"
    override var driver: String = "org.h2.Driver"
    override var username: String = "sa"
    override var password: String = ""
}