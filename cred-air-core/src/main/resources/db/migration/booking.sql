-- This script creates three tables for a flight booking system,
-- all formatted for PostgreSQL with the requested corrections.
--
-- Key changes:
-- - All UUID types have been replaced with BIGSERIAL or BIGINT.
-- - All VARCHAR types have been replaced with TEXT or CHAR.
-- - ENUM types have been replaced with TEXT and a CHECK constraint.
-- - All FOREIGN KEY constraints have been removed as requested.

-- Table 1: Main booking record (payment level)
CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          booking_reference TEXT UNIQUE NOT NULL,
                          total_price DECIMAL(10,2) NOT NULL,
                          currency CHAR(3) NOT NULL,
                          passenger_count INTEGER NOT NULL,
                          booking_status TEXT NOT NULL ,
                          payment_status TEXT NOT NULL ,
                          payment_method TEXT,
                          payment_transaction_id TEXT,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: Individual flight bookings (PNR level)
CREATE TABLE flight_bookings (
                                 id BIGSERIAL PRIMARY KEY,
                                 booking_id BIGINT NOT NULL,
                                 flight_id TEXT NOT NULL,
                                 pnr TEXT UNIQUE,
                                 total_flight_price DECIMAL(10,2) NOT NULL,
                                 currency CHAR(3) NOT NULL,
                                 passenger_count INTEGER NOT NULL,
                                 booking_class TEXT,
                                 status TEXT NOT NULL ,
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Unique constraint to ensure one booking per flight per main booking
                                 UNIQUE (flight_id, booking_id)
);

-- Table 3: Passengers for each flight booking
CREATE TABLE flight_passengers (
                                   id BIGSERIAL PRIMARY KEY,
                                   flight_booking_id BIGINT NOT NULL,
                                   passenger_external_id TEXT,
                                   title TEXT,
                                   first_name TEXT NOT NULL,
                                   last_name TEXT NOT NULL,
                                   date_of_birth DATE NOT NULL,
                                   email TEXT NOT NULL,
                                   phone TEXT,
                                   seat_number TEXT,
                                   ticket_number TEXT,
                                   individual_price DECIMAL(10,2) NOT NULL,
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Unique constraint to ensure one record per passenger per flight booking
                                   UNIQUE (flight_booking_id, passenger_external_id)
);

-- Indexes for performance
CREATE INDEX idx_bookings_reference ON bookings(booking_reference);
CREATE INDEX idx_bookings_status ON bookings(booking_status);
CREATE INDEX idx_flight_bookings_booking_id ON flight_bookings(booking_id);
CREATE INDEX idx_flight_bookings_flight_id ON flight_bookings(flight_id);
CREATE INDEX idx_flight_bookings_pnr ON flight_bookings(pnr);
CREATE INDEX idx_flight_passengers_flight_booking_id ON flight_passengers(flight_booking_id);
CREATE INDEX idx_flight_passengers_email ON flight_passengers(email);
