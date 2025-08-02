-- Create flights table
CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    airline_id BIGINT NOT NULL,
    flight_number VARCHAR(20) NOT NULL,
    source_airport VARCHAR(10) NOT NULL,
    destination_airport VARCHAR(10) NOT NULL,
    departure_time TIMESTAMP WITH TIME ZONE NOT NULL,
    arrival_time TIMESTAMP WITH TIME ZONE NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    aircraft_type VARCHAR(50),
    is_direct BOOLEAN NOT NULL DEFAULT true,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_flights_airline_id FOREIGN KEY (airline_id) REFERENCES airlines(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_flights_airline_id ON flights(airline_id);
CREATE INDEX idx_flights_flight_number ON flights(flight_number);
CREATE INDEX idx_flights_route ON flights(source_airport, destination_airport);
CREATE INDEX idx_flights_departure_time ON flights(departure_time);
CREATE INDEX idx_flights_arrival_time ON flights(arrival_time);
CREATE INDEX idx_flights_active ON flights(active);
CREATE INDEX idx_flights_available_seats ON flights(available_seats);
CREATE INDEX idx_flights_price ON flights(price);

-- Create composite indexes for common queries
CREATE INDEX idx_flights_route_date ON flights(source_airport, destination_airport, DATE(departure_time));
CREATE INDEX idx_flights_available ON flights(active, available_seats, departure_time) WHERE active = true AND available_seats > 0;

-- Add constraints
ALTER TABLE flights ADD CONSTRAINT chk_flights_departure_before_arrival CHECK (departure_time < arrival_time);
ALTER TABLE flights ADD CONSTRAINT chk_flights_price_positive CHECK (price > 0);
ALTER TABLE flights ADD CONSTRAINT chk_flights_total_seats_positive CHECK (total_seats > 0);
ALTER TABLE flights ADD CONSTRAINT chk_flights_available_seats_non_negative CHECK (available_seats >= 0);
ALTER TABLE flights ADD CONSTRAINT chk_flights_available_seats_lte_total CHECK (available_seats <= total_seats);
ALTER TABLE flights ADD CONSTRAINT chk_flights_airports_different CHECK (source_airport != destination_airport);
ALTER TABLE flights ADD CONSTRAINT chk_flights_flight_number_not_empty CHECK (LENGTH(TRIM(flight_number)) > 0);

-- Create unique constraint for airline + flight number + departure time (prevents duplicate flights)
CREATE UNIQUE INDEX idx_flights_unique_flight ON flights(airline_id, flight_number, DATE(departure_time));