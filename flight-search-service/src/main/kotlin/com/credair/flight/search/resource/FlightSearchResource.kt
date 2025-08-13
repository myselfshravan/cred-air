package com.credair.flight.search.resource

import com.credair.flight.search.managers.FlightSearchManager
import com.credair.flight.search.models.request.SearchCriteria
import com.credair.flight.search.models.request.SortBy
import com.credair.flight.search.models.request.SortCriteria
import com.credair.flight.search.models.request.SortOrder
import com.credair.flight.search.models.response.FlightSearchResponse
import com.google.inject.Inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import org.slf4j.LoggerFactory

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class FlightSearchResource @Inject constructor(private val flightSearchManager: FlightSearchManager) {

    private val logger = LoggerFactory.getLogger(FlightSearchResource::class.java)

    @GET
    @Path("/flights")
    fun searchFlights(
        @QueryParam("from") sourceAirportCode: String,
        @QueryParam("to") destinationAirportCode: String,
        @QueryParam("date") departureDate: String?,
        @QueryParam("minSeats") minSeats: Int?,
        @QueryParam("sortBy") sortBy: SortBy?,
        @QueryParam("sortOrder") sortOrder: SortOrder?,
        @QueryParam("page") page: Int?,
        @QueryParam("pageSize") pageSize: Int?
    ): Response {
        return try {
            logger.info(
                "Searching for flights from {} to {} on {}",
                sourceAirportCode,
                destinationAirportCode,
                departureDate
            )
            val parsedDate = departureDate?.let { 
                LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
            }
            
            val currentPage = page ?: 0
            val currentPageSize = pageSize ?: 10
            
            val criteria = SearchCriteria(
                sourceAirport = sourceAirportCode,
                destinationAirport = destinationAirportCode,
                departureDate = parsedDate,
                noOfSeats = minSeats ?: 1
            )
            
            val sortCriteria = SortCriteria(
                sortBy = sortBy ?: SortBy.DURATION,
                sortOrder = sortOrder ?: SortOrder.ASC
            )
            
            val results = flightSearchManager.searchFlights(criteria, sortCriteria, currentPage, currentPageSize)
            val nextStartIndex = (currentPage + 1) * currentPageSize
            val hasMore = results.size == currentPageSize
            
            val response = FlightSearchResponse(
                results = results,
                nextStartIndex = nextStartIndex,
                hasMore = hasMore,
                pageSize = currentPageSize
            )
            Response.ok(response).build()
            
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

    @POST
    @Path("/getDetails")
    fun getDetails(flightIds: List<Long>): Response {
        return try {
            val flightJourney = flightSearchManager.getFlightJourney(flightIds)
            if (flightJourney != null) {
                Response.ok(flightJourney).build()
            } else {
                Response.status(Response.Status.NOT_FOUND).build()
            }
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}