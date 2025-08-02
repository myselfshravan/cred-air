package com.credair.flight.search.resource

import com.credair.core.manager.FlightSearchManager
import com.credair.core.manager.FlightSearchManager.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class FlightSearchResource constructor(private val flightSearchManager: FlightSearchManager) {

    @GET
    @Path("/flights")
    fun searchFlights(
        @QueryParam("from") sourceAirport: String,
        @QueryParam("to") destinationAirport: String,
        @QueryParam("date") departureDate: String?,
        @QueryParam("maxPrice") maxPrice: BigDecimal?,
        @QueryParam("minPrice") minPrice: BigDecimal?,
        @QueryParam("airlineId") airlineId: Long?,
        @QueryParam("minSeats") minSeats: Int?,
        @QueryParam("sortBy") sortBy: String?,
        @QueryParam("sortOrder") sortOrder: String?
    ): Response {
        return try {
            val parsedDate = departureDate?.let { 
                LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
            }
            
            val criteria = SearchCriteria(
                sourceAirport = sourceAirport,
                destinationAirport = destinationAirport,
                departureDate = parsedDate,
                maxPrice = maxPrice,
                minPrice = minPrice,
                airlineId = airlineId,
                minSeats = minSeats ?: 1
            )
            
            val sortCriteria = SortCriteria(
                sortBy = parseSortBy(sortBy),
                sortOrder = parseSortOrder(sortOrder)
            )
            
            val flights = flightSearchManager.searchFlights(criteria, sortCriteria)
            Response.ok(flights).build()
            
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

    @GET
    @Path("/flights/available")
    fun getAvailableFlights(): Response {
        return try {
            val flights = flightSearchManager.getAvailableFlights()
            Response.ok(flights).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/flights/airline/{airlineId}")
    fun getFlightsByAirline(@PathParam("airlineId") airlineId: Long): Response {
        return try {
            val flights = flightSearchManager.getFlightsByAirline(airlineId)
            Response.ok(flights).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/flights/number/{flightNumber}")
    fun getFlightByNumber(@PathParam("flightNumber") flightNumber: String): Response {
        return try {
            val flights = flightSearchManager.getFlightByNumber(flightNumber)
            Response.ok(flights).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/flights/cheapest")
    fun getCheapestFlights(
        @QueryParam("from") sourceAirport: String,
        @QueryParam("to") destinationAirport: String,
        @QueryParam("limit") limit: Int?
    ): Response {
        return try {
            val flights = flightSearchManager.findCheapestFlights(
                sourceAirport, 
                destinationAirport, 
                limit ?: 5
            )
            Response.ok(flights).build()
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

    @GET
    @Path("/flights/fastest")
    fun getFastestFlights(
        @QueryParam("from") sourceAirport: String,
        @QueryParam("to") destinationAirport: String,
        @QueryParam("limit") limit: Int?
    ): Response {
        return try {
            val flights = flightSearchManager.findFastestFlights(
                sourceAirport, 
                destinationAirport, 
                limit ?: 5
            )
            Response.ok(flights).build()
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

    @GET
    @Path("/flights/recommendations")
    fun getFlightRecommendations(
        @QueryParam("from") sourceAirport: String,
        @QueryParam("to") destinationAirport: String
    ): Response {
        return try {
            val recommendations = flightSearchManager.getFlightRecommendations(
                sourceAirport, 
                destinationAirport
            )
            Response.ok(recommendations).build()
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

    private fun parseSortBy(sortBy: String?): SortBy {
        return when (sortBy?.uppercase()) {
            "DEPARTURE_TIME" -> SortBy.DEPARTURE_TIME
            "ARRIVAL_TIME" -> SortBy.ARRIVAL_TIME
            "PRICE" -> SortBy.PRICE
            "DURATION" -> SortBy.DURATION
            "AVAILABLE_SEATS" -> SortBy.AVAILABLE_SEATS
            else -> SortBy.DEPARTURE_TIME
        }
    }

    private fun parseSortOrder(sortOrder: String?): SortOrder {
        return when (sortOrder?.uppercase()) {
            "DESC" -> SortOrder.DESC
            else -> SortOrder.ASC
        }
    }
}