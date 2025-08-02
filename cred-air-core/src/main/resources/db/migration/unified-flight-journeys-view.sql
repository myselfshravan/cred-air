
-- Create unified materialized view for all flight journeys
CREATE MATERIALIZED VIEW mv_flight_journeys AS
SELECT
    src_airport_code,
    dest_airport_code,
    DATE(departs_at) as departure_date,
    departs_at as departure_time,
    arrives_at as arrival_time,
    EXTRACT(EPOCH FROM (arrives_at - departs_at))/60 as total_time_minutes,
    stops,
    layover_points,
    total_cost,
    min_available_seats,
    airline_name,
    airline_logo_url,
    aircraft_type,
    flight_number
FROM (
    -- 0-Stop Flights (Direct)
    SELECT
        f1.src_airport_code,
        f1.dest_airport_code,
        f1.departs_at,
        f1.arrives_at,
        0 AS stops,
        ARRAY[]::text[] AS layover_points,
        f1.price AS total_cost,
        f1.available_seats AS min_available_seats,
        a1.name AS airline_name,
        a1.logo_url AS airline_logo_url,
        f1.aircraft_type,
        ARRAY[f1.flight_id] AS flight_number
    FROM flights AS f1
    LEFT JOIN airlines AS a1 ON a1.id = f1.airline_id
    WHERE f1.departs_at >= NOW()
      AND f1.active = true
      AND f1.available_seats > 0

    UNION ALL

    -- 1-Stop Flights
    SELECT
        f1.src_airport_code,
        f2.dest_airport_code,
        f1.departs_at,
        f2.arrives_at,
        1 AS stops,
        ARRAY[f1.dest_airport_code] AS layover_points,
        f1.price + f2.price AS total_cost,
        LEAST(f1.available_seats, f2.available_seats) AS min_available_seats,
        a1.name AS airline_name,
        a1.logo_url AS airline_logo_url,
        f1.aircraft_type,
        ARRAY[f1.flight_id, f2.flight_id] AS flight_number
    FROM flights AS f1
    JOIN flights AS f2 ON f1.dest_airport_code = f2.src_airport_code
    LEFT JOIN airlines AS a1 ON a1.id = f1.airline_id
    WHERE
        f1.departs_at >= NOW()
        AND f1.active = true
        AND f2.active = true
        AND f1.available_seats > 0
        AND f2.available_seats > 0
        AND f2.departs_at > f1.arrives_at + INTERVAL '45 minutes'
        AND f2.departs_at < f1.arrives_at + INTERVAL '24 hours'
        AND f1.dest_airport_code != f1.src_airport_code  -- Prevent immediate return to source
        AND f2.dest_airport_code != f1.src_airport_code  -- Prevent final destination being source

    UNION ALL

    -- 2-Stop Flights
    SELECT
        f1.src_airport_code,
        f3.dest_airport_code,
        f1.departs_at,
        f3.arrives_at,
        2 AS stops,
        ARRAY[f1.dest_airport_code, f2.dest_airport_code] AS layover_points,
        f1.price + f2.price + f3.price AS total_cost,
        LEAST(f1.available_seats, f2.available_seats, f3.available_seats) AS min_available_seats,
        a1.name AS airline_name,
        a1.logo_url AS airline_logo_url,
        f1.aircraft_type,
        ARRAY[f1.flight_id, f2.flight_id, f3.flight_id] AS flight_number
    FROM flights AS f1
    JOIN flights AS f2 ON f1.dest_airport_code = f2.src_airport_code
    JOIN flights AS f3 ON f2.dest_airport_code = f3.src_airport_code
    LEFT JOIN airlines AS a1 ON a1.id = f1.airline_id
    WHERE
        f1.departs_at >= NOW()
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
        AND f1.dest_airport_code != f1.src_airport_code  -- Prevent immediate return to source
        AND f2.dest_airport_code != f1.src_airport_code  -- Prevent return to source at second stop
        AND f2.dest_airport_code != f1.dest_airport_code  -- Prevent revisiting first layover
        AND f3.dest_airport_code != f1.src_airport_code  -- Prevent final destination being source
        AND f3.dest_airport_code != f1.dest_airport_code  -- Prevent final destination being first layover
) journey_data
WITH DATA;

-- Create optimized indexes for query pattern: src_airport, dest_airport, departure_date, available_seats
CREATE UNIQUE INDEX idx_flight_journeys_primary 
ON mv_flight_journeys (src_airport_code, dest_airport_code, departure_date, departure_time, layover_points);

-- Index for filtering by available seats
CREATE INDEX idx_flight_journeys_seats 
ON mv_flight_journeys (src_airport_code, dest_airport_code, departure_date, min_available_seats);

-- Indexes for sorting by cost and time
CREATE INDEX idx_flight_journeys_cost_sort 
ON mv_flight_journeys (src_airport_code, dest_airport_code, departure_date, total_cost, total_time_minutes);

CREATE INDEX idx_flight_journeys_time_sort 
ON mv_flight_journeys (src_airport_code, dest_airport_code, departure_date, total_time_minutes, total_cost);

-- Covering index to avoid table lookups for common queries
CREATE INDEX idx_flight_journeys_covering 
ON mv_flight_journeys (src_airport_code, dest_airport_code, departure_date, min_available_seats) 
INCLUDE (departure_time, arrival_time, total_cost, total_time_minutes, stops, airline_name);

-- Update the refresh procedure to use the unified view
CREATE OR REPLACE PROCEDURE refresh_flight_journeys()
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE NOTICE 'Refreshing mv_flight_journeys...';
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_flight_journeys;
    RAISE NOTICE 'Unified flight journeys view refreshed successfully.';
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Flight journeys materialized view refresh failed: %', SQLERRM;
END;
$$;

-- Update the scheduled job to use the new procedure
SELECT cron.unschedule('refresh-flight-views');
SELECT cron.schedule('refresh-flight-journeys', '*/15 * * * *', 'CALL refresh_flight_journeys();');