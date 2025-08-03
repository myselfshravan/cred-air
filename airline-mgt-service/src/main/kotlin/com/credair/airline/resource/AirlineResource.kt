package com.credair.airline.resource

import com.credair.core.manager.AirlineManager
import com.credair.core.model.Airline
import jakarta.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/airlines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AirlineResource @Inject constructor(private val airlineManager: AirlineManager) {

    @GET
    fun getAllAirlines(@QueryParam("active") active: Boolean?): Response {
        return try {
            val airlines = when (active) {
                true -> airlineManager.getActiveAirlines()
                false -> airlineManager.getAllAirlines().filter { !it.active }
                null -> airlineManager.getAllAirlines()
            }
            Response.ok(airlines).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/{id}")
    fun getAirlineById(@PathParam("id") id: Long): Response {
        return try {
            val airline = airlineManager.getAirlineById(id)
            Response.ok(airline).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/code/{code}")
    fun getAirlineByCode(@PathParam("code") code: String): Response {
        return try {
            val airline = airlineManager.getAirlineByCode(code)
            Response.ok(airline).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/country/{country}")
    fun getAirlinesByCountry(@PathParam("country") country: String): Response {
        return try {
            val airlines = airlineManager.getAirlinesByCountry(country)
            Response.ok(airlines).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @POST
    fun createAirline(airline: Airline): Response {
        return try {
            val createdAirline = airlineManager.createAirline(airline)
            Response.status(Response.Status.CREATED)
                .entity(createdAirline)
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

    @PUT
    @Path("/{id}")
    fun updateAirline(@PathParam("id") id: Long, airline: Airline): Response {
        return try {
            val updatedAirline = airlineManager.updateAirline(id, airline)
            Response.ok(updatedAirline).build()
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

    @PUT
    @Path("/{id}/activate")
    fun activateAirline(@PathParam("id") id: Long): Response {
        return try {
            val airline = airlineManager.activateAirline(id)
            Response.ok(airline).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{id}/deactivate")
    fun deactivateAirline(@PathParam("id") id: Long): Response {
        return try {
            val airline = airlineManager.deactivateAirline(id)
            Response.ok(airline).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @DELETE
    @Path("/{id}")
    fun deleteAirline(@PathParam("id") id: Long): Response {
        return try {
            val deleted = airlineManager.deleteAirline(id)
            if (deleted) {
                Response.noContent().build()
            } else {
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(mapOf("error" to "Failed to delete airline"))
                    .build()
            }
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}