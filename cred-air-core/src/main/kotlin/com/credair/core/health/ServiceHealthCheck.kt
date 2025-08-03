package com.credair.core.health

import com.codahale.metrics.health.HealthCheck
import org.slf4j.LoggerFactory
import jakarta.inject.Singleton

@Singleton
class ServiceHealthCheck(
    private val serviceName: String
) : HealthCheck() {

    companion object {
        private val logger = LoggerFactory.getLogger(ServiceHealthCheck::class.java)
    }

    override fun check(): Result {
        return try {
            logger.debug("Service health check for {} passed", serviceName)
            Result.healthy("$serviceName is running")
        } catch (e: Exception) {
            logger.error("Service health check for {} failed", serviceName, e)
            Result.unhealthy("$serviceName health check failed: ${e.message}")
        }
    }
}