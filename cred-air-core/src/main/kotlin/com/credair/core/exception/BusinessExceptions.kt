package com.credair.core.exception

sealed class BusinessException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class ResourceNotFoundException(message: String) : BusinessException(message)

class ValidationException(message: String) : BusinessException(message)

class BusinessRuleViolationException(message: String) : BusinessException(message)

class PaymentProcessingException(message: String, cause: Throwable? = null) : BusinessException(message, cause)

class AirlineIntegrationException(message: String, cause: Throwable? = null) : BusinessException(message, cause)

class DatabaseException(message: String, cause: Throwable? = null) : BusinessException(message, cause)

class ConfigurationException(message: String, cause: Throwable? = null) : BusinessException(message, cause)