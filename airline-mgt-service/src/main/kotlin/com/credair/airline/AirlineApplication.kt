package com.credair.airline

import com.credair.airline.config.AirlineModule
import com.credair.airline.resource.AirlineResource
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
        resources().forEach { resource ->
            environment.jersey()
                .register(injector.getInstance(resource))
        }
    }

    private fun resources(): List<Class<*>> {
        return listOf(
            AirlineResource::class.java
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            AirlineApplication().run(*args)
        }
    }
}

class AirlineConfiguration : Configuration()