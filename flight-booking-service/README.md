# Flight Booking Module

This module is responsible for booking flights.

## Running the Application

### IntelliJ IDEA
1. Create a run configuration for `com.credair.booking.BookingApplication`
2. Add program arguments: `server src/main/resources/server-config.yml`
3. Run the application

### Command Line
```bash
mvn clean install
java -cp target/classes:target/dependency/* com.credair.booking.BookingApplication server src/main/resources/server-config.yml
```

The application will start on:
- Main server: http://localhost:8080
- Admin interface: http://localhost:8081

## Implementation Plan

- Define a data class for Booking.
- Create a DAO to interact with the database.
- Implement a service to book flights.
- Implement a resource to expose the following endpoints:
  - `POST /bookings`: Book a flight.
  - `GET /bookings`: Get a list of all bookings.
  - `GET /bookings/{id}`: Get details of a specific booking.
