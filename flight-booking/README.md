# Flight Booking Module

This module is responsible for booking flights.

## Implementation Plan

- Define a data class for Booking.
- Create a DAO to interact with the database.
- Implement a service to book flights.
- Implement a resource to expose the following endpoints:
  - `POST /bookings`: Book a flight.
  - `GET /bookings`: Get a list of all bookings.
  - `GET /bookings/{id}`: Get details of a specific booking.
