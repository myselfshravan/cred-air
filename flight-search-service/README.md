# Flight Search Module

This module is responsible for searching for flights based on user-provided criteria.

## Running the Application

### IntelliJ IDEA
1. Create a run configuration for `com.credair.flight.search.FlightSearchApplication`
2. Set **Working directory** to: `flight-search-service` (or absolute path to this module)
3. Add program arguments: `server src/main/resources/server-config.yml`
4. Run the application

### Command Line
```bash
mvn clean install
java -cp target/classes:target/dependency/* com.credair.flight.search.FlightSearchApplication server src/main/resources/server-config.yml
```

The application will start on:
- Main server: http://localhost:8084
- Admin interface: http://localhost:8085

## Implementation Plan

- Define a data class for SearchCriteria.
- Implement a service to search for flights based on the following criteria:
  - Source
  - Destination
  - Date
  - Number of seats
  - Direct flights only or with layovers
- Implement a resource to expose the following endpoint:
  - `GET /flights/search`: Search for flights.
- The search results should be sortable by cheapest and fastest routes.
