package com.credair.booking.resource

import com.credair.core.manager.BookingManager
import com.credair.core.model.BookingStatus
import com.google.inject.Inject
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import com.stripe.net.Webhook
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import org.slf4j.LoggerFactory

@Path("/webhooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class WebhookResource @Inject constructor(
    private val bookingManager: BookingManager
) {
    
    private val logger = LoggerFactory.getLogger(WebhookResource::class.java)
    private val webhookSecret = System.getenv("STRIPE_WEBHOOK_SECRET") 
        ?: throw IllegalStateException("STRIPE_WEBHOOK_SECRET environment variable is required")
    
    @POST
    @Path("/stripe")
    fun handleStripeWebhook(
        @HeaderParam("Stripe-Signature") signature: String?,
        payload: String
    ): Response {
        return try {
            if (signature == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Missing Stripe signature"))
                    .build()
            }
            
            val event = Webhook.constructEvent(payload, signature, webhookSecret)
            
            when (event.type) {
                "payment_intent.succeeded" -> handlePaymentSucceeded(event)
                "payment_intent.payment_failed" -> handlePaymentFailed(event)
                "payment_intent.canceled" -> handlePaymentCanceled(event)
                else -> {
                    logger.info("Unhandled webhook event type: ${event.type}")
                    Response.ok(mapOf("status" to "ignored")).build()
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing Stripe webhook", e)
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
    
    private fun handlePaymentSucceeded(event: Event): Response {
        val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
        
        return if (paymentIntent != null) {
            try {
                val bookingId = paymentIntent.metadata["booking_id"]?.toLongOrNull()
                
                if (bookingId != null) {
                    val booking = bookingManager.getBookingById(bookingId)
                    
                    if (booking.bookingStatus == BookingStatus.SOFT_RESERVED) {
                        bookingManager.confirmBooking(bookingId)
                        logger.info("Successfully confirmed booking $bookingId via Stripe webhook")
                    }
                    
                    Response.ok(mapOf("status" to "payment_confirmed")).build()
                } else {
                    logger.warn("No booking ID found in payment intent metadata")
                    Response.ok(mapOf("status" to "no_booking_id")).build()
                }
            } catch (e: Exception) {
                logger.error("Error confirming booking from webhook", e)
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(mapOf("error" to e.message))
                    .build()
            }
        } else {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid payment intent"))
                .build()
        }
    }
    
    private fun handlePaymentFailed(event: Event): Response {
        val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
        
        return if (paymentIntent != null) {
            try {
                val bookingId = paymentIntent.metadata["booking_id"]?.toLongOrNull()
                
                if (bookingId != null) {
                    val booking = bookingManager.getBookingById(bookingId)
                    
                    if (booking.bookingStatus == BookingStatus.SOFT_RESERVED) {
                        bookingManager.cancelBooking(bookingId)
                        logger.info("Cancelled booking $bookingId due to payment failure")
                    }
                    
                    Response.ok(mapOf("status" to "booking_cancelled")).build()
                } else {
                    logger.warn("No booking ID found in failed payment intent metadata")
                    Response.ok(mapOf("status" to "no_booking_id")).build()
                }
            } catch (e: Exception) {
                logger.error("Error cancelling booking from webhook", e)
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(mapOf("error" to e.message))
                    .build()
            }
        } else {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid payment intent"))
                .build()
        }
    }
    
    private fun handlePaymentCanceled(event: Event): Response {
        val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
        
        return if (paymentIntent != null) {
            try {
                val bookingId = paymentIntent.metadata["booking_id"]?.toLongOrNull()
                
                if (bookingId != null) {
                    bookingManager.cancelBooking(bookingId)
                    logger.info("Cancelled booking $bookingId due to payment cancellation")
                    
                    Response.ok(mapOf("status" to "booking_cancelled")).build()
                } else {
                    logger.warn("No booking ID found in cancelled payment intent metadata")
                    Response.ok(mapOf("status" to "no_booking_id")).build()
                }
            } catch (e: Exception) {
                logger.error("Error cancelling booking from webhook", e)
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(mapOf("error" to e.message))
                    .build()
            }
        } else {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid payment intent"))
                .build()
        }
    }
}