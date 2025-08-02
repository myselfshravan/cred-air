package com.credair.flight.search.resource

import com.credair.core.manager.FlightSearchManager
import com.credair.core.manager.FlightSearchManager.*
import com.google.inject.Inject
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class FlightSearchResource @Inject constructor(private val flightSearchManager: FlightSearchManager) {

    @GET
    @Path("/flights")
    fun searchFlights(
        @QueryParam("from") sourceAirport: String,
        @QueryParam("to") destinationAirport: String,
        @QueryParam("date") departureDate: String?,
        @QueryParam("maxPrice") maxPrice: BigDecimal?,
        @QueryParam("minPrice") minPrice: BigDecimal?,
        @QueryParam("airlineId") airlineId: String?,
        @QueryParam("minSeats") minSeats: Int?,
        @QueryParam("sortBy") sortBy: SortBy?,
        @QueryParam("sortOrder") sortOrder: SortOrder?
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
                sortBy = sortBy ?: SortBy.DURATION,
                sortOrder = sortOrder ?: SortOrder.ASC
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
}