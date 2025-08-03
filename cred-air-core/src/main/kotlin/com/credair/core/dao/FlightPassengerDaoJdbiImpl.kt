package com.credair.core.dao

import com.credair.core.dao.interfaces.FlightPassengerDao
import com.credair.core.model.FlightPassenger
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.LocalDateTime

@Singleton
class FlightPassengerDaoJdbiImpl @Inject constructor(private val jdbi: Jdbi) : FlightPassengerDao {

    private val flightPassengerMapper = RowMapper { rs: ResultSet, _: StatementContext ->
        FlightPassenger(
            id = rs.getLong("id"),
            flightBookingId = rs.getLong("flight_booking_id"),
            passengerExternalId = rs.getString("passenger_external_id"),
            title = rs.getString("title"),
            firstName = rs.getString("first_name"),
            lastName = rs.getString("last_name"),
            dateOfBirth = rs.getDate("date_of_birth").toLocalDate(),
            email = rs.getString("email"),
            phone = rs.getString("phone"),
            seatNumber = rs.getString("seat_number"),
            ticketNumber = rs.getString("ticket_number"),
            individualPrice = rs.getBigDecimal("individual_price"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at")
        )
    }

    override fun save(entity: FlightPassenger): FlightPassenger {
        val newId = jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO flight_passengers (flight_booking_id, passenger_external_id, title, first_name,
                                              last_name, date_of_birth, email, phone, seat_number,
                                              ticket_number, individual_price)
                VALUES (:flightBookingId, :passengerExternalId, :title, :firstName,
                        :lastName, :dateOfBirth, :email, :phone, :seatNumber,
                        :ticketNumber, :individualPrice)
            """)
                .bind("flightBookingId", entity.flightBookingId)
                .bind("passengerExternalId", entity.passengerExternalId)
                .bind("title", entity.title)
                .bind("firstName", entity.firstName)
                .bind("lastName", entity.lastName)
                .bind("dateOfBirth", entity.dateOfBirth)
                .bind("email", entity.email)
                .bind("phone", entity.phone)
                .bind("seatNumber", entity.seatNumber)
                .bind("ticketNumber", entity.ticketNumber)
                .bind("individualPrice", entity.individualPrice)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long::class.java)
                .one()
        }
        return entity.copy(id = newId)
    }

    override fun update(entity: FlightPassenger): FlightPassenger {
        val now = LocalDateTime.now()
        jdbi.withHandle<Unit, Exception> { handle ->
            handle.createUpdate("""
                UPDATE flight_passengers 
                SET flight_booking_id = :flightBookingId, passenger_external_id = :passengerExternalId,
                    title = :title, first_name = :firstName, last_name = :lastName,
                    date_of_birth = :dateOfBirth, email = :email, phone = :phone,
                    seat_number = :seatNumber, ticket_number = :ticketNumber,
                    individual_price = :individualPrice, updated_at = :updatedAt
                WHERE id = :id
            """)
                .bind("id", entity.id)
                .bind("flightBookingId", entity.flightBookingId)
                .bind("passengerExternalId", entity.passengerExternalId)
                .bind("title", entity.title)
                .bind("firstName", entity.firstName)
                .bind("lastName", entity.lastName)
                .bind("dateOfBirth", entity.dateOfBirth)
                .bind("email", entity.email)
                .bind("phone", entity.phone)
                .bind("seatNumber", entity.seatNumber)
                .bind("ticketNumber", entity.ticketNumber)
                .bind("individualPrice", entity.individualPrice)
                .bind("updatedAt", now)
                .execute()
        }
        return entity.copy(updatedAt = java.sql.Timestamp.valueOf(now))
    }

    override fun findById(id: Long): FlightPassenger? {
        return jdbi.withHandle<FlightPassenger?, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_passengers WHERE id = :id")
                .bind("id", id)
                .map(flightPassengerMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findAll(): List<FlightPassenger> {
        return jdbi.withHandle<List<FlightPassenger>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_passengers ORDER BY created_at DESC")
                .map(flightPassengerMapper)
                .list()
        }
    }

    override fun delete(id: Long): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM flight_passengers WHERE id = :id")
                .bind("id", id)
                .execute()
        } > 0
    }

    override fun findByFlightBookingId(flightBookingId: Long): List<FlightPassenger> {
        return jdbi.withHandle<List<FlightPassenger>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_passengers WHERE flight_booking_id = :flightBookingId ORDER BY created_at DESC")
                .bind("flightBookingId", flightBookingId)
                .map(flightPassengerMapper)
                .list()
        }
    }

    override fun findByEmail(email: String): List<FlightPassenger> {
        return jdbi.withHandle<List<FlightPassenger>, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_passengers WHERE email = :email ORDER BY created_at DESC")
                .bind("email", email)
                .map(flightPassengerMapper)
                .list()
        }
    }

    override fun findByExternalId(externalId: String): FlightPassenger? {
        return jdbi.withHandle<FlightPassenger?, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_passengers WHERE passenger_external_id = :externalId")
                .bind("externalId", externalId)
                .map(flightPassengerMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findByFlightBookingIdAndExternalId(flightBookingId: Long, externalId: String): FlightPassenger? {
        return jdbi.withHandle<FlightPassenger?, Exception> { handle ->
            handle.createQuery("SELECT * FROM flight_passengers WHERE flight_booking_id = :flightBookingId AND passenger_external_id = :externalId")
                .bind("flightBookingId", flightBookingId)
                .bind("externalId", externalId)
                .map(flightPassengerMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun updateSeatNumber(id: Long, seatNumber: String): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE flight_passengers SET seat_number = :seatNumber, updated_at = :updatedAt WHERE id = :id")
                .bind("id", id)
                .bind("seatNumber", seatNumber)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }

    override fun updateTicketNumber(id: Long, ticketNumber: String): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE flight_passengers SET ticket_number = :ticketNumber, updated_at = :updatedAt WHERE id = :id")
                .bind("id", id)
                .bind("ticketNumber", ticketNumber)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }
}