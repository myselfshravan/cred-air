package com.credair.core.events

import com.credair.core.services.FlightsMaterializedViewManager
import com.google.inject.Inject
import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.PreDestroy

@Singleton
class FlightEventPublisher @Inject constructor(
    private val flightsMaterializedViewManager: FlightsMaterializedViewManager
) {

    companion object {
        private val logger = LoggerFactory.getLogger(FlightEventPublisher::class.java)
        private val eventQueue = LinkedBlockingQueue<FlightChangeEvent>()
        private val executor = Executors.newFixedThreadPool(2)
        private val isRunning = AtomicBoolean(true)
    }
    
    init {
        startEventProcessors()
    }
    
    fun publishEvent(event: FlightChangeEvent) {
        if (isRunning.get()) {
            val offered = eventQueue.offer(event)
            if (!offered) {
                logger.warn("Failed to queue event for flight ${event.flightId}, queue may be full")
            } else {
                logger.debug("Queued event ${event::class.simpleName} for flight ${event.flightId}")
            }
        }
    }
    
    private fun startEventProcessors() {
        repeat(2) { threadIndex ->
            executor.submit {
                logger.info("Starting event processor thread $threadIndex")
                
                while (isRunning.get()) {
                    try {
                        val event = eventQueue.take() // Blocking call
                        processEvent(event)
                    } catch (e: InterruptedException) {
                        logger.info("Event processor thread $threadIndex interrupted")
                        Thread.currentThread().interrupt()
                        break
                    } catch (e: Exception) {
                        logger.error("Error processing event in thread $threadIndex", e)
                    }
                }
                
                logger.info("Event processor thread $threadIndex stopped")
            }
        }
    }
    
    private fun processEvent(event: FlightChangeEvent) {
        try {
            when (event) {
                is FlightScheduleChangedEvent -> flightsMaterializedViewManager.handleScheduleChange(event)
                is FlightSeatsChangedEvent -> flightsMaterializedViewManager.handleSeatsChange(event)
                is FlightCancelledEvent -> flightsMaterializedViewManager.handleFlightCancellation(event)
                is FlightReactivatedEvent -> flightsMaterializedViewManager.handleFlightReactivation(event)
                is FlightCreatedEvent -> flightsMaterializedViewManager.handleFlightCreation(event)
            }
        } catch (e: Exception) {
            logger.error("Failed to process event ${event::class.simpleName} for flight ${event.flightId}", e)
            // In production, might want to retry or send to dead letter queue
        }
    }
    
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down FlightEventPublisher")
        isRunning.set(false)
        executor.shutdown()
        
        // Process remaining events
        while (!eventQueue.isEmpty()) {
            val event = eventQueue.poll()
            if (event != null) {
                try {
                    processEvent(event)
                } catch (e: Exception) {
                    logger.error("Error processing remaining event during shutdown", e)
                }
            }
        }
        
        logger.info("FlightEventPublisher shutdown complete")
    }
    
    fun getQueueSize(): Int = eventQueue.size
}