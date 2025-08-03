package com.credair.flight.search.utils

import com.credair.flight.search.models.request.SearchCriteria
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ValidationUtilsTest {

    // Source Airport Validation Tests
    @Test
    fun `validateSearchCriteria should throw exception when source airport is empty`() {
        val criteria = SearchCriteria(
            sourceAirport = "",
            destinationAirport = "BOM",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Source airport cannot be blank", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw exception when source airport is blank`() {
        val criteria = SearchCriteria(
            sourceAirport = "   ",
            destinationAirport = "BOM",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Source airport cannot be blank", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw exception when source airport contains only whitespace`() {
        val criteria = SearchCriteria(
            sourceAirport = "\t\n  ",
            destinationAirport = "BOM",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Source airport cannot be blank", exception.message)
    }

    // Destination Airport Validation Tests
    @Test
    fun `validateSearchCriteria should throw exception when destination airport is empty`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Destination airport cannot be blank", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw exception when destination airport is blank`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "   ",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Destination airport cannot be blank", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw exception when destination airport contains only whitespace`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "\t\n  ",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Destination airport cannot be blank", exception.message)
    }

    // Same Airport Validation Tests
    @Test
    fun `validateSearchCriteria should throw exception when source and destination are exactly same`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "DEL",
            noOfSeats = 1
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Source and destination airports cannot be the same", exception.message)
    }

    @Test
    fun `validateSearchCriteria should allow same airports with different cases`() {
        val criteria = SearchCriteria(
            sourceAirport = "del",
            destinationAirport = "DEL",
            noOfSeats = 1
        )

        // Should not throw since case-sensitive comparison
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should allow same airports with whitespace differences`() {
        val criteria = SearchCriteria(
            sourceAirport = " DEL ",
            destinationAirport = "DEL",
            noOfSeats = 1
        )

        // Should not throw since whitespace makes them different strings
        validateSearchCriteria(criteria)
    }

    // Seat Count Validation Tests
    @Test
    fun `validateSearchCriteria should throw exception when number of seats is zero`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = 0
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Minimum seats must be greater than 0", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw exception when number of seats is negative`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = -1
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Minimum seats must be greater than 0", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw exception when number of seats is large negative`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = -100
        )

        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Minimum seats must be greater than 0", exception.message)
    }

    // Valid Cases Tests
    @Test
    fun `validateSearchCriteria should pass for valid criteria with minimum seats`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = 1
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should pass for valid criteria with multiple seats`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = 5
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should pass for valid criteria with departure date`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            departureDate = LocalDateTime.now().plusDays(1),
            noOfSeats = 2
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should pass for valid criteria with null departure date`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            departureDate = null,
            noOfSeats = 1
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    // Airport Code Format Tests
    @Test
    fun `validateSearchCriteria should pass for three letter airport codes`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = 1
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should pass for different length airport codes`() {
        val criteria = SearchCriteria(
            sourceAirport = "VGHS", // 4 letter code
            destinationAirport = "JFK", // 3 letter code
            noOfSeats = 1
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should pass for international airport codes`() {
        val criteria = SearchCriteria(
            sourceAirport = "LHR",
            destinationAirport = "CDG",
            noOfSeats = 1
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    // Edge Cases
    @Test
    fun `validateSearchCriteria should pass for single character airport codes`() {
        val criteria = SearchCriteria(
            sourceAirport = "A",
            destinationAirport = "B",
            noOfSeats = 1
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should pass for numeric airport codes`() {
        val criteria = SearchCriteria(
            sourceAirport = "123",
            destinationAirport = "456",
            noOfSeats = 1
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should pass for mixed alphanumeric airport codes`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL1",
            destinationAirport = "BOM2",
            noOfSeats = 1
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    // Maximum Seat Count Tests
    @Test
    fun `validateSearchCriteria should pass for reasonable high seat count`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = 500 // Large aircraft capacity
        )

        // Should not throw any exception
        validateSearchCriteria(criteria)
    }

    @Test
    fun `validateSearchCriteria should pass for maximum integer seat count`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL",
            destinationAirport = "BOM",
            noOfSeats = Int.MAX_VALUE
        )

        // Should not throw any exception (assuming no upper limit validation)
        validateSearchCriteria(criteria)
    }

    // Multiple Validation Errors Tests
    @Test
    fun `validateSearchCriteria should throw first validation error for multiple issues`() {
        val criteria = SearchCriteria(
            sourceAirport = "", // Invalid
            destinationAirport = "", // Invalid
            noOfSeats = 0 // Invalid
        )

        // Should throw the first validation error (source airport)
        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Source airport cannot be blank", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw destination error when source is valid but destination invalid`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL", // Valid
            destinationAirport = "", // Invalid
            noOfSeats = 0 // Invalid
        )

        // Should throw the destination airport error
        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Destination airport cannot be blank", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw same airport error when both airports valid but same`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL", // Valid
            destinationAirport = "DEL", // Valid but same
            noOfSeats = 0 // Invalid
        )

        // Should throw the same airport error
        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Source and destination airports cannot be the same", exception.message)
    }

    @Test
    fun `validateSearchCriteria should throw seat count error when airports valid but seats invalid`() {
        val criteria = SearchCriteria(
            sourceAirport = "DEL", // Valid
            destinationAirport = "BOM", // Valid
            noOfSeats = 0 // Invalid
        )

        // Should throw the seat count error
        val exception = assertThrows<IllegalArgumentException> {
            validateSearchCriteria(criteria)
        }
        assertEquals("Minimum seats must be greater than 0", exception.message)
    }
}