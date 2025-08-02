package com.credair.flight.search

import com.credair.flight.search.resource.FlightSearchResource
import com.google.inject.Guice
import io.dropwizard.Configuration
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.Application
import com.fasterxml.jackson.annotation.JsonProperty
import org.eclipse.jetty.servlets.CrossOriginFilter
import java.util.EnumSet
import javax.servlet.DispatcherType
import javax.servlet.FilterRegistration

class FlightSearchApplication : Application<FlightSearchConfiguration>() {

    override fun getName(): String = "flight-search-service"

    override fun initialize(bootstrap: Bootstrap<FlightSearchConfiguration>) {
    }

    override fun run(configuration: FlightSearchConfiguration, environment: Environment) {
        configureCors(environment, configuration.corsConfiguration)
        
        val injector = Guice.createInjector(FlightSearchModule())
        resources().forEach { resource ->
            environment.jersey()
                .register(injector.getInstance(resource))
        }
    }
    
    private fun configureCors(environment: Environment, corsConfig: CorsConfiguration) {
        val filter: FilterRegistration.Dynamic = environment.servlets()
            .addFilter("CORS", CrossOriginFilter::class.java)
            
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType::class.java), true, "/*")
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, corsConfig.allowedOrigins.joinToString(","))
        filter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, corsConfig.allowedHeaders.joinToString(","))
        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, corsConfig.allowedMethods.joinToString(","))
        filter.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, corsConfig.allowCredentials.toString())
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

class FlightSearchConfiguration : Configuration() {
    @JsonProperty("cors")
    val corsConfiguration = CorsConfiguration()
}

class CorsConfiguration {
    @JsonProperty("allowedOrigins")
    val allowedOrigins: List<String> = listOf(
        "http://localhost:5173",
        "https://localhost:5173"
    )
    
    @JsonProperty("allowedMethods")
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
    
    @JsonProperty("allowedHeaders")
    val allowedHeaders: List<String> = listOf("*")
    
    @JsonProperty("allowCredentials")
    val allowCredentials: Boolean = true
}