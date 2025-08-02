package com.credair.airline.dao

import com.credair.airline.model.Flight
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.LocalDateTime
import java.math.BigDecimal

class FlightDaoImpl(private val jdbi: Jdbi) : FlightDao {

    private val flightMapper = RowMapper<Flight> { rs: ResultSet, _: StatementContext ->
        Flight(
            id = rs.getLong("id"),
            airlineId = rs.getLong("airline_id"),
            flightNumber = rs.getString("flight_number"),
            sourceAirport = rs.getString("source_airport"),
            destinationAirport = rs.getString("destination_airport"),
            departureTime = rs.getTimestamp("departure_time").toLocalDateTime(),
            arrivalTime = rs.getTimestamp("arrival_time").toLocalDateTime(),
            price = rs.getBigDecimal("price"),
            currency = rs.getString("currency"),
            totalSeats = rs.getInt("total_seats"),
            availableSeats = rs.getInt("available_seats"),
            aircraftType = rs.getString("aircraft_type"),
            active = rs.getBoolean("active"),
            createdAt = rs.getTimestamp("created_at")?.toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at")?.toLocalDateTime()
        )
    }

    override fun findById(id: Long): Flight? {
        return jdbi.withHandle<Flight?, Exception> { handle ->
            handle.createQuery("SELECT * FROM flights WHERE id = :id")
                .bind("id", id)
                .map(flightMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findAll(): List<Flight> {
        return jdbi.withHandle<List<Flight>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flights ORDER BY departure_time")
                .map(flightMapper)
                .list()
        }
    }

    override fun save(entity: Flight): Flight {
        val now = LocalDateTime.now()
        val newId = jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO flights (airline_id, flight_number, source_airport, destination_airport, 
                                   departure_time, arrival_time, price, currency, total_seats, 
                                   available_seats, aircraft_type, active, created_at, updated_at) 
                VALUES (:airlineId, :flightNumber, :sourceAirport, :destinationAirport, 
                        :departureTime, :arrivalTime, :price, :currency, :totalSeats, 
                        :availableSeats, :aircraftType, :active, :createdAt, :updatedAt)
            """)
                .bind("airlineId", entity.airlineId)
                .bind("flightNumber", entity.flightNumber)
                .bind("sourceAirport", entity.sourceAirport)
                .bind("destinationAirport", entity.destinationAirport)
                .bind("departureTime", entity.departureTime)
                .bind("arrivalTime", entity.arrivalTime)
                .bind("price", entity.price)
                .bind("currency", entity.currency)
                .bind("totalSeats", entity.totalSeats)
                .bind("availableSeats", entity.availableSeats)
                .bind("aircraftType", entity.aircraftType)
                .bind("active", entity.active)
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long::class.java)
                .one()
        }
        return entity.copy(id = newId, createdAt = now, updatedAt = now)
    }

    override fun update(entity: Flight): Flight {
        val now = LocalDateTime.now()
        jdbi.withHandle<Unit, Exception> { handle ->
            handle.createUpdate("""
                UPDATE flights 
                SET airline_id = :airlineId, flight_number = :flightNumber, 
                    source_airport = :sourceAirport, destination_airport = :destinationAirport,
                    departure_time = :departureTime, arrival_time = :arrivalTime, 
                    price = :price, currency = :currency, total_seats = :totalSeats,
                    available_seats = :availableSeats, aircraft_type = :aircraftType,
                    active = :active, updated_at = :updatedAt 
                WHERE id = :id
            """)
                .bind("id", entity.id)
                .bind("airlineId", entity.airlineId)
                .bind("flightNumber", entity.flightNumber)
                .bind("sourceAirport", entity.sourceAirport)
                .bind("destinationAirport", entity.destinationAirport)
                .bind("departureTime", entity.departureTime)
                .bind("arrivalTime", entity.arrivalTime)
                .bind("price", entity.price)
                .bind("currency", entity.currency)
                .bind("totalSeats", entity.totalSeats)
                .bind("availableSeats", entity.availableSeats)
                .bind("aircraftType", entity.aircraftType)
                .bind("active", entity.active)
                .bind("updatedAt", now)
                .execute()
        }
        return entity.copy(updatedAt = now)
    }

    override fun deleteById(id: Long): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM flights WHERE id = :id")
                .bind("id", id)
                .execute()
        } > 0
    }

    override fun exists(id: Long): Boolean {
        return jdbi.withHandle<Boolean, Exception> { handle ->
            handle.createQuery("SELECT COUNT(*) FROM flights WHERE id = :id")
                .bind("id", id)
                .mapTo(Int::class.java)
                .one() > 0
        }
    }

    override fun findByAirlineId(airlineId: Long): List<Flight> {
        return jdbi.withHandle<List<Flight>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flights WHERE airline_id = :airlineId ORDER BY departure_time")
                .bind("airlineId", airlineId)
                .map(flightMapper)
                .list()
        }
    }

    override fun findByFlightNumber(flightNumber: String): List<Flight> {
        return jdbi.withHandle<List<Flight>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flights WHERE flight_number = :flightNumber ORDER BY departure_time")
                .bind("flightNumber", flightNumber)
                .map(flightMapper)
                .list()
        }
    }

    override fun findByRoute(sourceAirport: String, destinationAirport: String): List<Flight> {
        return jdbi.withHandle<List<Flight>, Exception> { handle ->
            handle.createQuery("""
                SELECT * FROM flights 
                WHERE source_airport = :sourceAirport AND destination_airport = :destinationAirport 
                ORDER BY departure_time
            """)
                .bind("sourceAirport", sourceAirport)
                .bind("destinationAirport", destinationAirport)
                .map(flightMapper)
                .list()
        }
    }

    override fun findByRouteAndDate(
        sourceAirport: String, 
        destinationAirport: String, 
        departureDate: LocalDateTime
    ): List<Flight> {
        return jdbi.withHandle<List<Flight>, Exception> { handle ->
            handle.createQuery("""
                SELECT * FROM flights 
                WHERE source_airport = :sourceAirport AND destination_airport = :destinationAirport 
                AND DATE(departure_time) = DATE(:departureDate)
                ORDER BY departure_time
            """)
                .bind("sourceAirport", sourceAirport)
                .bind("destinationAirport", destinationAirport)
                .bind("departureDate", departureDate)
                .map(flightMapper)
                .list()
        }
    }

    override fun findAvailableFlights(): List<Flight> {
        return jdbi.withHandle<List<Flight>, Exception> { handle ->
            handle.createQuery("""
                SELECT * FROM flights 
                WHERE active = true AND available_seats > 0 AND departure_time > NOW()
                ORDER BY departure_time
            """)
                .map(flightMapper)
                .list()
        }
    }

    override fun findByAirlineIdAndFlightNumber(airlineId: Long, flightNumber: String): Flight? {
        return jdbi.withHandle<Flight?, Exception> { handle ->
            handle.createQuery("""
                SELECT * FROM flights 
                WHERE airline_id = :airlineId AND flight_number = :flightNumber
            """)
                .bind("airlineId", airlineId)
                .bind("flightNumber", flightNumber)
                .map(flightMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun updateAvailableSeats(flightId: Long, availableSeats: Int): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                UPDATE flights 
                SET available_seats = :availableSeats, updated_at = :updatedAt 
                WHERE id = :id
            """)
                .bind("id", flightId)
                .bind("availableSeats", availableSeats)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }
}