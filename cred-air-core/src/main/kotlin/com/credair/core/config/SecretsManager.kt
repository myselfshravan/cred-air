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
            url = "jdbc:h2:mem:credair_db",
            driver = "org.h2.Driver",
            username = "sa",
            password = ""
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