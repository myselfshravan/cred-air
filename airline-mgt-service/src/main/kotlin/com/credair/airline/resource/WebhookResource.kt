package com.credair.airline.resource

import com.credair.core.manager.FlightUpdateManager
import com.credair.core.model.Flight
import org.slf4j.LoggerFactory
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/webhooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class WebhookResource(private val flightUpdateManager: FlightUpdateManager) {

    companion object {
        private val logger = LoggerFactory.getLogger(WebhookResource::class.java)
    }

    @POST
    @Path("/flight")
    fun updateFlightWebhook(flightData: Flight): Response {
        logger.info("Received flight update webhook for flight: ${flightData.flightNumber}")
        
        return try {
            // Validate required fields
            if (flightData.flightId == null) {
                logger.warn("Flight update rejected - missing flight ID")
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Flight ID is required"))
                    .build()
            }
            
            // Process the flight update
            val updatedFlight = flightUpdateManager.updateFlight(flightData)
            
            logger.info("Successfully processed flight update webhook for flight ID: ${flightData.flightId}")
            Response.ok(mapOf(
                "status" to "success",
                "message" to "Flight updated successfully",
                "flightId" to updatedFlight.flightId
            )).build()
            
        } catch (e: IllegalArgumentException) {
            logger.warn("Flight update webhook validation failed: ${e.message}")
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
                
        } catch (e: Exception) {
            logger.error("Flight update webhook failed for flight ${flightData.flightId}", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Internal server error"))
                .build()
        }
    }
}