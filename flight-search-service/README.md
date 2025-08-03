# Flight Search Service

This service is responsible for providing flight search functionality to users.

## Design Choices

- **Framework**: Built with [Dropwizard](https://www.dropwizard.io/), a lightweight framework for building high-performance, RESTful web services.
- **Language**: Written in [Kotlin](https://kotlinlang.org/), a modern, concise, and safe programming language.
- **Dependency Injection**: Uses [Google Guice](https://github.com/google/guice) for managing dependencies, promoting loose coupling and testability.

## Layers of Responsibility

The service follows a layered architecture to separate concerns:

```mermaid
graph TD
    subgraph "Flight Search Service"
        A[Resource Layer] --> B[Manager Layer]
        B --> C[Core Module]
    end

    subgraph "cred-air-core"
        C --> D[DAO Layer]
        D --> E[(Database)]
    end
```

-   **Resource Layer** (`com.credair.flight.search.resource`): This layer is responsible for handling incoming HTTP requests and exposing RESTful endpoints for flight searches. It delegates the business logic to the manager layer.
-   **Manager Layer** (`com.credair.flight.search.managers`): This layer contains the core business logic for searching flights. It orchestrates calls to the `cred-air-core` module to query the database and aggregate results.
-   **Core Module** (`cred-air-core`): This is a shared module that contains the Data Access Objects (DAOs), data models, and other shared business logic.

## Principles Followed

-   **Separation of Concerns**: Each layer has a distinct responsibility, making the codebase easier to understand, maintain, and test.
-   **Dependency Injection**: By using Guice, we avoid tight coupling between components and can easily swap out implementations.
-   **Interface-based Design**: The service relies on interfaces for its core components (e.g., DAOs), allowing for multiple implementations and easier testing.
-   **Materialized Views**: The service utilizes a materialized view (`flights_materialized_view`) to optimize search performance by pre-aggregating flight data.