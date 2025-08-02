# Cred-Air - Airline Aggregation System

This project is an airline aggregation system that allows users to search for and book flights from multiple airlines.

## Modules

- `airline-onboarding`: Manages the onboarding of new airlines into the system.
- `flight-registration`: Manages the registration of new flights from onboarded airlines.
- `flight-search`: Provides functionality to search for flights based on various criteria.
- `flight-booking`: Handles the booking of flights.
- `common`: Contains shared code and data models used across all modules.

## Technology Stack

- **Language:** Kotlin
- **Framework:** Dropwizard
- **Build Tool:** Maven
- **Database:** PostgreSQL

## Database

For this project, we will use a free tier PostgreSQL database. A good option is [ElephantSQL](https://www.elephantsql.com/), which provides free PostgreSQL as a service.

## Design Principles

- **Modularity:** The system is divided into independent modules, each with a specific responsibility. This promotes separation of concerns and makes the system easier to maintain and scale.
- **Interfaces:** We will make extensive use of interfaces to promote loose coupling and plug-and-play architecture. This will allow us to easily swap out implementations without affecting other parts of the system.
