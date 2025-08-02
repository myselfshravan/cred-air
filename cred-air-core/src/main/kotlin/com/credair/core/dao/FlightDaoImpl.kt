package com.credair.core.dao

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.Flight
import com.credair.core.model.FlightAirline
import com.credair.core.model.FlightDetails
import com.credair.core.model.FlightPrice
import com.credair.core.model.FlightSegment
import com.credair.core.model.FlightStop
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Duration
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

    private val flightSegmentMapper = RowMapper { rs: ResultSet, _: StatementContext ->
        FlightSegment(
            airline = FlightAirline(
                name = rs.getString("airline_name") ?: "Unknown Airline",
                logoUrl = rs.getString("airline_logo_url") ?: ""
            ),
            flightDetails = FlightDetails(
                flightNumber = rs.getString("flight_number"),
                aircraftType = rs.getString("aircraft_type") ?: "Unknown"
            ),
            departure = FlightStop(
                time = rs.getTimestamp("departure_time").toLocalDateTime().toLocalTime(),
                airportCode = rs.getString("src_airport_code"),
                city = rs.getString("src_airport_code") // Default to airport code, could be enhanced
            ),
            arrival = FlightStop(
                time = rs.getTimestamp("arrival_time").toLocalDateTime().toLocalTime(),
                airportCode = rs.getString("dest_airport_code"),
                city = rs.getString("dest_airport_code") // Default to airport code, could be enhanced
            ),
            segmentDuration = Duration.between(
                rs.getTimestamp("departure_time").toLocalDateTime(),
                rs.getTimestamp("arrival_time").toLocalDateTime()
            ),
            price = FlightPrice(
                amount = rs.getBigDecimal("total_cost"),
                currency = "USD",
                perPerson = true
            ),
            availableSeats = rs.getInt("available_seats")
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
    ): List<FlightSegment> {
        return jdbi.withHandle<List<FlightSegment>, Exception> { handle ->
            val baseSql = """
                SELECT mv.*, f.flight_number, f.aircraft_type, f.available_seats, a.name as airline_name, a.logo_url as airline_logo_url
                FROM (
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
                ) mv
                JOIN flights f ON f.flight_id = mv.path[1]
                LEFT JOIN airlines a ON a.id = f.airline_id
                WHERE f.available_seats >= :noOfSeats
            """
            
            val dateFilter = if (departureDate != null) " AND DATE(mv.departure_time) = DATE(:departureDate)" else ""
            
            val orderByClause = when (sortBy.lowercase()) {
                "departure_time" -> "mv.departure_time"
                "arrival_time" -> "mv.arrival_time"
                "price" -> "mv.total_cost"
                "duration" -> "(mv.arrival_time - mv.departure_time)"
                else -> "mv.departure_time"
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
            
            query.map(flightSegmentMapper).list()
        }
    }
}