# Airline Management Module

This module is responsible for managing airlines and their flights.

## Implementation Plan

### Airline Management

- Define a data class for Airline.
- Create a DAO to interact with the database.
- Implement a resource to expose the following endpoints:
  - `POST /airlines`: Register a new airline.
  - `GET /airlines`: Get a list of all airlines.
  - `GET /airlines/{id}`: Get details of a specific airline.
  - `PUT /airlines/{id}`: Update details of a specific airline.
  - `DELETE /airlines/{id}`: Delete an airline.

### Flight Management

- Define a data class for Flight.
- Create a DAO to interact with the database.
- Implement a resource to expose the following endpoints, which will be nested under an airline:
  - `POST /airlines/{airlineId}/flights`: Register a new flight for a specific airline.
  - `GET /airlines/{airlineId}/flights`: Get a list of all flights for a specific airline.
  - `GET /airlines/{airlineId}/flights/{flightId}`: Get details of a specific flight for a specific airline.
  - `PUT /airlines/{airlineId}/flights/{flightId}`: Update details of a specific flight for a specific airline.
  - `DELETE /airlines/{airlineId}/flights/{flightId}`: Delete a flight for a specific airline.
