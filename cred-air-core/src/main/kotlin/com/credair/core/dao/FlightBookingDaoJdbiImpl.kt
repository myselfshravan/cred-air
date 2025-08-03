package com.credair.core.dao

import com.credair.core.dao.interfaces.FlightBookingDao
import com.credair.core.model.FlightBooking
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.LocalDateTime

@Singleton
class FlightBookingDaoJdbiImpl @Inject constructor(private val jdbi: Jdbi) : FlightBookingDao {

    private val flightBookingMapper = RowMapper { rs: ResultSet, _: StatementContext ->
        FlightBooking(
            id = rs.getLong("id"),
            bookingId = rs.getLong("booking_id"),
            flightId = rs.getString("flight_id"),
            pnr = rs.getString("pnr"),
            totalFlightPrice = rs.getBigDecimal("total_flight_price"),
            currency = rs.getString("currency"),
            passengerCount = rs.getInt("passenger_count"),
            bookingClass = rs.getString("booking_class"),
            status = rs.getString("status"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at")
        )
    }

    override fun save(entity: FlightBooking): FlightBooking {
        val newId = jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO flight_bookings (booking_id, flight_id, pnr, total_flight_price, currency,
                                           passenger_count, booking_class, status)
                VALUES (:bookingId, :flightId, :pnr, :totalFlightPrice, :currency,
                        :passengerCount, :bookingClass, :status)
            """)
                .bind("bookingId", entity.bookingId)
                .bind("flightId", entity.flightId)
                .bind("pnr", entity.pnr)
                .bind("totalFlightPrice", entity.totalFlightPrice)
                .bind("currency", entity.currency)
                .bind("passengerCount", entity.passengerCount)
                .bind("bookingClass", entity.bookingClass)
                .bind("status", entity.status)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long::class.java)
                .one()
        }
        return entity.copy(id = newId)
    }

    override fun update(entity: FlightBooking): FlightBooking {
        val now = LocalDateTime.now()
        jdbi.withHandle<Unit, Exception> { handle ->
            handle.createUpdate("""
                UPDATE flight_bookings 
                SET booking_id = :bookingId, flight_id = :flightId, pnr = :pnr,
                    total_flight_price = :totalFlightPrice, currency = :currency,
                    passenger_count = :passengerCount, booking_class = :bookingClass,
                    status = :status, updated_at = :updatedAt
                WHERE id = :id
            """)
                .bind("id", entity.id)
                .bind("bookingId", entity.bookingId)
                .bind("flightId", entity.flightId)
                .bind("pnr", entity.pnr)
                .bind("totalFlightPrice", entity.totalFlightPrice)
                .bind("currency", entity.currency)
                .bind("passengerCount", entity.passengerCount)
                .bind("bookingClass", entity.bookingClass)
                .bind("status", entity.status)
                .bind("updatedAt", now)
                .execute()
        }
        return entity.copy(updatedAt = java.sql.Timestamp.valueOf(now))
    }

    override fun findById(id: Long): FlightBooking? {
        return jdbi.withHandle<FlightBooking?, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_bookings WHERE id = :id")
                .bind("id", id)
                .map(flightBookingMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findAll(): List<FlightBooking> {
        return jdbi.withHandle<List<FlightBooking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_bookings ORDER BY created_at DESC")
                .map(flightBookingMapper)
                .list()
        }
    }

    override fun delete(id: Long): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM flight_bookings WHERE id = :id")
                .bind("id", id)
                .execute()
        } > 0
    }

    override fun findByBookingId(bookingId: Long): List<FlightBooking> {
        return jdbi.withHandle<List<FlightBooking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_bookings WHERE booking_id = :bookingId ORDER BY created_at DESC")
                .bind("bookingId", bookingId)
                .map(flightBookingMapper)
                .list()
        }
    }

    override fun findByFlightId(flightId: String): List<FlightBooking> {
        return jdbi.withHandle<List<FlightBooking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_bookings WHERE flight_id = :flightId ORDER BY created_at DESC")
                .bind("flightId", flightId)
                .map(flightBookingMapper)
                .list()
        }
    }

    override fun findByPnr(pnr: String): FlightBooking? {
        return jdbi.withHandle<FlightBooking?, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_bookings WHERE pnr = :pnr")
                .bind("pnr", pnr)
                .map(flightBookingMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findByBookingIdAndFlightId(bookingId: Long, flightId: String): FlightBooking? {
        return jdbi.withHandle<FlightBooking?, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_bookings WHERE booking_id = :bookingId AND flight_id = :flightId")
                .bind("bookingId", bookingId)
                .bind("flightId", flightId)
                .map(flightBookingMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun updateStatus(id: Long, status: String): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE flight_bookings SET status = :status, updated_at = :updatedAt WHERE id = :id")
                .bind("id", id)
                .bind("status", status)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }

    override fun updatePnr(id: Long, pnr: String): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE flight_bookings SET pnr = :pnr, updated_at = :updatedAt WHERE id = :id")
                .bind("id", id)
                .bind("pnr", pnr)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }
}