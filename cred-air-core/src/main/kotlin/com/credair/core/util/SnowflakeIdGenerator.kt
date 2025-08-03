package com.credair.core.util

import java.net.InetAddress
import java.security.MessageDigest
import java.time.Instant

// Enum for ID prefixes, keeping it separate for clarity
enum class IdType(val prefix: String) {
    BOOKING("BKG")
}

object SnowflakeIdGenerator {

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
            ((hashBytes[0].toLong() and 0xFF) or ((hashBytes[1].toLong() and 0xFF) shl 8)) and maxWorkerId
        } catch (e: Exception) {
            System.err.println("Failed to determine host ID. Using a random fallback.")
            (System.currentTimeMillis() % 1024).toLong()
        }
    }

    // --- Public Generation Function ---
    fun generate(type: IdType): String {
        return "${type.prefix}-${generateNumericId()}"
    }

    // --- Core Logic ---
    private fun generateNumericId(): Long = synchronized(this) {
        var timestamp = Instant.now().toEpochMilli()

        if (timestamp < lastTimestamp) {
            throw IllegalStateException("Clock moved backward.")
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) timestamp = waitForNextMillis(lastTimestamp)
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