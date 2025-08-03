package com.credair.flight.search.managers

import com.credair.core.dao.interfaces.FlightDao
import com.credair.core.model.FlightJourney
import com.credair.core.model.FlightSearchResult
import com.credair.core.model.*
import java.time.Duration
import com.credair.flight.search.models.request.SearchCriteria
import com.credair.flight.search.models.request.SortBy
import com.credair.flight.search.models.request.SortCriteria
import com.credair.flight.search.models.request.SortOrder
import com.github.benmanes.caffeine.cache.Cache
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlightSearchManagerTest {

    private lateinit var flightDao: FlightDao
    private lateinit var searchCache: Cache<String, List<FlightSearchResult>>
    private lateinit var flightSearchManager: FlightSearchManager

    @BeforeEach
    fun setUp() {
        flightDao = mock()
        searchCache = mock()
        flightSearchManager = FlightSearchManager(flightDao, searchCache)
    }

    // Validation Tests
    @Test
    fun `searchFlights should throw exception when source airport is blank`() {
        val criteria = SearchCriteria(
            sourceAirport = "",
            destinationAirport = "BOM",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            flightSearchManager.searchFlights(criteria)
        }
        assertEquals("Source airport cannot be blank", exception.message)
    }

    @Test
    fun `searchFlights should throw exception when destination airport is blank`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            flightSearchManager.searchFlights(criteria)
        }
        assertEquals("Destination airport cannot be blank", exception.message)
    }

    @Test
    fun `searchFlights should throw exception when source and destination are same`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "DEL",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            flightSearchManager.searchFlights(criteria)
        }
        assertEquals("Source and destination airports cannot be the same", exception.message)
    }

    @Test
    fun `searchFlights should throw exception when number of seats is zero`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = 0
        )

        val exception = assertThrows<IllegalArgumentException> {
            flightSearchManager.searchFlights(criteria)
        }
        assertEquals("Minimum seats must be greater than 0", exception.message)
    }

    @Test
    fun `searchFlights should throw exception when number of seats is negative`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = -1
        )

        val exception = assertThrows<IllegalArgumentException> {
            flightSearchManager.searchFlights(criteria)
        }
        assertEquals("Minimum seats must be greater than 0", exception.message)
    }

    // Search Functionality Tests
    @Test
    fun `searchFlights should return filtered results using default parameters`() {
        val criteria = createValidSearchCriteria()
        val mockResults = listOf(
            createValidFlightSearchResult("DEL", "BOM"),
            createCircularFlightSearchResult(), // Should be filtered out
            createValidFlightSearchResult("DEL", "BOM", listOf("MAA"))
        )

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", null, 1, "duration", "ASC", 0, 10
        )).thenReturn(mockResults)

        val result = flightSearchManager.searchFlights(criteria)

        assertEquals(2, result.size) // Circular route should be filtered out
        verify(flightDao).searchFlightsOptimized("DEL", "BOM", null, 1, "duration", "ASC", 0, 10)
        verify(searchCache).put(any(), any())
    }

    @Test
    fun `searchFlights should return cached results when available`() {
        val criteria = createValidSearchCriteria()
        val cachedResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(cachedResults)

        val result = flightSearchManager.searchFlights(criteria)

        assertEquals(1, result.size)
        verify(searchCache).getIfPresent(any())
        verifyNoInteractions(flightDao)
    }

    @Test
    fun `searchFlights should use custom sort criteria`() {
        val criteria = createValidSearchCriteria()
        val sortCriteria = SortCriteria(SortBy.PRICE, SortOrder.DESC)
        val mockResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", null, 1, "price", "DESC", 0, 10
        )).thenReturn(mockResults)

        val result = flightSearchManager.searchFlights(criteria, sortCriteria)

        assertEquals(1, result.size)
        verify(flightDao).searchFlightsOptimized("DEL", "BOM", null, 1, "price", "DESC", 0, 10)
    }

    @Test
    fun `searchFlights should use custom pagination parameters`() {
        val criteria = createValidSearchCriteria()
        val mockResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", null, 1, "duration", "ASC", 2, 20
        )).thenReturn(mockResults)

        val result = flightSearchManager.searchFlights(criteria, page = 2, pageSize = 20)

        assertEquals(1, result.size)
        verify(flightDao).searchFlightsOptimized("DEL", "BOM", null, 1, "duration", "ASC", 2, 20)
    }

    @Test
    fun `searchFlights should handle departure date parameter`() {
        val departureDate = LocalDateTime.now().plusDays(1)
        val criteria = createValidSearchCriteria().copy(departureDate = departureDate)
        val mockResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", departureDate, 1, "duration", "ASC", 0, 10
        )).thenReturn(mockResults)

        val result = flightSearchManager.searchFlights(criteria)

        assertEquals(1, result.size)
        verify(flightDao).searchFlightsOptimized("DEL", "BOM", departureDate, 1, "duration", "ASC", 0, 10)
    }

    @Test
    fun `searchFlights should handle multiple passengers`() {
        val criteria = createValidSearchCriteria().copy(noOfSeats = 3)
        val mockResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", null, 3, "duration", "ASC", 0, 10
        )).thenReturn(mockResults)

        val result = flightSearchManager.searchFlights(criteria)

        assertEquals(1, result.size)
        verify(flightDao).searchFlightsOptimized("DEL", "BOM", null, 3, "duration", "ASC", 0, 10)
    }

    // Sort Criteria Tests
    @Test
    fun `searchFlights should handle departure time sorting`() {
        val criteria = createValidSearchCriteria()
        val sortCriteria = SortCriteria(SortBy.DEPARTURE_TIME, SortOrder.ASC)
        val mockResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", null, 1, "departure_time", "ASC", 0, 10
        )).thenReturn(mockResults)

        flightSearchManager.searchFlights(criteria, sortCriteria)

        verify(flightDao).searchFlightsOptimized("DEL", "BOM", null, 1, "departure_time", "ASC", 0, 10)
    }

    @Test
    fun `searchFlights should handle arrival time sorting`() {
        val criteria = createValidSearchCriteria()
        val sortCriteria = SortCriteria(SortBy.ARRIVAL_TIME, SortOrder.DESC)
        val mockResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", null, 1, "arrival_time", "DESC", 0, 10
        )).thenReturn(mockResults)

        flightSearchManager.searchFlights(criteria, sortCriteria)

        verify(flightDao).searchFlightsOptimized("DEL", "BOM", null, 1, "arrival_time", "DESC", 0, 10)
    }

    @Test
    fun `searchFlights should handle price sorting`() {
        val criteria = createValidSearchCriteria()
        val sortCriteria = SortCriteria(SortBy.PRICE, SortOrder.ASC)
        val mockResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", null, 1, "price", "ASC", 0, 10
        )).thenReturn(mockResults)

        flightSearchManager.searchFlights(criteria, sortCriteria)

        verify(flightDao).searchFlightsOptimized("DEL", "BOM", null, 1, "price", "ASC", 0, 10)
    }

    @Test
    fun `searchFlights should handle duration sorting`() {
        val criteria = createValidSearchCriteria()
        val sortCriteria = SortCriteria(SortBy.DURATION, SortOrder.DESC)
        val mockResults = listOf(createValidFlightSearchResult("DEL", "BOM"))

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", null, 1, "duration", "DESC", 0, 10
        )).thenReturn(mockResults)

        flightSearchManager.searchFlights(criteria, sortCriteria)

        verify(flightDao).searchFlightsOptimized("DEL", "BOM", null, 1, "duration", "DESC", 0, 10)
    }

    // Route Filtering Tests
    @Test
    fun `searchFlights should filter out circular routes`() {
        val criteria = createValidSearchCriteria()
        val mockResults = listOf(
            createValidFlightSearchResult("DEL", "BOM"), // Valid
            createCircularFlightSearchResult(), // Invalid - circular route
            createValidFlightSearchResult("DEL", "BOM", listOf("CCU")) // Valid - one stop
        )

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized("DEL", "BOM", null, 1, "duration", "ASC", 0, 10))
            .thenReturn(mockResults)

        val result = flightSearchManager.searchFlights(criteria)

        assertEquals(2, result.size)
        assertTrue(result.none { it.stopAirports.contains("DEL") && it.stopAirports.size > 0 })
    }

    @Test
    fun `searchFlights should filter out backtracking routes`() {
        val criteria = createValidSearchCriteria()
        val mockResults = listOf(
            createValidFlightSearchResult("DEL", "BOM"), // Valid direct
            createBacktrackingFlightSearchResult(), // Invalid - backtracking
            createValidFlightSearchResult("DEL", "BOM", listOf("MAA")) // Valid one-stop
        )

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized("DEL", "BOM", null, 1, "duration", "ASC", 0, 10))
            .thenReturn(mockResults)

        val result = flightSearchManager.searchFlights(criteria)

        assertEquals(2, result.size)
    }

    @Test
    fun `searchFlights should return empty list when no results from dao`() {
        val criteria = createValidSearchCriteria()

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(emptyList())

        val result = flightSearchManager.searchFlights(criteria)

        assertTrue(result.isEmpty())
    }

    // Flight Journey Tests
    @Test
    fun `getFlightJourney should return journey for valid flight IDs`() {
        val flightIds = listOf(1L, 2L)
        val validJourney = createValidFlightJourney()

        whenever(flightDao.getFlightJourney(flightIds)).thenReturn(validJourney)

        val result = flightSearchManager.getFlightJourney(flightIds)

        assertEquals(validJourney, result)
        verify(flightDao).getFlightJourney(flightIds)
    }

    @Test
    fun `getFlightJourney should return null when no journey found`() {
        val flightIds = listOf(999L)

        whenever(flightDao.getFlightJourney(flightIds)).thenReturn(null)

        val result = flightSearchManager.getFlightJourney(flightIds)

        assertEquals(null, result)
        verify(flightDao).getFlightJourney(flightIds)
    }

    @Test
    fun `getFlightJourney should handle empty flight IDs list`() {
        val flightIds = emptyList<Long>()

        whenever(flightDao.getFlightJourney(flightIds)).thenReturn(null)

        val result = flightSearchManager.getFlightJourney(flightIds)

        assertEquals(null, result)
        verify(flightDao).getFlightJourney(flightIds)
    }

    @Test
    fun `getFlightJourney should handle single flight ID`() {
        val flightIds = listOf(1L)
        val validJourney = createValidFlightJourney()

        whenever(flightDao.getFlightJourney(flightIds)).thenReturn(validJourney)

        val result = flightSearchManager.getFlightJourney(flightIds)

        assertEquals(validJourney, result)
        verify(flightDao).getFlightJourney(flightIds)
    }

    // Error Handling Tests
    @Test
    fun `searchFlights should propagate dao exceptions`() {
        val criteria = createValidSearchCriteria()

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized("DEL", "BOM", null, 1, "duration", "ASC", 0, 10))
            .thenThrow(RuntimeException("Database error"))

        val exception = assertThrows<RuntimeException> {
            flightSearchManager.searchFlights(criteria)
        }
        assertEquals("Database error", exception.message)
    }

    @Test
    fun `getFlightJourney should propagate dao exceptions`() {
        val flightIds = listOf(1L)

        whenever(flightDao.getFlightJourney(flightIds)).thenThrow(RuntimeException("Journey fetch failed"))

        val exception = assertThrows<RuntimeException> {
            flightSearchManager.getFlightJourney(flightIds)
        }
        assertEquals("Journey fetch failed", exception.message)
    }

    // Integration Tests
    @Test
    fun `searchFlights should handle complex search with all parameters`() {
        val departureDate = LocalDateTime.now().plusDays(2)
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            departureDate = departureDate,
            noOfSeats = 4
        )
        val sortCriteria = SortCriteria(SortBy.PRICE, SortOrder.ASC)
        val mockResults = listOf(
            createValidFlightSearchResult("DEL", "BOM"),
            createValidFlightSearchResult("DEL", "BOM", listOf("MAA"))
        )

        whenever(searchCache.getIfPresent(any())).thenReturn(null)
        whenever(flightDao.searchFlightsOptimized(
            "DEL", "BOM", departureDate, 4, "price", "ASC", 1, 15
        )).thenReturn(mockResults)

        val result = flightSearchManager.searchFlights(criteria, sortCriteria, 1, 15)

        assertEquals(2, result.size)
        verify(flightDao).searchFlightsOptimized("DEL", "BOM", departureDate, 4, "price", "ASC", 1, 15)
    }

    // Helper Methods
    private fun createValidSearchCriteria() = SearchCriteria(
        sourceAirport = "DEL",
        destinationAirport = "BOM",
        noOfSeats = 1
    )

    private fun createValidFlightSearchResult(
        departure: String,
        arrival: String,
        stops: List<String> = emptyList()
    ) = FlightSearchResult(
        airlineName = "Air India",
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

    private fun createCircularFlightSearchResult() = FlightSearchResult(
        airlineName = "Test Airline",
        airlineLogoUrl = "https://example.com/logo.png",
        departureTime = System.currentTimeMillis(),
        arrivalTime = System.currentTimeMillis() + 10800000, // 3 hours later
        departureAirport = "DEL",
        arrivalAirport = "BOM",
        totalDurationMinutes = 180,
        stopCount = 1,
        stopAirports = listOf("DEL"), // Circular - goes back to source
        priceAmount = BigDecimal("300.00"),
        priceCurrency = "USD",
        flightIds = listOf(2L)
    )

    private fun createBacktrackingFlightSearchResult() = FlightSearchResult(
        airlineName = "Test Airline",
        airlineLogoUrl = "https://example.com/logo.png",
        departureTime = System.currentTimeMillis(),
        arrivalTime = System.currentTimeMillis() + 14400000, // 4 hours later
        departureAirport = "DEL",
        arrivalAirport = "BOM",
        totalDurationMinutes = 240,
        stopCount = 1,
        stopAirports = listOf("BOM"), // Backtracking - layover at destination
        priceAmount = BigDecimal("250.00"),
        priceCurrency = "USD",
        flightIds = listOf(3L)
    )

    private fun createValidFlightJourney() = FlightJourney(
        totalDuration = Duration.ofHours(2),
        totalTimeInAir = Duration.ofHours(2),
        price = FlightPrice(BigDecimal("100.00"), "USD"),
        segments = listOf(
            FlightSegment(
                airline = FlightAirline("Test Airline", "https://example.com/logo.png"),
                departure = FlightStop(System.currentTimeMillis(), System.currentTimeMillis() + 3600000, "DEL", "Delhi"),
                arrival = FlightStop(System.currentTimeMillis() + 7200000, System.currentTimeMillis() + 7200000, "BOM", "Mumbai"),
                segmentDuration = Duration.ofHours(2),
                price = FlightPrice(BigDecimal("100.00"), "USD"),
                id = 1L
            )
        ),
        layovers = emptyList()
    )
}