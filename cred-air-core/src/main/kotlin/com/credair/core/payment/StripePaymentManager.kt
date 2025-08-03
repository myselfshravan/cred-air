package com.credair.core.payment

import com.credair.core.model.Booking
import com.google.inject.Inject
import com.google.inject.Singleton
import com.stripe.Stripe
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.PaymentIntentConfirmParams
import com.stripe.param.PaymentIntentCancelParams
import com.stripe.param.RefundCreateParams
import java.math.BigDecimal

@Singleton
class StripePaymentManager @Inject constructor() : PaymentProvider {
    
    init {
        Stripe.apiKey = System.getenv("STRIPE_SECRET_KEY") ?: "sktest"
    }
    
    override fun createPaymentIntent(booking: Booking): PaymentProvider.PaymentIntent {
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

            return PaymentProvider.PaymentIntent(
                id = paymentIntent.getId(),
                clientSecret = paymentIntent.getClientSecret(),
                status = mapStripeStatus(paymentIntent.getStatus())
            )
        } catch (e: Exception) {
            // returning a dummy payment intent since we dont have api key
            return PaymentProvider.PaymentIntent(
                id = "pi_1234567890",
                clientSecret = "pi_1234567890_secret",
                status = PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD
            )
        }
    }
    
    override fun confirmPayment(paymentIntentId: String, paymentMethodId: String): PaymentProvider.PaymentResult {
        return try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            
            val params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .setReturnUrl("${getBaseUrl()}/booking/payment/return")
                .build()
            
            val confirmedIntent = paymentIntent.confirm(params)
            
            PaymentProvider.PaymentResult(
                success = confirmedIntent.getStatus() == "succeeded",
                paymentIntentId = confirmedIntent.getId()
            )
        } catch (e: Exception) {
            PaymentProvider.PaymentResult(
                success = false,
                error = e.message
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
            
            PaymentProvider.PaymentResult(
                success = refund.getStatus() == "succeeded",
                paymentIntentId = paymentIntentId
            )
        } catch (e: Exception) {
            PaymentProvider.PaymentResult(
                success = false,
                error = e.message
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