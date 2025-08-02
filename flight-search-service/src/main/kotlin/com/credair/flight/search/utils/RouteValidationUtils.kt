package com.credair.flight.search.utils

import com.credair.core.model.FlightSearchResult

object RouteValidationUtils {

    fun isEfficientRoute(result: FlightSearchResult): Boolean {
        val stopAirports = result.stopAirports
        val sourceAirport = result.departureAirport
        val destinationAirport = result.arrivalAirport
        
        return when {
            stopAirports.isEmpty() -> true
            stopAirports.size == 1 -> isEfficientSingleStopRoute(sourceAirport, stopAirports[0], destinationAirport)
            stopAirports.size == 2 -> isEfficientTwoStopRoute(sourceAirport, stopAirports[0], stopAirports[1], destinationAirport)
            else -> false
        }
    }
    
    private fun isEfficientSingleStopRoute(source: String, layover: String, destination: String): Boolean {
        return !isBacktrackingRoute(source, layover, destination) && 
               !isCircularRoute(listOf(source, layover, destination))
    }
    
    private fun isEfficientTwoStopRoute(source: String, layover1: String, layover2: String, destination: String): Boolean {
        val route = listOf(source, layover1, layover2, destination)
        return !isCircularRoute(route) && 
               !hasRedundantBacktracking(route)
    }
    
    private fun isCircularRoute(airports: List<String>): Boolean {
        return airports.distinct().size != airports.size
    }
    
    private fun isBacktrackingRoute(source: String, layover: String, destination: String): Boolean {
        return layover == source || layover == destination
    }
    
    private fun hasRedundantBacktracking(airports: List<String>): Boolean {
        for (i in 0 until airports.size - 2) {
            if (airports[i] == airports[i + 2]) {
                return true
            }
        }
        return false
    }

}