package com.credair.core.dao

import com.credair.core.dao.interfaces.BookingDao
import com.credair.core.model.Booking
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
            totalPrice = rs.getBigDecimal("total_price"),
            currency = rs.getString("currency"),
            passengerCount = rs.getInt("passenger_count"),
            bookingStatus = rs.getString("booking_status"),
            paymentStatus = rs.getString("payment_status"),
            paymentMethod = rs.getString("payment_method"),
            paymentTransactionId = rs.getString("payment_transaction_id"),
            createdAt = rs.getTimestamp("created_at"),
            updatedAt = rs.getTimestamp("updated_at")
        )
    }

    override fun save(entity: Booking): Booking {
        val newId = jdbi.withHandle<Long, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO bookings (booking_reference, total_price, currency, passenger_count,
                                     booking_status, payment_status, payment_method, payment_transaction_id)
                VALUES (:bookingReference, :totalPrice, :currency, :passengerCount,
                        :bookingStatus, :paymentStatus, :paymentMethod, :paymentTransactionId)
            """)
                .bind("bookingReference", entity.bookingReference)
                .bind("totalPrice", entity.totalPrice)
                .bind("currency", entity.currency)
                .bind("passengerCount", entity.passengerCount)
                .bind("bookingStatus", entity.bookingStatus)
                .bind("paymentStatus", entity.paymentStatus)
                .bind("paymentMethod", entity.paymentMethod)
                .bind("paymentTransactionId", entity.paymentTransactionId)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long::class.java)
                .one()
        }
        return entity.copy(id = newId)
    }

    override fun update(entity: Booking): Booking {
        val now = LocalDateTime.now()
        jdbi.withHandle<Unit, Exception> { handle ->
            handle.createUpdate("""
                UPDATE bookings 
                SET booking_reference = :bookingReference, total_price = :totalPrice,
                    currency = :currency, passenger_count = :passengerCount,
                    booking_status = :bookingStatus, payment_status = :paymentStatus,
                    payment_method = :paymentMethod, payment_transaction_id = :paymentTransactionId,
                    updated_at = :updatedAt
                WHERE id = :id
            """)
                .bind("id", entity.id)
                .bind("bookingReference", entity.bookingReference)
                .bind("totalPrice", entity.totalPrice)
                .bind("currency", entity.currency)
                .bind("passengerCount", entity.passengerCount)
                .bind("bookingStatus", entity.bookingStatus)
                .bind("paymentStatus", entity.paymentStatus)
                .bind("paymentMethod", entity.paymentMethod)
                .bind("paymentTransactionId", entity.paymentTransactionId)
                .bind("updatedAt", now)
                .execute()
        }
        return entity.copy(updatedAt = java.sql.Timestamp.valueOf(now))
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

    override fun findByBookingStatus(status: String): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE booking_status = :status ORDER BY created_at DESC")
                .bind("status", status)
                .map(bookingMapper)
                .list()
        }
    }

    override fun findByPaymentStatus(status: String): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE payment_status = :status ORDER BY created_at DESC")
                .bind("status", status)
                .map(bookingMapper)
                .list()
        }
    }

    override fun findByCreatedDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Booking> {
        return jdbi.withHandle<List<Booking>, Exception> { handle ->
            handle.createQuery("SELECT * FROM bookings WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC")
                .bind("startDate", startDate)
                .bind("endDate", endDate)
                .map(bookingMapper)
                .list()
        }
    }

    override fun updateBookingStatus(bookingId: Long, status: String): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE bookings SET booking_status = :status, updated_at = :updatedAt WHERE id = :id")
                .bind("id", bookingId)
                .bind("status", status)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }

    override fun updatePaymentStatus(bookingId: Long, status: String): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE bookings SET payment_status = :status, updated_at = :updatedAt WHERE id = :id")
                .bind("id", bookingId)
                .bind("status", status)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }

    override fun updatePaymentTransactionId(bookingId: Long, transactionId: String): Boolean {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("UPDATE bookings SET payment_transaction_id = :transactionId, updated_at = :updatedAt WHERE id = :id")
                .bind("id", bookingId)
                .bind("transactionId", transactionId)
                .bind("updatedAt", LocalDateTime.now())
                .execute()
        } > 0
    }
}