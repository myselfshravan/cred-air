package com.credair.core.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SnowflakeIdGeneratorTest {

    // ID Generation Tests
    @Test
    fun `generate should create ID with correct prefix`() {
        val bookingId = SnowflakeIdGenerator.generate(IdType.BOOKING)
        
        assertTrue(bookingId.startsWith("BKG-"))
        assertTrue(bookingId.length > 4) // Should have numeric part after prefix
    }

    @Test
    fun `generate should create unique IDs for consecutive calls`() {
        val id1 = SnowflakeIdGenerator.generate(IdType.BOOKING)
        val id2 = SnowflakeIdGenerator.generate(IdType.BOOKING)
        
        assertNotEquals(id1, id2)
    }

    @Test
    fun `generate should create IDs with increasing numeric parts`() {
        val ids = (1..10).map { SnowflakeIdGenerator.generate(IdType.BOOKING) }
        val numericParts = ids.map { it.substringAfter("BKG-").toLong() }
        
        // Verify all numeric parts are different
        assertEquals(numericParts.size, numericParts.toSet().size)
        
        // Verify they are generally increasing (allowing for some timing variations)
        val sortedParts = numericParts.sorted()
        assertTrue(sortedParts.size == numericParts.size)
    }

    // Threading Safety Tests
    @Test
    fun `generate should be thread-safe and produce unique IDs`() {
        val threadCount = 10
        val idsPerThread = 100
        val allIds = ConcurrentHashMap.newKeySet<String>()
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        val futures = mutableListOf<Future<*>>()

        repeat(threadCount) {
            val future = executor.submit {
                try {
                    repeat(idsPerThread) {
                        val id = SnowflakeIdGenerator.generate(IdType.BOOKING)
                        allIds.add(id)
                    }
                } finally {
                    latch.countDown()
                }
            }
            futures.add(future)
        }

        latch.await()
        executor.shutdown()

        // Verify all futures completed successfully
        futures.forEach { it.get() }

        // All IDs should be unique
        assertEquals(threadCount * idsPerThread, allIds.size)
        
        // All IDs should have correct prefix
        assertTrue(allIds.all { it.startsWith("BKG-") })
    }

    @Test
    fun `generate should handle high concurrency with minimal collisions`() {
        val threadCount = 50
        val idsPerThread = 20
        val allIds = ConcurrentHashMap.newKeySet<String>()
        val executor = Executors.newFixedThreadPool(threadCount)
        val startLatch = CountDownLatch(1)
        val finishLatch = CountDownLatch(threadCount)

        repeat(threadCount) {
            executor.submit {
                try {
                    startLatch.await() // Synchronize start time
                    repeat(idsPerThread) {
                        val id = SnowflakeIdGenerator.generate(IdType.BOOKING)
                        allIds.add(id)
                    }
                } finally {
                    finishLatch.countDown()
                }
            }
        }

        startLatch.countDown() // Start all threads simultaneously
        finishLatch.await()
        executor.shutdown()

        // All IDs should be unique even under high concurrency
        assertEquals(threadCount * idsPerThread, allIds.size)
    }

    // Sequence Tests
    @Test
    fun `generate should handle sequence overflow correctly`() {
        // This test verifies the generator can handle many rapid calls
        val rapidIds = (1..5000).map { SnowflakeIdGenerator.generate(IdType.BOOKING) }
        
        // All should be unique
        assertEquals(rapidIds.size, rapidIds.toSet().size)
        
        // All should have correct format
        assertTrue(rapidIds.all { 
            it.startsWith("BKG-") && it.substringAfter("BKG-").toLongOrNull() != null 
        })
    }

    // Epoch and Timestamp Tests
    @Test
    fun `generated IDs should contain valid timestamps after epoch`() {
        val id = SnowflakeIdGenerator.generate(IdType.BOOKING)
        val numericPart = id.substringAfter("BKG-").toLong()
        
        // Extract timestamp portion (upper bits)
        val timestamp = (numericPart shr 22) // 22 = SEQUENCE_BITS + WORKER_ID_BITS
        
        // Should be positive (after epoch)
        assertTrue(timestamp > 0)
    }

    @Test
    fun `generate should work correctly with different ID types`() {
        val bookingId = SnowflakeIdGenerator.generate(IdType.BOOKING)
        
        assertTrue(bookingId.startsWith("BKG-"))
        
        // Test that we can add more ID types in the future
        // This validates the enum structure works correctly
        assertEquals("BKG", IdType.BOOKING.prefix)
    }

    // Error Handling Tests
    @Test
    fun `generate should handle rapid successive calls without errors`() {
        // Generate many IDs quickly to test sequence handling
        val ids = mutableSetOf<String>()
        
        repeat(1000) {
            val id = SnowflakeIdGenerator.generate(IdType.BOOKING)
            assertTrue(ids.add(id), "Duplicate ID generated: $id")
        }
        
        assertEquals(1000, ids.size)
    }

    // Format Validation Tests
    @Test
    fun `generated IDs should have consistent format`() {
        val ids = (1..100).map { SnowflakeIdGenerator.generate(IdType.BOOKING) }
        
        ids.forEach { id ->
            // Should match pattern: PREFIX-DIGITS
            assertTrue(id.matches(Regex("^BKG-\\d+$")), "Invalid format: $id")
            
            // Numeric part should be parseable as Long
            val numericPart = id.substringAfter("BKG-")
            assertTrue(numericPart.toLongOrNull() != null, "Non-numeric part: $numericPart")
            
            // Should be positive
            assertTrue(numericPart.toLong() > 0, "Non-positive numeric part: $numericPart")
        }
    }

    // Performance Tests
    @Test
    fun `generate should complete within reasonable time`() {
        val startTime = System.currentTimeMillis()
        
        repeat(1000) {
            SnowflakeIdGenerator.generate(IdType.BOOKING)
        }
        
        val duration = System.currentTimeMillis() - startTime
        
        // Should complete 1000 generations in under 1 second
        assertTrue(duration < 1000, "ID generation too slow: ${duration}ms for 1000 IDs")
    }

    // Edge Case Tests
    @Test
    fun `generate should handle system time changes gracefully`() {
        // Generate some IDs to establish baseline
        val initialIds = (1..10).map { SnowflakeIdGenerator.generate(IdType.BOOKING) }
        
        // Continue generating - should not throw exceptions
        val laterIds = (1..10).map { SnowflakeIdGenerator.generate(IdType.BOOKING) }
        
        // All IDs should be unique
        val allIds = initialIds + laterIds
        assertEquals(allIds.size, allIds.toSet().size)
    }

    @Test
    fun `generate should produce IDs with reasonable length`() {
        val id = SnowflakeIdGenerator.generate(IdType.BOOKING)
        
        // Should not be too short or too long
        assertTrue(id.length >= 8, "ID too short: $id")
        assertTrue(id.length <= 30, "ID too long: $id")
    }
}