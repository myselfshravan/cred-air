package com.credair.core.manager

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.events.*
import com.credair.core.model.Flight
import com.credair.core.services.FlightsMaterializedViewManager
import com.credair.core.util.FlightChangeDetector
import com.google.inject.Inject
import com.google.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class FlightUpdateManager @Inject constructor(
    private val flightDao: FlightDao,
    private val flightsMaterializedViewManager: FlightsMaterializedViewManager
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(FlightUpdateManager::class.java)
    }

    fun updateFlight(flightData: Flight): Flight {
        require(flightData.flightId != null) { "Flight ID cannot be null for update operation" }
        
        logger.info("Processing flight update for flight ID: ${flightData.flightId}")
        
        try {
            // Fetch existing flight for change detection
            val oldFlight = flightDao.findById(flightData.flightId!!)
            
            // Analyze what changed
            val changeAnalysis = FlightChangeDetector.analyzeChanges(oldFlight, flightData)
            logger.debug("Change analysis for flight ${flightData.flightId}: ${changeAnalysis.changeType}")
            
            // Update the database
            val updatedFlight = flightDao.update(flightData)
            
            // Handle materialized view updates based on change type
            handleMaterializedViewUpdate(changeAnalysis)
            
            logger.info("Successfully updated flight ${flightData.flightId}")
            return updatedFlight
            
        } catch (e: Exception) {
            logger.error("Failed to update flight ${flightData.flightId}", e)
            throw e
        }
    }
    
    private fun handleMaterializedViewUpdate(changeAnalysis: FlightChangeAnalysis) {
        val event = FlightChangeDetector.createEvent(changeAnalysis)
        
        if (event != null) {
            try {
                when (event) {
                    is FlightScheduleChangedEvent -> {
                        flightsMaterializedViewManager.handleScheduleChange(event)
                    }
                    is FlightSeatsChangedEvent -> {
                        flightsMaterializedViewManager.handleSeatsChange(event)
                    }
                    is FlightCancelledEvent -> {
                        flightsMaterializedViewManager.handleFlightCancellation(event)
                    }
                    is FlightReactivatedEvent -> {
                        flightsMaterializedViewManager.handleFlightReactivation(event)
                    }
                    is FlightCreatedEvent -> {
                        flightsMaterializedViewManager.handleFlightCreation(event)
                    }
                }
                logger.info("Successfully processed materialized view update for change type: ${changeAnalysis.changeType}")
            } catch (e: Exception) {
                logger.error("Failed to update materialized view for flight ${changeAnalysis.newFlight.flightId}", e)
                // Don't throw - flight update succeeded, MV update can be retried later
            }
        } else {
            logger.debug("No materialized view update needed for flight ${changeAnalysis.newFlight.flightId}")
        }
    }
}