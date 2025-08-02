# Flight Search Module

This module is responsible for searching for flights based on user-provided criteria.

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
