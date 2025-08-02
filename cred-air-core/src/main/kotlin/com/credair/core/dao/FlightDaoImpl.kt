package com.credair.core.dao

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.Flight
import com.credair.core.model.SearchResult
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import java.math.BigDecimal

@Singleton
class FlightDaoImpl @Inject constructor(private val jdbi: Jdbi) : FlightDao {

    private val flightMapper = RowMapper { rs: ResultSet, _: StatementContext ->
        Flight(
            flightId = rs.getLong("flight_id"),
            flightNumber = rs.getString("flight_number"),
            srcAirportCode = rs.getString("src_airport_code"),
            destAirportCode = rs.getString("dest_airport_code"),
            departureTime = rs.getTimestamp("departure_time"),
            arrivalTime = rs.getTimestamp("arrival_time"),
            price = rs.getBigDecimal("price"),
            currency = rs.getString("currency") ?: "USD",
            totalSeats = rs.getInt("total_seats"),
            availableSeats = rs.getInt("available_seats"),
            aircraftType = rs.getString("aircraft_type"),
            active = rs.getBoolean("active"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at")
        )
    }

    private val searchResultMapper = RowMapper { rs: ResultSet, _: StatementContext ->
        val pathArray = rs.getArray("path")
        val path = (pathArray.array as Array<*>).map { it as Long }.toLongArray()
        
        SearchResult(
            srcAirportCode = rs.getString("src_airport_code"),
            destAirportCode = rs.getString("dest_airport_code"),
            path = path.toTypedArray(),
            departureTime = rs.getTimestamp("departure_time").toLocalDateTime(),
            arrivalTime = rs.getTimestamp("arrival_time").toLocalDateTime(),
            stops = rs.getInt("stops"),
            totalCost = rs.getBigDecimal("total_cost")
        )
    }

    override fun findById(id: Long): Flight? {
        return jdbi.withHandle<Flight?, Exception> { handle ->
            handle.createQuery("SELECT * FROM flights WHERE flight_id = :id")
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
        val now = Timestamp(System.currentTimeMillis())
        val newId = jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO flights (flight_number, src_airport_code, dest_airport_code, 
                                   departure_time, arrival_time, price, currency, total_seats, 
                                   available_seats, aircraft_type, active, created_at, updated_at) 
                VALUES (:flightNumber, :srcAirportCode, :destAirportCode, 
                        :departureTime, :arrivalTime, :price, :currency, :totalSeats, 
                        :availableSeats, :aircraftType, :active, :createdAt, :updatedAt)
            """)
                .bind("flightNumber", entity.flightNumber)
                .bind("srcAirportCode", entity.srcAirportCode)
                .bind("destAirportCode", entity.destAirportCode)
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
                .executeAndReturnGeneratedKeys("flight_id")
                .mapTo(Long::class.java)
                .first()
        }
        return entity.copy(flightId = newId, createdAt = now, updatedAt = now)
    }

    override fun update(entity: Flight): Flight {
        val now = Timestamp(System.currentTimeMillis())
        jdbi.withHandle<Unit, Exception> { handle ->
            handle.createUpdate("""
                UPDATE flights 
                SET flight_number = :flightNumber, 
                    src_airport_code = :srcAirportCode, dest_airport_code = :destAirportCode,
                    departure_time = :departureTime, arrival_time = :arrivalTime, 
                    price = :price, currency = :currency, total_seats = :totalSeats,
                    available_seats = :availableSeats, aircraft_type = :aircraftType,
                    active = :active, updated_at = :updatedAt 
                WHERE flight_id = :flightId
            """)
                .bind("flightId", entity.flightId)
                .bind("flightNumber", entity.flightNumber)
                .bind("srcAirportCode", entity.srcAirportCode)
                .bind("destAirportCode", entity.destAirportCode)
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

    override fun findByAirlineIdAndFlightNumber(airlineId: Long, flightNumber: String): Flight? {
        return jdbi.withHandle<Flight?, Exception> { handle ->
            handle.createQuery("""
                SELECT * FROM flights 
                WHERE flight_number = :flightNumber
            """)
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
                WHERE flight_id = :id
            """)
                .bind("id", flightId)
                .bind("availableSeats", availableSeats)
                .bind("updatedAt", Timestamp(System.currentTimeMillis()))
                .execute()
        } > 0
    }

    override fun searchFlightsOptimized(
        srcAirportCode: String,
        destAirportCode: String,
        limit: Int
    ): List<SearchResult> {
        return jdbi.withHandle<List<SearchResult>, Exception> { handle ->
            handle.createQuery("""
                SELECT *
                FROM mv_flights_0_stop
                WHERE src_airport_code = :srcAirportCode AND dest_airport_code = :destAirportCode 
                AND departure_time > NOW()
                
                UNION ALL
                
                SELECT *
                FROM mv_flights_1_stop
                WHERE src_airport_code = :srcAirportCode AND dest_airport_code = :destAirportCode 
                AND departure_time > NOW()
                
                UNION ALL
                
                SELECT *
                FROM mv_flights_2_stop
                WHERE src_airport_code = :srcAirportCode AND dest_airport_code = :destAirportCode 
                AND departure_time > NOW()
                
                ORDER BY stops ASC, total_cost ASC, arrival_time ASC
                LIMIT :limit
            """)
                .bind("srcAirportCode", srcAirportCode)
                .bind("destAirportCode", destAirportCode)
                .bind("limit", limit)
                .map(searchResultMapper)
                .list()
        }
    }
}