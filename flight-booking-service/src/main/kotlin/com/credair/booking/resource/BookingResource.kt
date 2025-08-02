package com.credair.booking.resource

import com.credair.core.manager.BookingManager
import com.credair.core.manager.BookingManager.BookingRequest
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BookingResource constructor(private val bookingManager: BookingManager) {

    @POST
    fun createBooking(request: BookingRequest): Response {
        return try {
            val booking = bookingManager.createBooking(request)
            Response.status(Response.Status.CREATED)
                .entity(booking)
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
    @Path("/{id}")
    fun getBookingById(@PathParam("id") id: Long): Response {
        return try {
            val booking = bookingManager.getBookingById(id)
            Response.ok(booking).build()
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
    @Path("/reference/{reference}")
    fun getBookingByReference(@PathParam("reference") reference: String): Response {
        return try {
            val booking = bookingManager.getBookingByReference(reference)
            Response.ok(booking).build()
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
    @Path("/passenger/{email}")
    fun getBookingsByPassengerEmail(@PathParam("email") email: String): Response {
        return try {
            val bookings = bookingManager.getBookingsByPassengerEmail(email)
            Response.ok(bookings).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    @Path("/flight/{flightId}")
    fun getBookingsByFlightId(@PathParam("flightId") flightId: Long): Response {
        return try {
            val bookings = bookingManager.getBookingsByFlightId(flightId)
            Response.ok(bookings).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @PUT
    @Path("/{id}/confirm")
    fun confirmBooking(@PathParam("id") id: Long): Response {
        return try {
            val booking = bookingManager.confirmBooking(id)
            Response.ok(booking).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
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

    @PUT
    @Path("/{id}/cancel")
    fun cancelBooking(
        @PathParam("id") id: Long,
        request: Map<String, String>?
    ): Response {
        return try {
            val reason = request?.get("reason")
            val booking = bookingManager.cancelBooking(id, reason)
            Response.ok(booking).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
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

    @POST
    @Path("/{id}/payment")
    fun processPayment(
        @PathParam("id") id: Long,
        request: Map<String, String>
    ): Response {
        return try {
            val paymentMethod = request["paymentMethod"]
                ?: throw IllegalArgumentException("Payment method is required")
            
            val booking = bookingManager.processPayment(id, paymentMethod)
            Response.ok(booking).build()
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

    @POST
    @Path("/{id}/checkin")
    fun checkInPassenger(
        @PathParam("id") id: Long,
        request: Map<String, String>?
    ): Response {
        return try {
            val seatNumber = request?.get("seatNumber")
            val booking = bookingManager.checkInPassenger(id, seatNumber)
            Response.ok(booking).build()
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
    @Path("/flight/{flightId}/statistics")
    fun getBookingStatistics(@PathParam("flightId") flightId: Long): Response {
        return try {
            val statistics = bookingManager.getBookingStatistics(flightId)
            Response.ok(statistics).build()
        } catch (e: Exception) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}