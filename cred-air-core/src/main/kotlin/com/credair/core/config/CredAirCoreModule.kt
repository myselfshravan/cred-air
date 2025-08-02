package com.credair.core.config

import com.credair.core.dao.AirlineDaoImpl
import com.credair.core.dao.BookingDaoJdbiImpl
import com.credair.core.dao.FlightDaoImpl
import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.manager.AirlineManager
import com.credair.core.manager.BookingManager
import com.credair.core.manager.FlightSearchManager
import com.google.inject.AbstractModule
import com.google.inject.Singleton

class CredAirCoreModule : AbstractModule() {

    override fun configure() {
        bind(AirlineDao::class.java).to(AirlineDaoImpl::class.java).`in`(Singleton::class.java)
        bind(FlightDao::class.java).to(FlightDaoImpl::class.java).`in`(Singleton::class.java)
        bind(BookingDao::class.java).to(BookingDaoJdbiImpl::class.java).`in`(Singleton::class.java)
        bind(AirlineManager::class.java).`in`(Singleton::class.java)
        bind(FlightSearchManager::class.java).`in`(Singleton::class.java)
        bind(BookingManager::class.java).`in`(Singleton::class.java)
    }
}