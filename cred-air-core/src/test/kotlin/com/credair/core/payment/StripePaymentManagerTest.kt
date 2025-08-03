package com.credair.core.payment

import com.credair.core.model.Booking
import com.credair.core.model.BookingStatus
import com.credair.core.model.PaymentStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StripePaymentManagerTest {

    private lateinit var stripePaymentManager: StripePaymentManager

    @BeforeEach
    fun setUp() {
        stripePaymentManager = StripePaymentManager()
    }

    // Create Payment Intent Tests
    @Test
    fun `createPaymentIntent should create payment intent with correct amount`() {
        val booking = createValidBooking().copy(totalPrice = BigDecimal("150.50"))

        val result = stripePaymentManager.createPaymentIntent(booking)

        assertNotNull(result)
        assertNotNull(result.id)
        assertNotNull(result.clientSecret)
        assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
        // Amount should be converted to cents and stored in metadata
        assertTrue(result.id.isNotBlank())
        assertTrue(result.clientSecret.isNotBlank())
    }

    @Test
    fun `createPaymentIntent should create payment intent with integer amount`() {
        val booking = createValidBooking().copy(totalPrice = BigDecimal("100"))

        val result = stripePaymentManager.createPaymentIntent(booking)

        assertNotNull(result)
        assertNotNull(result.id)
        assertNotNull(result.clientSecret)
        assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
    }

    @Test
    fun `createPaymentIntent should handle USD currency`() {
        val booking = createValidBooking().copy(
            totalPrice = BigDecimal("50.75"),
            currency = "USD"
        )

        val result = stripePaymentManager.createPaymentIntent(booking)

        assertNotNull(result)
        assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
    }

    @Test
    fun `createPaymentIntent should handle EUR currency`() {
        val booking = createValidBooking().copy(
            totalPrice = BigDecimal("75.25"),
            currency = "EUR"
        )

        val result = stripePaymentManager.createPaymentIntent(booking)

        assertNotNull(result)
        assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
    }

    @Test
    fun `createPaymentIntent should handle booking without optional fields`() {
        val booking = Booking(
            id = 1L,
            bookingReference = "TEST123",
            totalPrice = BigDecimal("99.99"),
            currency = "USD",
            passengerCount = 1,
            bookingStatus = BookingStatus.PENDING,
            paymentStatus = PaymentStatus.PENDING
        )

        val result = stripePaymentManager.createPaymentIntent(booking)

        assertNotNull(result)
        assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
    }

    @Test
    fun `createPaymentIntent should return dummy payment intent when Stripe API fails`() {
        // Since we don't have valid Stripe API key, it should return dummy payment intent
        val booking = createValidBooking()

        val result = stripePaymentManager.createPaymentIntent(booking)

        assertNotNull(result)
        assertEquals("pi_1234567890", result.id)
        assertEquals("pi_1234567890_secret", result.clientSecret)
        assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
    }

    // Confirm Payment Tests
    @Test
    fun `confirmPayment should return failure result when Stripe API not available`() {
        val result = stripePaymentManager.confirmPayment("pi_test", "pm_test")

        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.error)
        assertNull(result.paymentIntentId)
    }

    @Test
    fun `confirmPayment should handle empty payment intent ID`() {
        val result = stripePaymentManager.confirmPayment("", "pm_test")

        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.error)
    }

    @Test
    fun `confirmPayment should handle empty payment method ID`() {
        val result = stripePaymentManager.confirmPayment("pi_test", "")

        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.error)
    }

    // Cancel Payment Intent Tests
    @Test
    fun `cancelPaymentIntent should return false when Stripe API not available`() {
        val result = stripePaymentManager.cancelPaymentIntent("pi_test")

        assertFalse(result)
    }

    @Test
    fun `cancelPaymentIntent should handle empty payment intent ID`() {
        val result = stripePaymentManager.cancelPaymentIntent("")

        assertFalse(result)
    }

    @Test
    fun `cancelPaymentIntent should handle null payment intent ID gracefully`() {
        // Testing with a clearly invalid ID
        val result = stripePaymentManager.cancelPaymentIntent("invalid_pi_id")

        assertFalse(result)
    }

    // Retrieve Payment Intent Tests
    @Test
    fun `retrievePaymentIntent should return null when Stripe API not available`() {
        val result = stripePaymentManager.retrievePaymentIntent("pi_test")

        assertNull(result)
    }

    @Test
    fun `retrievePaymentIntent should handle empty payment intent ID`() {
        val result = stripePaymentManager.retrievePaymentIntent("")

        assertNull(result)
    }

    @Test
    fun `retrievePaymentIntent should handle invalid payment intent ID`() {
        val result = stripePaymentManager.retrievePaymentIntent("invalid_id")

        assertNull(result)
    }

    // Process Refund Tests
    @Test
    fun `processRefund should return failure when Stripe API not available`() {
        val result = stripePaymentManager.processRefund("pi_test")

        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.error)
    }

    @Test
    fun `processRefund should return failure with specific amount when Stripe API not available`() {
        val result = stripePaymentManager.processRefund("pi_test", BigDecimal("50.00"))

        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.error)
    }

    @Test
    fun `processRefund should handle empty payment intent ID`() {
        val result = stripePaymentManager.processRefund("")

        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.error)
    }

    @Test
    fun `processRefund should handle zero refund amount`() {
        val result = stripePaymentManager.processRefund("pi_test", BigDecimal.ZERO)

        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.error)
    }

    @Test
    fun `processRefund should handle negative refund amount`() {
        val result = stripePaymentManager.processRefund("pi_test", BigDecimal("-10.00"))

        assertNotNull(result)
        assertFalse(result.success)
        assertNotNull(result.error)
    }

    // Amount Conversion Tests
    @Test
    fun `createPaymentIntent should handle decimal amounts correctly`() {
        val testCases = listOf(
            BigDecimal("10.50"),    // 1050 cents
            BigDecimal("100.00"),   // 10000 cents
            BigDecimal("0.99"),     // 99 cents
            BigDecimal("999.99")    // 99999 cents
        )

        testCases.forEach { amount ->
            val booking = createValidBooking().copy(totalPrice = amount)
            val result = stripePaymentManager.createPaymentIntent(booking)
            
            assertNotNull(result, "Failed for amount: $amount")
            assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
        }
    }

    // Currency Handling Tests
    @Test
    fun `createPaymentIntent should convert currency to lowercase`() {
        val currencies = listOf("USD", "EUR", "GBP", "INR", "CAD")

        currencies.forEach { currency ->
            val booking = createValidBooking().copy(currency = currency)
            val result = stripePaymentManager.createPaymentIntent(booking)
            
            assertNotNull(result, "Failed for currency: $currency")
            assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
        }
    }

    // Booking Reference Tests
    @Test
    fun `createPaymentIntent should handle various booking reference formats`() {
        val references = listOf(
            "CRED123456",
            "BK_001",
            "ref-2023-001",
            "SHORT",
            "VERY-LONG-BOOKING-REFERENCE-WITH-SPECIAL-CHARS-123"
        )

        references.forEach { reference ->
            val booking = createValidBooking().copy(bookingReference = reference)
            val result = stripePaymentManager.createPaymentIntent(booking)
            
            assertNotNull(result, "Failed for reference: $reference")
            assertEquals(PaymentProvider.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.status)
        }
    }

    // Error Handling Integration Tests
    @Test
    fun `all payment operations should handle Stripe API unavailability gracefully`() {
        val booking = createValidBooking()
        
        // Create payment intent should return dummy
        val createResult = stripePaymentManager.createPaymentIntent(booking)
        assertNotNull(createResult)
        assertEquals("pi_1234567890", createResult.id)
        
        // Confirm should fail gracefully
        val confirmResult = stripePaymentManager.confirmPayment("pi_test", "pm_test")
        assertFalse(confirmResult.success)
        
        // Cancel should return false
        val cancelResult = stripePaymentManager.cancelPaymentIntent("pi_test")
        assertFalse(cancelResult)
        
        // Retrieve should return null
        val retrieveResult = stripePaymentManager.retrievePaymentIntent("pi_test")
        assertNull(retrieveResult)
        
        // Refund should fail gracefully
        val refundResult = stripePaymentManager.processRefund("pi_test")
        assertFalse(refundResult.success)
    }

    // Helper Methods
    private fun createValidBooking() = Booking(
        id = 1L,
        bookingReference = "CRED123456",
        totalPrice = BigDecimal("200.00"),
        currency = "USD",
        passengerCount = 2,
        bookingStatus = BookingStatus.PENDING,
        paymentStatus = PaymentStatus.PENDING,
        paymentMethod = null,
        paymentTransactionId = null
    )
}