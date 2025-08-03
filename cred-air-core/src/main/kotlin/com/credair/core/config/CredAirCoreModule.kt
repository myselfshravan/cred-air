package com.credair.core.config

import com.credair.core.dao.AirlineDaoImpl
import com.credair.core.dao.BookingDaoJdbiImpl
import com.credair.core.dao.FlightBookingDaoJdbiImpl
import com.credair.core.dao.FlightDaoImpl
import com.credair.core.dao.FlightPassengerDaoJdbiImpl
import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.dao.interfaces.FlightBookingDao
import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.dao.interfaces.FlightPassengerDao
import com.credair.core.exception.ConfigurationException
import com.credair.core.payment.PaymentProvider
import com.credair.core.payment.StripePaymentManager
import com.credair.core.util.DatabaseConfig
import com.credair.core.util.SimpleDataSource
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class CredAirCoreModule : AbstractModule() {

    companion object {
        private val logger = LoggerFactory.getLogger(CredAirCoreModule::class.java)
    }

    override fun configure() {
        logger.info("Configuring CredAirCoreModule dependencies")
        bind(SecretsManager::class.java).to(DummySecretsManager::class.java)
        bind(AirlineDao::class.java).to(AirlineDaoImpl::class.java)
        bind(FlightDao::class.java).to(FlightDaoImpl::class.java)
        bind(BookingDao::class.java).to(BookingDaoJdbiImpl::class.java)
        bind(FlightPassengerDao::class.java).to(FlightPassengerDaoJdbiImpl::class.java)
        bind(FlightBookingDao::class.java).to(FlightBookingDaoJdbiImpl::class.java)
        bind(PaymentProvider::class.java).to(StripePaymentManager::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDatabaseConfig(secretsManager: SecretsManager): DatabaseConfig {
        logger.info("Providing database configuration")
        try {
            val config = secretsManager.getDatabaseConfig("credair-db")
            logger.info("Database configuration loaded successfully for: {}", config.url)
            return config
        } catch (e: Exception) {
            logger.error("Failed to load database configuration", e)
            throw ConfigurationException("Database configuration error: ${e.message}", e)
        }
    }
    
    @Provides
    @Singleton
    fun provideDataSource(config: DatabaseConfig): DataSource {
        logger.info("Creating data source for database: {}", config.url)
        try {
            val dataSource = SimpleDataSource(config.url, config.username, config.password)
            logger.info("Data source created successfully")
            return dataSource
        } catch (e: Exception) {
            logger.error("Failed to create data source for: {}", config.url, e)
            throw ConfigurationException("Data source creation error: ${e.message}", e)
        }
    }
    
    @Provides
    @Singleton
    fun provideJdbi(dataSource: DataSource): Jdbi {
        logger.info("Creating JDBI instance with Kotlin plugins")
        try {
            val jdbi = Jdbi.create(dataSource)
                .installPlugin(KotlinPlugin())
                .installPlugin(KotlinSqlObjectPlugin())
            logger.info("JDBI instance created successfully with plugins installed")
            return jdbi
        } catch (e: Exception) {
            logger.error("Failed to create JDBI instance", e)
            throw ConfigurationException("JDBI creation error: ${e.message}", e)
        }
    }
}