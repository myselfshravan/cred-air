package com.credair.core.config

import com.credair.core.dao.AirlineDaoImpl
import com.credair.core.dao.BookingDaoJdbiImpl
import com.credair.core.dao.FlightDaoImpl
import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.dao.interfaces.FlightDao
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
import javax.sql.DataSource

class CredAirCoreModule : AbstractModule() {

    override fun configure() {
        bind(SecretsManager::class.java).to(DummySecretsManager::class.java)
        bind(AirlineDao::class.java).to(AirlineDaoImpl::class.java)
        bind(FlightDao::class.java).to(FlightDaoImpl::class.java)
        bind(BookingDao::class.java).to(BookingDaoJdbiImpl::class.java)
        bind(PaymentProvider::class.java).to(StripePaymentManager::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDatabaseConfig(secretsManager: SecretsManager): DatabaseConfig {
        return secretsManager.getDatabaseConfig("credair-db")
    }
    
    @Provides
    @Singleton
    fun provideDataSource(config: DatabaseConfig): DataSource {
        return SimpleDataSource(config.url, config.username, config.password)
    }
    
    @Provides
    @Singleton
    fun provideJdbi(dataSource: DataSource): Jdbi {
        return Jdbi.create(dataSource)
            .installPlugin(KotlinPlugin())
            .installPlugin(KotlinSqlObjectPlugin())
    }
}