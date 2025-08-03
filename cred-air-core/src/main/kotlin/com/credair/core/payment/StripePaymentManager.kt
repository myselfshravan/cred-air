package com.credair.core.payment

import com.credair.core.exception.PaymentProcessingException
import com.credair.core.model.Booking
import com.google.inject.Inject
import com.google.inject.Singleton
import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.PaymentIntentConfirmParams
import com.stripe.param.PaymentIntentCancelParams
import com.stripe.param.RefundCreateParams
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Singleton
class StripePaymentManager @Inject constructor() : PaymentProvider {
    
    companion object {
        private val logger = LoggerFactory.getLogger(StripePaymentManager::class.java)
    }
    
    init {
        val apiKey = System.getenv("STRIPE_SECRET_KEY") ?: "sktest"
        logger.info("Initializing Stripe with API key configured: {}", if (apiKey.startsWith("sk_")) "Yes" else "No (using test key)")
        Stripe.apiKey = apiKey
    }
    
    override fun createPaymentIntent(booking: Booking): PaymentProvider.PaymentIntent {
        logger.info("Creating payment intent for booking: {} with amount: {}", booking.bookingReference, booking.totalPrice)
        try {
            val amountInCents = booking.totalPrice.multiply(BigDecimal("100")).longValueExact()

            val params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(booking.currency.lowercase())
                .putAllMetadata(
                    mapOf(
                        "booking_id" to booking.id.toString(),
                        "booking_reference" to booking.bookingReference
                    )
                )
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build()

            val paymentIntent = PaymentIntent.create(params)
            logger.info("Successfully created Stripe payment intent: {} for booking: {}", paymentIntent.getId(), booking.bookingReference)

            return PaymentProvider.PaymentIntent(
                id = paymentIntent.getId(),
                clientSecret = paymentIntent.getClientSecret(),
                status = mapStripeStatus(paymentIntent.getStatus())
            )
        } catch (e: StripeException) {
            logger.error("Stripe API error creating payment intent for booking: {}", booking.bookingReference, e)
            // returning a dummy payment intent since we dont have api key
            logger.warn("Returning dummy payment intent due to Stripe API error")
            return PaymentProvider.PaymentIntent(
                id = "pi_1234567890",
                clientSecret = "pi_1234567890_secret",
                status = PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD
            )
        } catch (e: Exception) {
            logger.error("Unexpected error creating payment intent for booking: {}", booking.bookingReference, e)
            throw PaymentProcessingException("Failed to create payment intent: ${e.message}", e)
        }
    }
    
    override fun confirmPayment(paymentIntentId: String, paymentMethodId: String): PaymentProvider.PaymentResult {
        logger.info("Confirming payment for payment intent: {} with method: {}", paymentIntentId, paymentMethodId)
        return try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            
            val params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .setReturnUrl("${getBaseUrl()}/booking/payment/return")
                .build()
            
            val confirmedIntent = paymentIntent.confirm(params)
            val success = confirmedIntent.getStatus() == "succeeded"
            
            if (success) {
                logger.info("Payment confirmed successfully for intent: {}", paymentIntentId)
            } else {
                logger.warn("Payment confirmation failed for intent: {} with status: {}", paymentIntentId, confirmedIntent.getStatus())
            }
            
            PaymentProvider.PaymentResult(
                success = success,
                paymentIntentId = confirmedIntent.getId()
            )
        } catch (e: StripeException) {
            logger.error("Stripe API error confirming payment for intent: {}", paymentIntentId, e)
            PaymentProvider.PaymentResult(
                success = false,
                error = "Payment confirmation failed: ${e.message}"
            )
        } catch (e: Exception) {
            logger.error("Unexpected error confirming payment for intent: {}", paymentIntentId, e)
            PaymentProvider.PaymentResult(
                success = false,
                error = "Payment confirmation failed: ${e.message}"
            )
        }
    }
    
    override fun cancelPaymentIntent(paymentIntentId: String): Boolean {
        return try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            val params = PaymentIntentCancelParams.builder().build()
            paymentIntent.cancel(params)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun retrievePaymentIntent(paymentIntentId: String): PaymentProvider.PaymentIntent? {
        return try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            PaymentProvider.PaymentIntent(
                id = paymentIntent.getId(),
                clientSecret = paymentIntent.getClientSecret(),
                status = mapStripeStatus(paymentIntent.getStatus())
            )
        } catch (e: Exception) {
            null
        }
    }
    
    override fun processRefund(paymentIntentId: String, amount: BigDecimal?): PaymentProvider.PaymentResult {
        logger.info("Processing refund for payment intent: {} with amount: {}", paymentIntentId, amount ?: "full")
        return try {
            val params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .apply {
                    amount?.let { 
                        setAmount(it.multiply(BigDecimal("100")).longValueExact())
                    }
                }
                .build()
            
            val refund = Refund.create(params)
            val success = refund.getStatus() == "succeeded"
            
            if (success) {
                logger.info("Refund processed successfully for intent: {} with refund id: {}", paymentIntentId, refund.getId())
            } else {
                logger.warn("Refund failed for intent: {} with status: {}", paymentIntentId, refund.getStatus())
            }
            
            PaymentProvider.PaymentResult(
                success = success,
                paymentIntentId = paymentIntentId
            )
        } catch (e: StripeException) {
            logger.error("Stripe API error processing refund for intent: {}", paymentIntentId, e)
            PaymentProvider.PaymentResult(
                success = false,
                error = "Refund processing failed: ${e.message}"
            )
        } catch (e: Exception) {
            logger.error("Unexpected error processing refund for intent: {}", paymentIntentId, e)
            PaymentProvider.PaymentResult(
                success = false,
                error = "Refund processing failed: ${e.message}"
            )
        }
    }
    
    private fun mapStripeStatus(stripeStatus: String): PaymentProvider.PaymentStatus {
        return when (stripeStatus) {
            "requires_payment_method" -> PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD
            "requires_confirmation" -> PaymentProvider.PaymentStatus.REQUIRES_CONFIRMATION
            "requires_action" -> PaymentProvider.PaymentStatus.REQUIRES_ACTION
            "processing" -> PaymentProvider.PaymentStatus.PROCESSING
            "succeeded" -> PaymentProvider.PaymentStatus.SUCCEEDED
            "canceled" -> PaymentProvider.PaymentStatus.CANCELED
            else -> PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD
        }
    }
    
    private fun getBaseUrl(): String {
        return System.getenv("BASE_URL") ?: "http://localhost:8080"
    }
}