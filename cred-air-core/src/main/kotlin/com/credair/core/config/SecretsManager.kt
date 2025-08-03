package com.credair.core.config

import com.credair.core.util.DatabaseConfig
import com.google.inject.Singleton

interface SecretsManager {
    fun getDatabaseConfig(key: String): DatabaseConfig
}

@Singleton
class DummySecretsManager : SecretsManager {
    
    private val secrets = mapOf(
        "credair-db" to DatabaseConfigImpl(
            url = "jdbc:postgresql://34.47.240.149:5432/postgres",
            driver = "org.postgresql.Driver",
            username = "admin",
            password = "admin"
        )
    )
    
    override fun getDatabaseConfig(key: String): DatabaseConfig {
        return secrets[key] ?: throw IllegalArgumentException("Database config not found for key: $key")
    }
}

private data class DatabaseConfigImpl(
    override var url: String,
    override var driver: String,
    override var username: String,
    override var password: String
) : DatabaseConfig