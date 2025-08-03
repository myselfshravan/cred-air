package com.credair.booking.resource

import com.credair.core.manager.BookingManager
import com.credair.core.model.BookingRequestPayload
import com.credair.core.util.with
import com.google.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BookingResource @Inject constructor(private val bookingManager: BookingManager) {

    @POST
    @Path("")
    fun createBookingFromFrontend(
        payload: BookingRequestPayload,
        @Suspended asyncResponse: AsyncResponse
    ): AsyncResponse = asyncResponse.with {
        return@with try {
            val bookings = bookingManager.createBookingFromPayload(payload)
            Response.status(Response.Status.CREATED)
                .entity(mapOf(
                    "bookings" to bookings,
                    "message" to "Bookings created successfully with soft reservation",
                ))
                .build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: IllegalStateException) {
            Response.status(Response.Status.CONFLICT)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/{bookingId}/confirmation")
    fun getBookingConfirmation(
        @PathParam("bookingId") bookingId: Long,
        @Suspended asyncResponse: AsyncResponse
    ): AsyncResponse = asyncResponse.with {
        return@with bookingManager.getBookingConfirmation(bookingId)
    }
}