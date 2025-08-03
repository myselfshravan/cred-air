package com.credair.booking

import com.credair.booking.config.BookingModule
import com.credair.booking.resource.BookingResource
import com.credair.core.exception.GlobalExceptionMapper
import com.credair.core.health.DatabaseHealthCheck
import com.credair.core.health.ServiceHealthCheck
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

class BookingApplication : Application<BookingConfiguration>() {

    override fun getName(): String = "flight-booking-service"

    override fun initialize(bootstrap: Bootstrap<BookingConfiguration>) {
    }

    override fun run(configuration: BookingConfiguration, environment: Environment) {
        configureCors(environment, configuration.corsConfiguration)
        
        val injector = Guice.createInjector(BookingModule())
        
        // Register global exception mapper
        environment.jersey().register(GlobalExceptionMapper())
        
        // Register health checks
        environment.healthChecks().register("database", injector.getInstance(DatabaseHealthCheck::class.java))
        environment.healthChecks().register("service", ServiceHealthCheck("flight-booking-service"))
        
        // Register resources
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

class BookingConfiguration : Configuration() {
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