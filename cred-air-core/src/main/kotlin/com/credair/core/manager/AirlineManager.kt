package com.credair.core.manager

import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.model.Airline
import com.google.inject.Inject
import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Singleton
class AirlineManager @Inject constructor(private val airlineDao: AirlineDao) {

    companion object {
        private val logger = LoggerFactory.getLogger(AirlineManager::class.java)
    }

    fun createAirline(airline: Airline): Airline {
        logger.info("Creating airline with code: {}", airline.code)
        try {
            validateAirline(airline)
            validateUniqueCode(airline.code)
            
            val airlineToCreate = airline.copy(
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            
            val savedAirline = airlineDao.save(airlineToCreate)
            logger.info("Successfully created airline with id: {} and code: {}", savedAirline.id, savedAirline.code)
            return savedAirline
        } catch (e: IllegalArgumentException) {
            logger.warn("Validation failed for airline creation: {}", e.message)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error creating airline with code: {}", airline.code, e)
            throw RuntimeException("Failed to create airline: ${e.message}", e)
        }
    }

    fun updateAirline(id: Long, airline: Airline): Airline {
        logger.info("Updating airline with id: {}", id)
        try {
            val existingAirline = getAirlineById(id)
            validateAirline(airline)
            
            if (airline.code != existingAirline.code) {
                logger.info("Airline code changed from {} to {}, validating uniqueness", existingAirline.code, airline.code)
                validateUniqueCode(airline.code)
            }
            
            val airlineToUpdate = airline.copy(
                id = id,
                createdAt = existingAirline.createdAt,
                updatedAt = LocalDateTime.now()
            )
            
            val updatedAirline = airlineDao.update(airlineToUpdate)
            logger.info("Successfully updated airline with id: {}", id)
            return updatedAirline
        } catch (e: IllegalArgumentException) {
            logger.warn("Validation failed for airline update with id {}: {}", id, e.message)
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error updating airline with id: {}", id, e)
            throw RuntimeException("Failed to update airline: ${e.message}", e)
        }
    }

    fun getAirlineById(id: Long): Airline {
        logger.debug("Retrieving airline with id: {}", id)
        try {
            return airlineDao.findById(id) 
                ?: throw IllegalArgumentException("Airline with id $id not found")
        } catch (e: IllegalArgumentException) {
            logger.warn("Airline not found with id: {}", id)
            throw e
        } catch (e: Exception) {
            logger.error("Error retrieving airline with id: {}", id, e)
            throw RuntimeException("Failed to retrieve airline: ${e.message}", e)
        }
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
        logger.info("Deleting airline with id: {}", id)
        try {
            val airline = getAirlineById(id)
            val result = airlineDao.delete(id)
            if (result) {
                logger.info("Successfully deleted airline with id: {}", id)
            } else {
                logger.warn("Failed to delete airline with id: {} - no rows affected", id)
            }
            return result
        } catch (e: IllegalArgumentException) {
            logger.warn("Cannot delete airline - not found with id: {}", id)
            throw e
        } catch (e: Exception) {
            logger.error("Error deleting airline with id: {}", id, e)
            throw RuntimeException("Failed to delete airline: ${e.message}", e)
        }
    }

    private fun validateAirline(airline: Airline) {
        require(airline.name.isNotBlank()) { "Airline name cannot be blank" }
        require(airline.code.isNotBlank()) { "Airline code cannot be blank" }
        require(airline.code.length in 2..3) { "Airline code must be 2-3 characters" }
        require(airline.country.isNotBlank()) { "Country cannot be blank" }
        require(airline.code.matches(Regex("^[A-Z]+$"))) { "Airline code must contain only uppercase letters" }
    }

    private fun validateUniqueCode(code: String) {
        logger.debug("Validating unique code: {}", code)
        try {
            val existingAirline = airlineDao.findByCode(code)
            if (existingAirline != null) {
                logger.warn("Duplicate airline code detected: {}", code)
                throw IllegalArgumentException("Airline with code $code already exists")
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error validating airline code uniqueness for: {}", code, e)
            throw RuntimeException("Failed to validate airline code: ${e.message}", e)
        }
    }
}