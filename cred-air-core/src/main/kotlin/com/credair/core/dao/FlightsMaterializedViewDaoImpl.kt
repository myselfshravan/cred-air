package com.credair.core.dao

import com.credair.core.dao.interfaces.FlightsMaterializedViewDao
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi

@Singleton
class FlightsMaterializedViewDaoImpl @Inject constructor(
    private val jdbi: Jdbi
) : FlightsMaterializedViewDao {

    override fun deleteJourneysContainingFlight(flightId: Long): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                DELETE FROM mv_flight_journeys 
                WHERE :flightId = ANY(path)
            """)
            .bind("flightId", flightId)
            .execute()
        }
    }

    override fun updateSeatsForJourneys(flightId: Long, newAvailableSeats: Int): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                UPDATE mv_flight_journeys 
                SET min_available_seats = (
                    SELECT MIN(f.available_seats)
                    FROM unnest(path) AS flight_id
                    JOIN flights f ON f.flight_id = flight_id
                    WHERE f.active = true
                )
                WHERE :flightId = ANY(path)
            """)
            .bind("flightId", flightId)
            .execute()
        }
    }

    override fun recomputeDirectFlights(flightId: Long): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO mv_flight_journeys (
                    src_airport_code, dest_airport_code, departure_date, departure_time, 
                    arrival_time, total_time_minutes, stops, path, total_cost, 
                    min_available_seats, airline_name, airline_logo_url, aircraft_type, flight_number
                )
                SELECT
                    f1.src_airport_code,
                    f1.dest_airport_code,
                    DATE(f1.departs_at) as departure_date,
                    f1.departs_at as departure_time,
                    f1.arrives_at as arrival_time,
                    EXTRACT(EPOCH FROM (f1.arrives_at - f1.departs_at))/60 as total_time_minutes,
                    0 AS stops,
                    ARRAY[f1.flight_id] AS path,
                    f1.price AS total_cost,
                    f1.available_seats AS min_available_seats,
                    a1.name AS airline_name,
                    a1.logo_url AS airline_logo_url,
                    f1.aircraft_type,
                    f1.flight_number
                FROM flights AS f1
                LEFT JOIN airlines AS a1 ON a1.id = f1.airline_id
                WHERE f1.flight_id = :flightId
                  AND f1.departs_at >= NOW()
                  AND f1.active = true
                  AND f1.available_seats > 0
                ON CONFLICT (src_airport_code, dest_airport_code, departure_date, departure_time, path) DO NOTHING
            """)
            .bind("flightId", flightId)
            .execute()
        }
    }

    override fun recomputeOneStopFlightsWithFlightAsFirst(flightId: Long): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO mv_flight_journeys (
                    src_airport_code, dest_airport_code, departure_date, departure_time, 
                    arrival_time, total_time_minutes, stops, path, total_cost, 
                    min_available_seats, airline_name, airline_logo_url, aircraft_type, flight_number
                )
                SELECT
                    f1.src_airport_code,
                    f2.dest_airport_code,
                    DATE(f1.departs_at) as departure_date,
                    f1.departs_at as departure_time,
                    f2.arrives_at as arrival_time,
                    EXTRACT(EPOCH FROM (f2.arrives_at - f1.departs_at))/60 as total_time_minutes,
                    1 AS stops,
                    ARRAY[f1.flight_id, f2.flight_id] AS path,
                    f1.price + f2.price AS total_cost,
                    LEAST(f1.available_seats, f2.available_seats) AS min_available_seats,
                    a1.name AS airline_name,
                    a1.logo_url AS airline_logo_url,
                    f1.aircraft_type,
                    f1.flight_number || ',' || f2.flight_number AS flight_number
                FROM flights AS f1
                JOIN flights AS f2 ON f1.dest_airport_code = f2.src_airport_code
                LEFT JOIN airlines AS a1 ON a1.id = f1.airline_id
                WHERE f1.flight_id = :flightId
                  AND f1.departs_at >= NOW()
                  AND f1.active = true
                  AND f2.active = true
                  AND f1.available_seats > 0
                  AND f2.available_seats > 0
                  AND f2.departs_at > f1.arrives_at + INTERVAL '45 minutes'
                  AND f2.departs_at < f1.arrives_at + INTERVAL '24 hours'
                ON CONFLICT (src_airport_code, dest_airport_code, departure_date, departure_time, path) DO NOTHING
            """)
            .bind("flightId", flightId)
            .execute()
        }
    }

    override fun recomputeOneStopFlightsWithFlightAsSecond(flightId: Long): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO mv_flight_journeys (
                    src_airport_code, dest_airport_code, departure_date, departure_time, 
                    arrival_time, total_time_minutes, stops, path, total_cost, 
                    min_available_seats, airline_name, airline_logo_url, aircraft_type, flight_number
                )
                SELECT
                    f1.src_airport_code,
                    f2.dest_airport_code,
                    DATE(f1.departs_at) as departure_date,
                    f1.departs_at as departure_time,
                    f2.arrives_at as arrival_time,
                    EXTRACT(EPOCH FROM (f2.arrives_at - f1.departs_at))/60 as total_time_minutes,
                    1 AS stops,
                    ARRAY[f1.flight_id, f2.flight_id] AS path,
                    f1.price + f2.price AS total_cost,
                    LEAST(f1.available_seats, f2.available_seats) AS min_available_seats,
                    a1.name AS airline_name,
                    a1.logo_url AS airline_logo_url,
                    f1.aircraft_type,
                    f1.flight_number || ',' || f2.flight_number AS flight_number
                FROM flights AS f1
                JOIN flights AS f2 ON f1.dest_airport_code = f2.src_airport_code
                LEFT JOIN airlines AS a1 ON a1.id = f1.airline_id
                WHERE f2.flight_id = :flightId
                  AND f1.departs_at >= NOW()
                  AND f1.active = true
                  AND f2.active = true
                  AND f1.available_seats > 0
                  AND f2.available_seats > 0
                  AND f2.departs_at > f1.arrives_at + INTERVAL '45 minutes'
                  AND f2.departs_at < f1.arrives_at + INTERVAL '24 hours'
                ON CONFLICT (src_airport_code, dest_airport_code, departure_date, departure_time, path) DO NOTHING
            """)
            .bind("flightId", flightId)
            .execute()
        }
    }

    override fun recomputeTwoStopFlightsContainingFlight(flightId: Long): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            // This is more complex as we need to handle cases where the flight is first, second, or third leg
            // For now, implementing a simplified version that recomputes based on flight connections
            var totalInserted = 0
            
            // Flight as first leg
            totalInserted += handle.createUpdate("""
                INSERT INTO mv_flight_journeys (
                    src_airport_code, dest_airport_code, departure_date, departure_time, 
                    arrival_time, total_time_minutes, stops, path, total_cost, 
                    min_available_seats, airline_name, airline_logo_url, aircraft_type, flight_number
                )
                SELECT
                    f1.src_airport_code,
                    f3.dest_airport_code,
                    DATE(f1.departs_at) as departure_date,
                    f1.departs_at as departure_time,
                    f3.arrives_at as arrival_time,
                    EXTRACT(EPOCH FROM (f3.arrives_at - f1.departs_at))/60 as total_time_minutes,
                    2 AS stops,
                    ARRAY[f1.flight_id, f2.flight_id, f3.flight_id] AS path,
                    f1.price + f2.price + f3.price AS total_cost,
                    LEAST(f1.available_seats, f2.available_seats, f3.available_seats) AS min_available_seats,
                    a1.name AS airline_name,
                    a1.logo_url AS airline_logo_url,
                    f1.aircraft_type,
                    f1.flight_number || ',' || f2.flight_number || ',' || f3.flight_number AS flight_number
                FROM flights AS f1
                JOIN flights AS f2 ON f1.dest_airport_code = f2.src_airport_code
                JOIN flights AS f3 ON f2.dest_airport_code = f3.src_airport_code
                LEFT JOIN airlines AS a1 ON a1.id = f1.airline_id
                WHERE f1.flight_id = :flightId
                  AND f1.departs_at >= NOW()
                  AND f1.active = true
                  AND f2.active = true
                  AND f3.active = true
                  AND f1.available_seats > 0
                  AND f2.available_seats > 0
                  AND f3.available_seats > 0
                  AND f2.departs_at > f1.arrives_at + INTERVAL '45 minutes'
                  AND f2.departs_at < f1.arrives_at + INTERVAL '24 hours'
                  AND f3.departs_at > f2.arrives_at + INTERVAL '45 minutes'
                  AND f3.departs_at < f2.arrives_at + INTERVAL '24 hours'
                ON CONFLICT (src_airport_code, dest_airport_code, departure_date, departure_time, path) DO NOTHING
            """)
            .bind("flightId", flightId)
            .execute()
            
            totalInserted
        }
    }

    override fun refreshFullMaterializedView() {
        jdbi.withHandle<Unit, Exception> { handle ->
            handle.createCall("CALL refresh_flight_journeys()").invoke()
        }
    }
}