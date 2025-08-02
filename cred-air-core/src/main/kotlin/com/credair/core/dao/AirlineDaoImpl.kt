package com.credair.core.dao

import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.model.Airline
import com.google.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.LocalDateTime

class AirlineDaoImpl @Inject constructor(private val jdbi: Jdbi) : AirlineDao {

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
        return jdbi.withHandle<Airline?, Exception> { handle ->
            handle.createQuery("SELECT * FROM airlines WHERE id = :id")
                .bind("id", id)
                .map(airlineMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findAll(): List<Airline> {
        return jdbi.withHandle<List<Airline>, Exception> { handle ->
            handle.createQuery("SELECT * FROM airlines ORDER BY name")
                .map(airlineMapper)
                .list()
        }
    }

    override fun save(entity: Airline): Airline {
        val now = LocalDateTime.now()
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
        return entity.copy(id = newId, createdAt = now, updatedAt = now)
    }

    override fun update(entity: Airline): Airline {
        val now = LocalDateTime.now()
        jdbi.withHandle<Unit, Exception> { handle ->
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
        return entity.copy(updatedAt = now)
    }

    override fun delete(id: Long): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM airlines WHERE id = :id")
                .bind("id", id)
                .execute()
        } > 0
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