package com.credair.airline.resource

import com.credair.core.manager.FlightManager
import com.credair.core.model.Flight
import jakarta.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/flights")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class FlightResource @Inject constructor(private val flightManager: FlightManager) {

    @POST
    fun createOrUpdateFlight(flight: Flight): Response {
        return try {
            val result = flightManager.createOrUpdateFlight(flight)
            val statusCode = if (flight.flightId > 0) Response.Status.OK else Response.Status.CREATED
            Response.status(statusCode)
                .entity(result)
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}