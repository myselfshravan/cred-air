package com.credair.core.payment

import com.credair.core.model.Booking
import java.math.BigDecimal

interface PaymentProvider {
    
    data class PaymentIntent(
        val id: String,
        val clientSecret: String,
        val status: PaymentStatus
    )
    
    data class PaymentResult(
        val success: Boolean,
        val paymentIntentId: String? = null,
        val error: String? = null
    )
    
    enum class PaymentStatus {
        REQUIRES_PAYMENT_METHOD,
        REQUIRES_CONFIRMATION,
        REQUIRES_ACTION,
        PROCESSING,
        SUCCEEDED,
        CANCELED
    }
    
    fun createPaymentIntent(booking: Booking): PaymentIntent
    
    fun confirmPayment(paymentIntentId: String, paymentMethodId: String): PaymentResult
    
    fun cancelPaymentIntent(paymentIntentId: String): Boolean
    
    fun retrievePaymentIntent(paymentIntentId: String): PaymentIntent?
    
    fun processRefund(paymentIntentId: String, amount: BigDecimal? = null): PaymentResult
}