package com.credair.core.util

import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.security.MessageDigest
import java.time.Instant

// Enum for ID prefixes, keeping it separate for clarity
enum class IdType(val prefix: String) {
    BOOKING("BKG")
}

object SnowflakeIdGenerator {

    private val logger = LoggerFactory.getLogger(SnowflakeIdGenerator::class.java)

    // --- Configuration ---
    private const val WORKER_ID_BITS = 10L
    private const val SEQUENCE_BITS = 12L
    private val EPOCH = Instant.parse("2024-01-01T00:00:00Z").toEpochMilli()
    private const val MAX_SEQUENCE = -1L xor (-1L shl SEQUENCE_BITS.toInt())

    // --- Bit Shifts & State ---
    private const val WORKER_ID_SHIFT = SEQUENCE_BITS
    private const val TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS

    @Volatile private var lastTimestamp = -1L
    @Volatile private var sequence = 0L

    private val workerId: Long by lazy {
        try {
            val host = InetAddress.getLocalHost().canonicalHostName
            val hashBytes = MessageDigest.getInstance("MD5").digest(host.toByteArray())
            val maxWorkerId = -1L xor (-1L shl WORKER_ID_BITS.toInt())
            val id = ((hashBytes[0].toLong() and 0xFF) or ((hashBytes[1].toLong() and 0xFF) shl 8)) and maxWorkerId
            logger.info("Initialized Snowflake worker ID: {} for host: {}", id, host)
            id
        } catch (e: Exception) {
            val fallbackId = (System.currentTimeMillis() % 1024).toLong()
            logger.warn("Failed to determine host ID, using fallback: {}", fallbackId, e)
            fallbackId
        }
    }

    // --- Public Generation Function ---
    fun generate(type: IdType): String {
        try {
            val id = "${type.prefix}-${generateNumericId()}"
            logger.debug("Generated ID: {} for type: {}", id, type.prefix)
            return id
        } catch (e: Exception) {
            logger.error("Failed to generate ID for type: {}", type.prefix, e)
            throw RuntimeException("ID generation failed: ${e.message}", e)
        }
    }

    // --- Core Logic ---
    private fun generateNumericId(): Long = synchronized(this) {
        var timestamp = Instant.now().toEpochMilli()

        if (timestamp < lastTimestamp) {
            logger.error("Clock moved backward. Last: {}, Current: {}", lastTimestamp, timestamp)
            throw IllegalStateException("Clock moved backward. Last: $lastTimestamp, Current: $timestamp")
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) {
                logger.debug("Sequence exhausted, waiting for next millisecond")
                timestamp = waitForNextMillis(lastTimestamp)
            }
        } else {
            sequence = 0L
        }

        lastTimestamp = timestamp

        ((timestamp - EPOCH) shl TIMESTAMP_SHIFT.toInt()) or
                (workerId shl WORKER_ID_SHIFT.toInt()) or
                sequence
    }

    private fun waitForNextMillis(lastTimestamp: Long): Long {
        var timestamp = Instant.now().toEpochMilli()
        while (timestamp <= lastTimestamp) {
            timestamp = Instant.now().toEpochMilli()
        }
        return timestamp
    }
}