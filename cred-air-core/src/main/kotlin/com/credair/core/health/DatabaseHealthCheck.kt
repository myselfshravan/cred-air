package com.credair.core.health

import com.codahale.metrics.health.HealthCheck
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DatabaseHealthCheck @Inject constructor(
    private val jdbi: Jdbi
) : HealthCheck() {

    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseHealthCheck::class.java)
    }

    override fun check(): Result {
        return try {
            jdbi.withHandle<Boolean, Exception> { handle ->
                handle.execute("SELECT 1")
                true
            }
            logger.debug("Database health check passed")
            Result.healthy("Database connection is healthy")
        } catch (e: Exception) {
            logger.error("Database health check failed", e)
            Result.unhealthy("Database connection failed: ${e.message}")
        }
    }
}