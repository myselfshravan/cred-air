package com.credair.core.dao

import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.exception.BusinessRuleViolationException
import com.credair.core.exception.DatabaseException
import com.credair.core.exception.ResourceNotFoundException
import com.credair.core.exception.ValidationException
import com.credair.core.model.Airline
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDateTime

@Singleton
class AirlineDaoImpl @Inject constructor(private val jdbi: Jdbi) : AirlineDao {

    companion object {
        private val logger = LoggerFactory.getLogger(AirlineDaoImpl::class.java)
    }

    private val airlineMapper = RowMapper<Airline> { rs: ResultSet, _: StatementContext ->
        Airline(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            code = rs.getString("code"),
            country = rs.getString("country"),
            description = rs.getString("description"),
            website = rs.getString("website"),
            active = rs.getBoolean("active"),
            createdAt = rs.getTimestamp("created_at")?.toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at")?.toLocalDateTime()
        )
    }

    override fun findById(id: Long): Airline? {
        logger.debug("Finding airline by id: {}", id)
        return try {
            jdbi.withHandle<Airline?, Exception> { handle ->
                handle.createQuery("SELECT * FROM airlines WHERE id = :id")
                    .bind("id", id)
                    .map(airlineMapper)
                    .findFirst()
                    .orElse(null)
            }
        } catch (e: SQLException) {
            logger.error("Database error finding airline by id: {}", id, e)
            throw DatabaseException("Database error retrieving airline", e)
        } catch (e: Exception) {
            logger.error("Unexpected error finding airline by id: {}", id, e)
            throw DatabaseException("Error retrieving airline", e)
        }
    }

    override fun findAll(): List<Airline> {
        logger.debug("Finding all airlines")
        return try {
            jdbi.withHandle<List<Airline>, Exception> { handle ->
                handle.createQuery("SELECT * FROM airlines ORDER BY name")
                    .map(airlineMapper)
                    .list()
            }
        } catch (e: SQLException) {
            logger.error("Database error finding all airlines", e)
            throw DatabaseException("Database error retrieving airlines", e)
        } catch (e: Exception) {
            logger.error("Unexpected error finding all airlines", e)
            throw DatabaseException("Error retrieving airlines", e)
        }
    }

    override fun save(entity: Airline): Airline {
        logger.debug("Saving airline with code: {}", entity.code)
        val now = LocalDateTime.now()
        return try {
            val newId = jdbi.withHandle<Long, Exception> { handle ->
                handle.createUpdate("""
                    INSERT INTO airlines (name, code, country, description, website, active, created_at, updated_at) 
                    VALUES (:name, :code, :country, :description, :website, :active, :createdAt, :updatedAt)
                """)
                    .bind("name", entity.name)
                    .bind("code", entity.code)
                    .bind("country", entity.country)
                    .bind("description", entity.description)
                    .bind("website", entity.website)
                    .bind("active", entity.active)
                    .bind("createdAt", now)
                    .bind("updatedAt", now)
                    .executeAndReturnGeneratedKeys("id")
                    .mapTo(Long::class.java)
                    .one()
            }
            val savedAirline = entity.copy(id = newId, createdAt = now, updatedAt = now)
            logger.info("Successfully saved airline with id: {} and code: {}", newId, entity.code)
            savedAirline
        } catch (e: SQLException) {
            if (e.sqlState == "23505") { // Unique constraint violation
                logger.warn("Duplicate airline code attempted: {}", entity.code)
                throw ValidationException("Airline with code ${entity.code} already exists")
            }
            logger.error("Database error saving airline with code: {}", entity.code, e)
            throw DatabaseException("Database error saving airline", e)
        } catch (e: Exception) {
            logger.error("Unexpected error saving airline with code: {}", entity.code, e)
            throw DatabaseException("Error saving airline", e)
        }
    }

    override fun update(entity: Airline): Airline {
        logger.debug("Updating airline with id: {}", entity.id)
        val now = LocalDateTime.now()
        return try {
            val rowsUpdated = jdbi.withHandle<Int, Exception> { handle ->
                handle.createUpdate("""
                    UPDATE airlines 
                    SET name = :name, code = :code, country = :country, description = :description, 
                        website = :website, active = :active, updated_at = :updatedAt 
                    WHERE id = :id
                """)
                    .bind("id", entity.id)
                    .bind("name", entity.name)
                    .bind("code", entity.code)
                    .bind("country", entity.country)
                    .bind("description", entity.description)
                    .bind("website", entity.website)
                    .bind("active", entity.active)
                    .bind("updatedAt", now)
                    .execute()
            }
            if (rowsUpdated == 0) {
                logger.warn("No airline found to update with id: {}", entity.id)
                throw ResourceNotFoundException("Airline with id ${entity.id} not found")
            }
            val updatedAirline = entity.copy(updatedAt = now)
            logger.info("Successfully updated airline with id: {}", entity.id)
            updatedAirline
        } catch (e: SQLException) {
            if (e.sqlState == "23505") { // Unique constraint violation
                logger.warn("Duplicate airline code attempted during update: {}", entity.code)
                throw ValidationException("Airline with code ${entity.code} already exists")
            }
            logger.error("Database error updating airline with id: {}", entity.id, e)
            throw DatabaseException("Database error updating airline", e)
        } catch (e: ResourceNotFoundException) {
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error updating airline with id: {}", entity.id, e)
            throw DatabaseException("Error updating airline", e)
        }
    }

    override fun delete(id: Long): Boolean {
        logger.debug("Deleting airline with id: {}", id)
        return try {
            val rowsDeleted = jdbi.withHandle<Int, Exception> { handle ->
                handle.createUpdate("DELETE FROM airlines WHERE id = :id")
                    .bind("id", id)
                    .execute()
            }
            val success = rowsDeleted > 0
            if (success) {
                logger.info("Successfully deleted airline with id: {}", id)
            } else {
                logger.warn("No airline found to delete with id: {}", id)
            }
            success
        } catch (e: SQLException) {
            if (e.sqlState == "23503") { // Foreign key constraint violation
                logger.warn("Cannot delete airline with id {} - referenced by other entities", id)
                throw BusinessRuleViolationException("Cannot delete airline - it is referenced by flights or bookings")
            }
            logger.error("Database error deleting airline with id: {}", id, e)
            throw DatabaseException("Database error deleting airline", e)
        } catch (e: Exception) {
            logger.error("Unexpected error deleting airline with id: {}", id, e)
            throw DatabaseException("Error deleting airline", e)
        }
    }


    override fun findByCode(code: String): Airline? {
        return jdbi.withHandle<Airline?, Exception> { handle ->
            handle.createQuery("SELECT * FROM airlines WHERE code = :code")
                .bind("code", code)
                .map(airlineMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findByCountry(country: String): List<Airline> {
        return jdbi.withHandle<List<Airline>, Exception> { handle ->
            handle.createQuery("SELECT * FROM airlines WHERE country = :country ORDER BY name")
                .bind("country", country)
                .map(airlineMapper)
                .list()
        }
    }

    override fun findActiveAirlines(): List<Airline> {
        return jdbi.withHandle<List<Airline>, Exception> { handle ->
            handle.createQuery("SELECT * FROM airlines WHERE active = true ORDER BY name")
                .map(airlineMapper)
                .list()
        }
    }
}