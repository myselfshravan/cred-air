package com.credair.core.exception

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class GlobalExceptionMapper : ExceptionMapper<Exception> {

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionMapper::class.java)
    }

    override fun toResponse(exception: Exception): Response {
        val errorId = generateErrorId()
        
        return when (exception) {
            is ResourceNotFoundException -> {
                logger.warn("Resource not found [{}]: {}", errorId, exception.message)
                createErrorResponse(
                    status = Response.Status.NOT_FOUND,
                    errorCode = "RESOURCE_NOT_FOUND",
                    message = exception.message ?: "Resource not found",
                    errorId = errorId
                )
            }
            is ValidationException -> {
                logger.warn("Validation error [{}]: {}", errorId, exception.message)
                createErrorResponse(
                    status = Response.Status.BAD_REQUEST,
                    errorCode = "VALIDATION_ERROR",
                    message = exception.message ?: "Validation failed",
                    errorId = errorId
                )
            }
            is BusinessRuleViolationException -> {
                logger.warn("Business rule violation [{}]: {}", errorId, exception.message)
                createErrorResponse(
                    status = Response.Status.CONFLICT,
                    errorCode = "BUSINESS_RULE_VIOLATION",
                    message = exception.message ?: "Business rule violation",
                    errorId = errorId
                )
            }
            is PaymentProcessingException -> {
                logger.error("Payment processing error [{}]: {}", errorId, exception.message, exception)
                createErrorResponse(
                    status = Response.Status.BAD_REQUEST,
                    errorCode = "PAYMENT_ERROR",
                    message = exception.message ?: "Payment processing failed",
                    errorId = errorId
                )
            }
            is AirlineIntegrationException -> {
                logger.error("Airline integration error [{}]: {}", errorId, exception.message, exception)
                createErrorResponse(
                    status = Response.Status.BAD_GATEWAY,
                    errorCode = "AIRLINE_INTEGRATION_ERROR",
                    message = "External airline service error",
                    errorId = errorId
                )
            }
            is DatabaseException -> {
                logger.error("Database error [{}]: {}", errorId, exception.message, exception)
                createErrorResponse(
                    status = Response.Status.INTERNAL_SERVER_ERROR,
                    errorCode = "DATABASE_ERROR",
                    message = "Database operation failed",
                    errorId = errorId
                )
            }
            is ConfigurationException -> {
                logger.error("Configuration error [{}]: {}", errorId, exception.message, exception)
                createErrorResponse(
                    status = Response.Status.INTERNAL_SERVER_ERROR,
                    errorCode = "CONFIGURATION_ERROR",
                    message = "System configuration error",
                    errorId = errorId
                )
            }
            // Fallback for any other BusinessException subtypes
            is BusinessException -> {
                logger.error("Business exception [{}]: {}", errorId, exception.message, exception)
                createErrorResponse(
                    status = Response.Status.INTERNAL_SERVER_ERROR,
                    errorCode = "BUSINESS_ERROR",
                    message = exception.message ?: "Business operation failed",
                    errorId = errorId
                )
            }
            // Legacy exception handling for backward compatibility
            is IllegalArgumentException -> {
                logger.warn("Legacy validation error [{}]: {}", errorId, exception.message)
                createErrorResponse(
                    status = Response.Status.BAD_REQUEST,
                    errorCode = "VALIDATION_ERROR",
                    message = exception.message ?: "Validation failed",
                    errorId = errorId
                )
            }
            is IllegalStateException -> {
                logger.warn("Legacy business rule violation [{}]: {}", errorId, exception.message)
                createErrorResponse(
                    status = Response.Status.CONFLICT,
                    errorCode = "BUSINESS_RULE_VIOLATION",
                    message = exception.message ?: "Business rule violation",
                    errorId = errorId
                )
            }
            else -> {
                logger.error("Unexpected error [{}]: {}", errorId, exception.message, exception)
                createErrorResponse(
                    status = Response.Status.INTERNAL_SERVER_ERROR,
                    errorCode = "INTERNAL_ERROR",
                    message = "An unexpected error occurred",
                    errorId = errorId
                )
            }
        }
    }

    private fun createErrorResponse(
        status: Response.Status,
        errorCode: String,
        message: String,
        errorId: String
    ): Response {
        val errorResponse = ErrorResponse(
            errorCode = errorCode,
            message = message,
            timestamp = LocalDateTime.now(),
            errorId = errorId
        )
        return Response.status(status).entity(errorResponse).build()
    }

    private fun generateErrorId(): String {
        return "ERR-${System.currentTimeMillis()}-${(1000..9999).random()}"
    }
}

data class ErrorResponse(
    val errorCode: String,
    val message: String,
    val timestamp: LocalDateTime,
    val errorId: String
)