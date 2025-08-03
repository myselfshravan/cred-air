#!/bin/bash

# Build all modules
mvn clean install

# Run each service in the background
mvn -pl airline-mgt-service exec:java -Dexec.args="server airline-mgt-service/src/main/resources/server-config.yml" &
mvn -pl flight-search-service exec:java -Dexec.args="server flight-search-service/src/main/resources/server-config.yml" &
mvn -pl flight-booking-service exec:java -Dexec.args="server flight-booking-service/src/main/resources/server-config.yml" &

# Wait for all background processes to finish
wait