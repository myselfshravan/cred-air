package com.credair.core.dao

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.*
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
            departureTime = rs.getTimestamp("departs_at"),
            arrivalTime = rs.getTimestamp("arrives_at"),
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
            stopAirports = (rs.getArray("layover_points")?.array as? Array<String>)?.toList() ?: emptyList(),
            priceAmount = rs.getBigDecimal("total_cost"),
            priceCurrency = "USD",
            flightIds = (rs.getArray("flight_number")?.array as? Array<Long>)?.toList() ?: emptyList()
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
                    layover_points,
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

    override fun getFlightJourney(flightIds: List<Long>): FlightJourney? {
        if (flightIds.isEmpty()) return null
        
        return jdbi.withHandle<FlightJourney?, Exception> { handle ->
            val flights = handle.createQuery("""
                SELECT f.*, a.name as airline_name, a.logo_url as airline_logo_url 
                FROM flights f 
                JOIN airlines a ON f.airline_id = a.id 
                WHERE f.flight_id IN (<flightIds>) 
                ORDER BY f.departs_at
            """)
                .bindList("flightIds", flightIds)
                .map { rs, _ ->
                    Triple(
                        Flight(
                            flightId = rs.getLong("flight_id"),
                            flightNumber = rs.getString("flight_number"),
                            srcAirportCode = rs.getString("src_airport_code"),
                            destAirportCode = rs.getString("dest_airport_code"),
                            departureTime = rs.getTimestamp("departs_at"),
                            arrivalTime = rs.getTimestamp("arrives_at"),
                            price = rs.getBigDecimal("price"),
                            currency = rs.getString("currency") ?: "USD",
                            totalSeats = rs.getInt("total_seats"),
                            availableSeats = rs.getInt("available_seats"),
                            aircraftType = rs.getString("aircraft_type"),
                            active = rs.getBoolean("active"),
                            createdAt = rs.getTimestamp("created_at"),
                            updatedAt = rs.getTimestamp("updated_at"),
                            airlineId = rs.getLong("airline_id"),
                            sourceAirport = rs.getString("source_airport") ?: rs.getString("src_airport_code"),
                            destinationAirport = rs.getString("destination_airport") ?: rs.getString("dest_airport_code")
                        ),
                        rs.getString("airline_name"),
                        rs.getString("airline_logo_url") ?: ""
                    )
                }
                .list()

            if (flights.isEmpty()) return@withHandle null

            val segments = flights.map { (flight, airlineName, logoUrl) ->
                FlightSegment(
                    airline = FlightAirline(
                        name = airlineName,
                        logoUrl = logoUrl
                    ),
                    departure = FlightStop(
                        departsAt = flight.departureTime.toInstant().toEpochMilli(),
                        arrivesAt = flight.departureTime.toInstant().toEpochMilli(),
                        airportCode = flight.srcAirportCode,
                        city = flight.sourceAirport
                    ),
                    arrival = FlightStop(
                        departsAt = flight.arrivalTime.toInstant().toEpochMilli(),
                        arrivesAt = flight.arrivalTime.toInstant().toEpochMilli(),
                        airportCode = flight.destAirportCode,
                        city = flight.destinationAirport
                    ),
                    segmentDuration = Duration.ofMinutes(flight.duration),
                    price = FlightPrice(
                        amount = flight.price,
                        currency = flight.currency
                    ),
                    id = flight.flightId
                )
            }

            val layovers = mutableListOf<Layover>()
            for (i in 0 until segments.size - 1) {
                val currentSegment = segments[i]
                val nextSegment = segments[i + 1]
                val layoverDuration = Duration.ofMillis(
                    nextSegment.departure.departsAt - currentSegment.arrival.arrivesAt
                )
                layovers.add(
                    Layover(
                        airportCode = currentSegment.arrival.airportCode,
                        airportName = "${currentSegment.arrival.airportCode} Airport",
                        duration = layoverDuration
                    )
                )
            }

            val totalDuration = Duration.ofMillis(
                segments.last().arrival.arrivesAt - segments.first().departure.departsAt
            )
            val totalTimeInAir = segments.sumOf { it.segmentDuration.toMinutes() }.let { Duration.ofMinutes(it) }
            val totalPrice = segments.sumOf { it.price.amount }.let {
                FlightPrice(amount = it, currency = segments.first().price.currency)
            }

            FlightJourney(
                totalDuration = totalDuration,
                totalTimeInAir = totalTimeInAir,
                price = totalPrice,
                segments = segments,
                layovers = layovers
            )
        }
    }
}