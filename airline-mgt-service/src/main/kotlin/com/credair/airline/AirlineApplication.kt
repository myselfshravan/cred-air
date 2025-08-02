package com.credair.airline

import com.credair.airline.config.AirlineModule
import com.credair.airline.resource.AirlineResource
import com.credair.airline.resource.WebhookResource
import com.google.inject.Guice
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.Application

class AirlineManagementApplication : Application<AirlineConfiguration>() {

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
            AirlineResource::class.java,
            WebhookResource::class.java
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            AirlineManagementApplication().run(*args)
        }
    }
}

class AirlineConfiguration : Configuration()