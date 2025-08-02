package com.credair.core.dao

import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.model.Booking
import com.credair.core.model.BookingStatus
import com.credair.core.model.PaymentStatus
import com.credair.core.model.CheckInStatus
import com.google.inject.Inject
import com.google.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.time.LocalDateTime

@Singleton
class BookingDaoJdbiImpl @Inject constructor(private val jdbi: Jdbi) : BookingDao {

    private val bookingMapper = RowMapper { rs: ResultSet, _: StatementContext ->
        Booking(
            id = rs.getLong("id"),
            bookingReference = rs.getString("booking_reference"),
            flightId = rs.getLong("flight_id"),
            passengerName = rs.getString("passenger_name"),
            passengerEmail = rs.getString("passenger_email"),
            passengerPhone = rs.getString("passenger_phone"),
            seatNumber = rs.getString("seat_number"),
            numberOfSeats = rs.getInt("number_of_seats"),
            totalPrice = rs.getBigDecimal("total_price"),
            currency = rs.getString("currency"),
            bookingStatus = BookingStatus.valueOf(rs.getString("booking_status")),
            paymentStatus = PaymentStatus.valueOf(rs.getString("payment_status")),
            bookingDate = rs.getTimestamp("booking_date").toLocalDateTime(),
            paymentMethod = rs.getString("payment_method"),
            specialRequests = rs.getString("special_requests"),
            checkInStatus = CheckInStatus.valueOf(rs.getString("check_in_status")),
            createdAt = rs.getTimestamp("created_at")?.toLocalDateTime(),
            updatedAt = rs.getTimestamp("updated_at")?.toLocalDateTime()
        )
    }

    override fun save(entity: Booking): Booking {
        val now = LocalDateTime.now()
        val newId = jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO bookings (booking_reference, flight_id, passenger_name, passenger_email, 
                                     passenger_phone, seat_number, number_of_seats, total_price, currency,
                                     booking_status, payment_status, booking_date, payment_method, 
                                     special_requests, check_in_status, created_at, updated_at)
                VALUES (:bookingReference, :flightId, :passengerName, :passengerEmail, 
                        :passengerPhone, :seatNumber, :numberOfSeats, :totalPrice, :currency,
                        :bookingStatus, :paymentStatus, :bookingDate, :paymentMethod, 
                        :specialRequests, :checkInStatus, :createdAt, :updatedAt)
            """)
                .bind("bookingReference", entity.bookingReference)
                .bind("flightId", entity.flightId)
                .bind("passengerName", entity.passengerName)
                .bind("passengerEmail", entity.passengerEmail)
                .bind("passengerPhone", entity.passengerPhone)
                .bind("seatNumber", entity.seatNumber)
                .bind("numberOfSeats", entity.numberOfSeats)
                .bind("totalPrice", entity.totalPrice)
                .bind("currency", entity.currency)
                .bind("bookingStatus", entity.bookingStatus.name)
                .bind("paymentStatus", entity.paymentStatus.name)
                .bind("bookingDate", entity.bookingDate)
                .bind("paymentMethod", entity.paymentMethod)
                .bind("specialRequests", entity.specialRequests)
                .bind("checkInStatus", entity.checkInStatus.name)
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long::class.java)
                .one()
        }
        return entity.copy(id = newId, createdAt = now, updatedAt = now)
    }

    override fun update(entity: Booking): Booking {
        val now = LocalDateTime.now()
        jdbi.withHandle<Unit, Exception> { handle ->
            handle.createUpdate("""
                UPDATE bookings 
                SET booking_reference = :bookingReference, flight_id = :flightId, 
                    passenger_name = :passengerName, passenger_email = :passengerEmail,
                    passenger_phone = :passengerPhone, seat_number = :seatNumber,
                    number_of_seats = :numberOfSeats, total_price = :totalPrice,
                    currency = :currency, booking_status = :bookingStatus,
                    payment_status = :paymentStatus, booking_date = :bookingDate,
                    payment_method = :paymentMethod, special_requests = :specialRequests,
                    check_in_status = :checkInStatus, updated_at = :updatedAt
                WHERE id = :id
            """)
                .bind("id", entity.id)
                .bind("bookingReference", entity.bookingReference)
                .bind("flightId", entity.flightId)
                .bind("passengerName", entity.passengerName)
                .bind("passengerEmail", entity.passengerEmail)
                .bind("passengerPhone", entity.passengerPhone)
                .bind("seatNumber", entity.seatNumber)
                .bind("numberOfSeats", entity.numberOfSeats)
                .bind("totalPrice", entity.totalPrice)
                .bind("currency", entity.currency)
                .bind("bookingStatus", entity.bookingStatus.name)
                .bind("paymentStatus", entity.paymentStatus.name)
                .bind("bookingDate", entity.bookingDate)
                .bind("paymentMethod", entity.paymentMethod)
                .bind("specialRequests", entity.specialRequests)
                .bind("checkInStatus", entity.checkInStatus.name)
                .bind("updatedAt", now)
                .execute()
        }
        return entity.copy(updatedAt = now)
    }

    override fun findById(id: Long): Booking? {
        return jdbi.withHandle<Booking?, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE id = :id")
                .bind("id", id)
                .map(bookingMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findAll(): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings ORDER BY created_at DESC")
                .map(bookingMapper)
                .list()
        }
    }

    override fun delete(id: Long): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM bookings WHERE id = :id")
                .bind("id", id)
                .execute()
        } > 0
    }

    override fun findByBookingReference(bookingReference: String): Booking? {
        return jdbi.withHandle<Booking?, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE booking_reference = :bookingReference")
                .bind("bookingReference", bookingReference)
                .map(bookingMapper)
                .findFirst()
                .orElse(null)
        }
    }

    override fun findByFlightId(flightId: Long): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE flight_id = :flightId ORDER BY booking_date DESC")
                .bind("flightId", flightId)
                .map(bookingMapper)
                .list()
        }
    }

    override fun findByPassengerEmail(passengerEmail: String): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE passenger_email = :passengerEmail ORDER BY booking_date DESC")
                .bind("passengerEmail", passengerEmail)
                .map(bookingMapper)
                .list()
        }
    }

    override fun findByPassengerEmailAndFlightId(passengerEmail: String, flightId: Long): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE passenger_email = :passengerEmail AND flight_id = :flightId ORDER BY booking_date DESC")
                .bind("passengerEmail", passengerEmail)
                .bind("flightId", flightId)
                .map(bookingMapper)
                .list()
        }
    }

    override fun findByBookingStatus(status: BookingStatus): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE booking_status = :status ORDER BY booking_date DESC")
                .bind("status", status.name)
                .map(bookingMapper)
                .list()
        }
    }

    override fun findByPaymentStatus(status: PaymentStatus): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE payment_status = :status ORDER BY booking_date DESC")
                .bind("status", status.name)
                .map(bookingMapper)
                .list()
        }
    }

    override fun findByBookingDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE booking_date BETWEEN :startDate AND :endDate ORDER BY booking_date DESC")
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .map(bookingMapper)
                .list()
        }
    }

    override fun findActiveBookings(): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE booking_status IN ('PENDING', 'CONFIRMED') ORDER BY booking_date DESC")
                .map(bookingMapper)
                .list()
        }
    }

    override fun updateBookingStatus(bookingId: Long, status: BookingStatus): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE bookings SET booking_status = :status, updated_at = :updatedAt WHERE id = :id")
                .bind("id", bookingId)
                .bind("status", status.name)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }

    override fun updatePaymentStatus(bookingId: Long, status: PaymentStatus): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE bookings SET payment_status = :status, updated_at = :updatedAt WHERE id = :id")
                .bind("id", bookingId)
                .bind("status", status.name)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }

    override fun updateCheckInStatus(bookingId: Long, checkInStatus: CheckInStatus): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE bookings SET check_in_status = :status, updated_at = :updatedAt WHERE id = :id")
                .bind("id", bookingId)
                .bind("status", checkInStatus.name)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }

    override fun countBookingsByFlightId(flightId: Long): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createQuery("SELECT COUNT(*) FROM bookings WHERE flight_id = :flightId AND booking_status IN ('PENDING', 'CONFIRMED')")
                .bind("flightId", flightId)
                .mapTo(Int::class.java)
                .one()
        }
    }

    override fun getTotalSeatsBookedForFlight(flightId: Long): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createQuery("SELECT COALESCE(SUM(number_of_seats), 0) FROM bookings WHERE flight_id = :flightId AND booking_status IN ('PENDING', 'CONFIRMED')")
                .bind("flightId", flightId)
                .mapTo(Int::class.java)
                .one()
        }
    }
}