package com.credair.core.services

import com.credair.core.dao.interfaces.FlightsMaterializedViewDao
import com.credair.core.events.*
import com.google.inject.Inject
import com.google.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class FlightsMaterializedViewManager @Inject constructor(
    private val flightsMaterializedViewDao: FlightsMaterializedViewDao
) {

    companion object {
        private val logger = LoggerFactory.getLogger(FlightsMaterializedViewManager::class.java)
    }

    fun handleScheduleChange(event: FlightScheduleChangedEvent) {
        try {
            logger.info("Processing schedule change for flight ${event.flightId}")
            
            // Delete all journeys containing this flight as schedule change affects connections
            val deletedCount = flightsMaterializedViewDao.deleteJourneysContainingFlight(event.flightId)
            logger.info("Deleted $deletedCount journey records due to schedule change")

            // Recompute all journey types for this flight
            recomputeAllJourneysForFlight(event.flightId)
            
            logger.info("Successfully processed schedule change for flight ${event.flightId}")
        } catch (e: Exception) {
            logger.error("Failed to process schedule change for flight ${event.flightId}", e)
            throw e
        }
    }

    fun handleSeatsChange(event: FlightSeatsChangedEvent) {
        try {
            logger.info("Processing seats change for flight ${event.flightId}: ${event.oldAvailableSeats} -> ${event.newAvailableSeats}")
            
            // Update min_available_seats for all journeys containing this flight
            val updatedCount = flightsMaterializedViewDao.updateSeatsForJourneys(event.flightId, event.newAvailableSeats)
            
            logger.info("Updated $updatedCount journey records due to seat change for flight ${event.flightId}")
        } catch (e: Exception) {
            logger.error("Failed to process seats change for flight ${event.flightId}", e)
            throw e
        }
    }

    fun handleFlightCancellation(event: FlightCancelledEvent) {
        try {
            logger.info("Processing cancellation for flight ${event.flightId}")
            
            // Delete all journeys containing the cancelled flight
            val deletedCount = flightsMaterializedViewDao.deleteJourneysContainingFlight(event.flightId)
            
            logger.info("Deleted $deletedCount journey records due to cancellation of flight ${event.flightId}")
        } catch (e: Exception) {
            logger.error("Failed to process cancellation for flight ${event.flightId}", e)
            throw e
        }
    }

    fun handleFlightReactivation(event: FlightReactivatedEvent) {
        try {
            logger.info("Processing reactivation for flight ${event.flightId}")
            
            // Recompute all possible journeys with the reactivated flight
            recomputeAllJourneysForFlight(event.flightId)
            
            logger.info("Successfully processed reactivation for flight ${event.flightId}")
        } catch (e: Exception) {
            logger.error("Failed to process reactivation for flight ${event.flightId}", e)
            throw e
        }
    }

    fun handleFlightCreation(event: FlightCreatedEvent) {
        try {
            logger.info("Processing creation of new flight ${event.flightId}")
            
            // Compute all possible journeys with the new flight
            recomputeAllJourneysForFlight(event.flightId)
            
            logger.info("Successfully processed creation of flight ${event.flightId}")
        } catch (e: Exception) {
            logger.error("Failed to process creation for flight ${event.flightId}", e)
            throw e
        }
    }

    private fun recomputeAllJourneysForFlight(flightId: Long) {
        var totalInserted = 0
        
        // Recompute direct flights
        totalInserted += flightsMaterializedViewDao.recomputeDirectFlights(flightId)
        
        // Recompute one-stop flights where this flight is first leg
        totalInserted += flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsFirst(flightId)
        
        // Recompute one-stop flights where this flight is second leg
        totalInserted += flightsMaterializedViewDao.recomputeOneStopFlightsWithFlightAsSecond(flightId)
        
        // Recompute two-stop flights containing this flight
        totalInserted += flightsMaterializedViewDao.recomputeTwoStopFlightsContainingFlight(flightId)
        
        logger.info("Recomputed $totalInserted total journey records for flight $flightId")
    }

    fun refreshFullMaterializedView() {
        try {
            logger.info("Starting full materialized view refresh")
            flightsMaterializedViewDao.refreshFullMaterializedView()
            logger.info("Full materialized view refresh completed successfully")
        } catch (e: Exception) {
            logger.error("Failed to refresh full materialized view", e)
            throw e
        }
    }
}