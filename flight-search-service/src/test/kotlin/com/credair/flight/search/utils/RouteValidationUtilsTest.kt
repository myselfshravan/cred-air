package com.credair.flight.search.utils

import com.credair.core.model.FlightSearchResult
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RouteValidationUtilsTest {

    // Direct Route Tests
    @Test
    fun `isEfficientRoute should return true for direct flights`() {
        val directFlight = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = emptyList()
        )

        val result = RouteValidationUtils.isEfficientRoute(directFlight)

        assertTrue(result)
    }

    // Single Stop Route Tests
    @Test
    fun `isEfficientRoute should return true for valid single stop route`() {
        val validSingleStop = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA")
        )

        val result = RouteValidationUtils.isEfficientRoute(validSingleStop)

        assertTrue(result)
    }

    @Test
    fun `isEfficientRoute should return false for single stop circular route`() {
        val circularRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("DEL") // Goes back to source
        )

        val result = RouteValidationUtils.isEfficientRoute(circularRoute)

        assertFalse(result)
    }

    @Test
    fun `isEfficientRoute should return false for single stop backtracking to destination`() {
        val backtrackingRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("BOM") // Layover at destination
        )

        val result = RouteValidationUtils.isEfficientRoute(backtrackingRoute)

        assertFalse(result)
    }

    @Test
    fun `isEfficientRoute should return false for single stop backtracking to source`() {
        val backtrackingRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("DEL") // Layover back at source
        )

        val result = RouteValidationUtils.isEfficientRoute(backtrackingRoute)

        assertFalse(result)
    }

    // Two Stop Route Tests
    @Test
    fun `isEfficientRoute should return true for valid two stop route`() {
        val validTwoStop = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA", "HYD")
        )

        val result = RouteValidationUtils.isEfficientRoute(validTwoStop)

        assertTrue(result)
    }

    @Test
    fun `isEfficientRoute should return false for two stop circular route with duplicate source`() {
        val circularRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA", "DEL") // Goes back to source
        )

        val result = RouteValidationUtils.isEfficientRoute(circularRoute)

        assertFalse(result)
    }

    @Test
    fun `isEfficientRoute should return false for two stop circular route with duplicate destination`() {
        val circularRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA", "BOM") // Duplicate destination
        )

        val result = RouteValidationUtils.isEfficientRoute(circularRoute)

        assertFalse(result)
    }

    @Test
    fun `isEfficientRoute should return false for two stop circular route with duplicate stops`() {
        val circularRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA", "MAA") // Duplicate stops
        )

        val result = RouteValidationUtils.isEfficientRoute(circularRoute)

        assertFalse(result)
    }

    @Test
    fun `isEfficientRoute should return false for two stop redundant backtracking`() {
        val backtrackingRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA", "DEL") // Returns to source after first stop
        )

        val result = RouteValidationUtils.isEfficientRoute(backtrackingRoute)

        assertFalse(result)
    }

    @Test
    fun `isEfficientRoute should return false for complex backtracking pattern`() {
        val backtrackingRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("HYD", "DEL") // DEL -> HYD -> DEL -> BOM (goes back)
        )

        val result = RouteValidationUtils.isEfficientRoute(backtrackingRoute)

        assertFalse(result)
    }

    // Three or More Stops Tests
    @Test
    fun `isEfficientRoute should return false for routes with three or more stops`() {
        val threeStopRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA", "HYD", "CCU")
        )

        val result = RouteValidationUtils.isEfficientRoute(threeStopRoute)

        assertFalse(result)
    }

    @Test
    fun `isEfficientRoute should return false for routes with four stops`() {
        val fourStopRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA", "HYD", "CCU", "AMD")
        )

        val result = RouteValidationUtils.isEfficientRoute(fourStopRoute)

        assertFalse(result)
    }

    // Edge Case Tests
    @Test
    fun `isEfficientRoute should handle same source and destination with no stops`() {
        val sameSourceDest = createFlightSearchResult(
            departure = "DEL",
            arrival = "DEL",
            stops = emptyList()
        )

        val result = RouteValidationUtils.isEfficientRoute(sameSourceDest)

        assertTrue(result) // Direct route, even if source == destination
    }

    @Test
    fun `isEfficientRoute should handle empty airport codes`() {
        val emptyCodeRoute = createFlightSearchResult(
            departure = "",
            arrival = "BOM",
            stops = listOf("MAA")
        )

        val result = RouteValidationUtils.isEfficientRoute(emptyCodeRoute)

        assertTrue(result) // Empty string is different from stop, so it's considered valid
    }

    // Complex Circular Route Tests
    @Test
    fun `isEfficientRoute should detect circular route with source in middle of stops`() {
        val circularRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("MAA", "DEL") // DEL -> MAA -> DEL -> BOM
        )

        val result = RouteValidationUtils.isEfficientRoute(circularRoute)

        assertFalse(result)
    }

    @Test
    fun `isEfficientRoute should detect circular route with destination in stops`() {
        val circularRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "BOM",
            stops = listOf("BOM", "MAA") // DEL -> BOM -> MAA -> BOM
        )

        val result = RouteValidationUtils.isEfficientRoute(circularRoute)

        assertFalse(result)
    }

    // Valid Complex Routes Tests
    @Test
    fun `isEfficientRoute should accept valid hub-and-spoke patterns`() {
        val hubSpokeRoute = createFlightSearchResult(
            departure = "GOA",
            arrival = "CCU",
            stops = listOf("DEL") // Small city to hub to small city
        )

        val result = RouteValidationUtils.isEfficientRoute(hubSpokeRoute)

        assertTrue(result)
    }

    @Test
    fun `isEfficientRoute should accept valid two-stop hub pattern`() {
        val hubPattern = createFlightSearchResult(
            departure = "GOA",
            arrival = "CCU",
            stops = listOf("BOM", "DEL") // Regional -> Major hub -> Major hub -> Regional
        )

        val result = RouteValidationUtils.isEfficientRoute(hubPattern)

        assertTrue(result)
    }

    // International Route Tests
    @Test
    fun `isEfficientRoute should handle international airport codes`() {
        val internationalRoute = createFlightSearchResult(
            departure = "DEL",
            arrival = "JFK",
            stops = listOf("DOH") // Delhi -> Doha -> New York
        )

        val result = RouteValidationUtils.isEfficientRoute(internationalRoute)

        assertTrue(result)
    }

    @Test
    fun `isEfficientRoute should detect circular international routes`() {
        val circularInternational = createFlightSearchResult(
            departure = "DEL",
            arrival = "JFK",
            stops = listOf("DOH", "DEL") // Delhi -> Doha -> Delhi -> New York
        )

        val result = RouteValidationUtils.isEfficientRoute(circularInternational)

        assertFalse(result)
    }

    // Performance Tests
    @Test
    fun `isEfficientRoute should handle multiple route validations efficiently`() {
        val routes = listOf(
            createFlightSearchResult("DEL", "BOM", emptyList()),
            createFlightSearchResult("DEL", "BOM", listOf("MAA")),
            createFlightSearchResult("DEL", "BOM", listOf("MAA", "HYD")),
            createFlightSearchResult("DEL", "BOM", listOf("DEL")),
            createFlightSearchResult("DEL", "BOM", listOf("BOM"))
        )

        val startTime = System.currentTimeMillis()
        
        routes.forEach { route ->
            RouteValidationUtils.isEfficientRoute(route)
        }
        
        val duration = System.currentTimeMillis() - startTime
        
        // Should complete quickly
        assertTrue(duration < 100, "Route validation too slow: ${duration}ms")
    }

    // Helper Methods
    private fun createFlightSearchResult(
        departure: String,
        arrival: String,
        stops: List<String>
    ) = FlightSearchResult(
        airlineName = "Test Airline",
        airlineLogoUrl = "https://example.com/logo.png",
        departureTime = System.currentTimeMillis(),
        arrivalTime = System.currentTimeMillis() + 7200000, // 2 hours later
        departureAirport = departure,
        arrivalAirport = arrival,
        totalDurationMinutes = 120,
        stopCount = stops.size,
        stopAirports = stops,
        priceAmount = BigDecimal("200.00"),
        priceCurrency = "USD",
        flightIds = listOf(1L)
    )
}