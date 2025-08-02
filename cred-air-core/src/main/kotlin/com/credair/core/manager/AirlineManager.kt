package com.credair.core.manager

import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.model.Airline
import com.google.inject.Inject
import com.google.inject.Singleton
import java.time.LocalDateTime

@Singleton
class AirlineManager @Inject constructor(private val airlineDao: AirlineDao) {

    fun createAirline(airline: Airline): Airline {
        validateAirline(airline)
        validateUniqueCode(airline.code)
        
        val airlineToCreate = airline.copy(
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return airlineDao.save(airlineToCreate)
    }

    fun updateAirline(id: Long, airline: Airline): Airline {
        val existingAirline = getAirlineById(id)
        validateAirline(airline)
        
        if (airline.code != existingAirline.code) {
            validateUniqueCode(airline.code)
        }
        
        val airlineToUpdate = airline.copy(
            id = id,
            createdAt = existingAirline.createdAt,
            updatedAt = LocalDateTime.now()
        )
        
        return airlineDao.update(airlineToUpdate)
    }

    fun getAirlineById(id: Long): Airline {
        return airlineDao.findById(id) 
            ?: throw IllegalArgumentException("Airline with id $id not found")
    }

    fun getAirlineByCode(code: String): Airline {
        return airlineDao.findByCode(code) 
            ?: throw IllegalArgumentException("Airline with code $code not found")
    }

    fun getAllAirlines(): List<Airline> {
        return airlineDao.findAll()
    }

    fun getActiveAirlines(): List<Airline> {
        return airlineDao.findActiveAirlines()
    }

    fun getAirlinesByCountry(country: String): List<Airline> {
        return airlineDao.findByCountry(country)
    }

    fun activateAirline(id: Long): Airline {
        val airline = getAirlineById(id)
        val activatedAirline = airline.copy(
            active = true,
            updatedAt = LocalDateTime.now()
        )
        return airlineDao.update(activatedAirline)
    }

    fun deactivateAirline(id: Long): Airline {
        val airline = getAirlineById(id)
        val deactivatedAirline = airline.copy(
            active = false,
            updatedAt = LocalDateTime.now()
        )
        return airlineDao.update(deactivatedAirline)
    }

    fun deleteAirline(id: Long): Boolean {
        val airline = getAirlineById(id)
        return airlineDao.delete(id)
    }

    private fun validateAirline(airline: Airline) {
        require(airline.name.isNotBlank()) { "Airline name cannot be blank" }
        require(airline.code.isNotBlank()) { "Airline code cannot be blank" }
        require(airline.code.length in 2..3) { "Airline code must be 2-3 characters" }
        require(airline.country.isNotBlank()) { "Country cannot be blank" }
        require(airline.code.matches(Regex("^[A-Z]+$"))) { "Airline code must contain only uppercase letters" }
    }

    private fun validateUniqueCode(code: String) {
        val existingAirline = airlineDao.findByCode(code)
        if (existingAirline != null) {
            throw IllegalArgumentException("Airline with code $code already exists")
        }
    }
}