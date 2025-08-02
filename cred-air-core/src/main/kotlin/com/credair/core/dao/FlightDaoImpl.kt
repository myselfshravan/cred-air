package com.credair.core.dao

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.Flight
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime

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
            updatedAt = rs.getTimestamp("updated_at"),
            airlineId = rs.getLong("airline_id"),
            sourceAirport = rs.getString("source_airport"),
            destinationAirport = rs.getString("destination_airport")
        )
    }

    private val flightSearchResultMapper = RowMapper { rs: ResultSet, _: StatementContext ->
        com.credair.core.model.FlightSearchResult(
            airlineName = rs.getString("airline_name") ?: "Unknown Airline",
            airlineLogoUrl = rs.getString("airline_logo_url") ?: "",
            departureTime = rs.getTimestamp("departure_time").toInstant().toEpochMilli(),
            arrivalTime = rs.getTimestamp("arrival_time").toInstant().toEpochMilli(),
            departureAirport = rs.getString("src_airport_code"),
            arrivalAirport = rs.getString("dest_airport_code"),
            totalDurationMinutes = rs.getInt("total_time_minutes"),
            stopCount = rs.getInt("stops"),
            stopAirports = rs.getString("path")?.split(",")?.dropLast(1) ?: emptyList(),
            priceAmount = rs.getBigDecimal("total_cost"),
            priceCurrency = "USD"
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
            handle.createQuery("SELECT * FROM flights ORDER BY departs_at")
                .map(flightMapper)
                .list()
        }
    }

    override fun save(entity: Flight): Flight {
        val now = Timestamp(System.currentTimeMillis())
        val newId = jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO flights (flight_number, src_airport_code, dest_airport_code, 
                                   departs_at, arrives_at, price, currency, total_seats, 
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
                    departs_at = :departureTime, arrives_at = :arrivalTime, 
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

    override fun delete(id: Long): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM flights WHERE flight_id = :id")
                .bind("id", id)
                .execute()
        } > 0
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
        departureDate: LocalDateTime?,
        noOfSeats: Int,
        sortBy: String,
        sortOrder: String,
        page: Int,
        pageSize: Int
    ): List<com.credair.core.model.FlightSearchResult> {
        return jdbi.withHandle<List<com.credair.core.model.FlightSearchResult>, Exception> { handle ->
            val baseSql = """
                SELECT 
                    departure_time,
                    arrival_time,
                    total_cost,
                    total_time_minutes,
                    stops,
                    path,
                    min_available_seats,
                    airline_name,
                    airline_logo_url,
                    aircraft_type,
                    flight_number,
                    src_airport_code,
                    dest_airport_code
                FROM mv_flight_journeys
                WHERE src_airport_code = :srcAirportCode 
                  AND dest_airport_code = :destAirportCode 
                  AND departure_time >= NOW()
                  AND min_available_seats >= :noOfSeats
            """
            
            val dateFilter = if (departureDate != null) " AND departure_date = DATE(:departureDate)" else ""
            
            val orderByClause = when (sortBy.lowercase()) {
                "departure_time" -> "departure_time"
                "arrival_time" -> "arrival_time"
                "price" -> "total_cost"
                "duration" -> "total_time_minutes"
                else -> "departure_time"
            }
            
            val fullSql = "$baseSql$dateFilter ORDER BY $orderByClause ${sortOrder.uppercase()} LIMIT :pageSize OFFSET :offset"
            
            val query = handle.createQuery(fullSql)
                .bind("srcAirportCode", srcAirportCode)
                .bind("destAirportCode", destAirportCode)
                .bind("noOfSeats", noOfSeats)
                .bind("pageSize", pageSize)
                .bind("offset", page * pageSize)
            
            if (departureDate != null) {
                query.bind("departureDate", Timestamp.valueOf(departureDate))
            }
            
            query.map(flightSearchResultMapper).list()
        }
    }
}