package com.credair.core.manager

import com.credair.core.dao.interfaces.AirlineDao
import com.credair.core.exception.ResourceNotFoundException
import com.credair.core.exception.ValidationException
import com.credair.core.model.Airline
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AirlineManagerTest {

    private lateinit var airlineDao: AirlineDao
    private lateinit var airlineManager: AirlineManager

    @BeforeEach
    fun setUp() {
        airlineDao = mock()
        airlineManager = AirlineManager(airlineDao)
    }

    // Create Airline Tests
    @Test
    fun `createAirline should successfully create airline with valid data`() {
        val airline = createValidAirline()
        val savedAirline = airline.copy(id = 1L, createdAt = LocalDateTime.now(), updatedAt = LocalDateTime.now())
        
        whenever(airlineDao.findByCode("AI")).thenReturn(null)
        whenever(airlineDao.save(any())).thenReturn(savedAirline)

        val result = airlineManager.createAirline(airline)

        assertNotNull(result.id)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
        assertEquals("Air India", result.name)
        verify(airlineDao).findByCode("AI")
        verify(airlineDao).save(any())
    }

    @Test
    fun `createAirline should throw exception when airline name is blank`() {
        val airline = createValidAirline().copy(name = "")

        val exception = assertThrows<ValidationException> {
            airlineManager.createAirline(airline)
        }
        assertEquals("Airline name cannot be blank", exception.message)
    }

    @Test
    fun `createAirline should throw exception when airline code is blank`() {
        val airline = createValidAirline().copy(code = "")

        val exception = assertThrows<ValidationException> {
            airlineManager.createAirline(airline)
        }
        assertEquals("Airline code cannot be blank", exception.message)
    }

    @Test
    fun `createAirline should throw exception when airline code is too short`() {
        val airline = createValidAirline().copy(code = "A")

        val exception = assertThrows<ValidationException> {
            airlineManager.createAirline(airline)
        }
        assertEquals("Airline code must be 2-3 characters", exception.message)
    }

    @Test
    fun `createAirline should throw exception when airline code is too long`() {
        val airline = createValidAirline().copy(code = "ABCD")

        val exception = assertThrows<ValidationException> {
            airlineManager.createAirline(airline)
        }
        assertEquals("Airline code must be 2-3 characters", exception.message)
    }

    @Test
    fun `createAirline should throw exception when airline code contains lowercase letters`() {
        val airline = createValidAirline().copy(code = "ai")

        val exception = assertThrows<ValidationException> {
            airlineManager.createAirline(airline)
        }
        assertEquals("Airline code must contain only uppercase letters", exception.message)
    }

    @Test
    fun `createAirline should throw exception when airline code contains numbers`() {
        val airline = createValidAirline().copy(code = "A1")

        val exception = assertThrows<ValidationException> {
            airlineManager.createAirline(airline)
        }
        assertEquals("Airline code must contain only uppercase letters", exception.message)
    }

    @Test
    fun `createAirline should throw exception when country is blank`() {
        val airline = createValidAirline().copy(country = "")

        val exception = assertThrows<ValidationException> {
            airlineManager.createAirline(airline)
        }
        assertEquals("Country cannot be blank", exception.message)
    }

    @Test
    fun `createAirline should throw exception when airline code already exists`() {
        val airline = createValidAirline()
        val existingAirline = createValidAirline().copy(id = 1L)

        whenever(airlineDao.findByCode("AI")).thenReturn(existingAirline)

        val exception = assertThrows<ValidationException> {
            airlineManager.createAirline(airline)
        }
        assertEquals("Airline with code AI already exists", exception.message)
    }

    // Update Airline Tests
    @Test
    fun `updateAirline should successfully update airline`() {
        val existingAirline = createValidAirline().copy(id = 1L, createdAt = LocalDateTime.now())
        val updatedAirlineData = existingAirline.copy(name = "Air India Express")
        val savedAirline = updatedAirlineData.copy(updatedAt = LocalDateTime.now())

        whenever(airlineDao.findById(1L)).thenReturn(existingAirline)
        whenever(airlineDao.update(any())).thenReturn(savedAirline)

        val result = airlineManager.updateAirline(1L, updatedAirlineData)

        assertEquals("Air India Express", result.name)
        assertEquals(existingAirline.createdAt, result.createdAt)
        assertNotNull(result.updatedAt)
        verify(airlineDao).findById(1L)
        verify(airlineDao).update(any())
    }

    @Test
    fun `updateAirline should throw exception when airline not found`() {
        val airline = createValidAirline()

        whenever(airlineDao.findById(999L)).thenReturn(null)

        val exception = assertThrows<RuntimeException> {
            airlineManager.updateAirline(999L, airline)
        }
        assertEquals("Failed to update airline: Airline with id 999 not found", exception.message)
    }

    @Test
    fun `updateAirline should validate unique code when code changes`() {
        val existingAirline = createValidAirline().copy(id = 1L, code = "AI")
        val updatedAirlineData = existingAirline.copy(code = "SG")
        val conflictingAirline = createValidAirline().copy(id = 2L, code = "SG")

        whenever(airlineDao.findById(1L)).thenReturn(existingAirline)
        whenever(airlineDao.findByCode("SG")).thenReturn(conflictingAirline)

        val exception = assertThrows<ValidationException> {
            airlineManager.updateAirline(1L, updatedAirlineData)
        }
        assertEquals("Airline with code SG already exists", exception.message)
    }

    @Test
    fun `updateAirline should not validate unique code when code does not change`() {
        val existingAirline = createValidAirline().copy(id = 1L, createdAt = LocalDateTime.now())
        val updatedAirlineData = existingAirline.copy(name = "Updated Name")
        val savedAirline = updatedAirlineData.copy(updatedAt = LocalDateTime.now())

        whenever(airlineDao.findById(1L)).thenReturn(existingAirline)
        whenever(airlineDao.update(any())).thenReturn(savedAirline)

        val result = airlineManager.updateAirline(1L, updatedAirlineData)

        assertEquals("Updated Name", result.name)
        verify(airlineDao, never()).findByCode(any())
    }

    // Get Airline Tests
    @Test
    fun `getAirlineById should return airline when found`() {
        val airline = createValidAirline().copy(id = 1L)

        whenever(airlineDao.findById(1L)).thenReturn(airline)

        val result = airlineManager.getAirlineById(1L)

        assertEquals(airline, result)
        verify(airlineDao).findById(1L)
    }

    @Test
    fun `getAirlineById should throw exception when not found`() {
        whenever(airlineDao.findById(999L)).thenReturn(null)

        val exception = assertThrows<ResourceNotFoundException> {
            airlineManager.getAirlineById(999L)
        }
        assertEquals("Airline with id 999 not found", exception.message)
    }

    @Test
    fun `getAirlineByCode should return airline when found`() {
        val airline = createValidAirline().copy(id = 1L)

        whenever(airlineDao.findByCode("AI")).thenReturn(airline)

        val result = airlineManager.getAirlineByCode("AI")

        assertEquals(airline, result)
        verify(airlineDao).findByCode("AI")
    }

    @Test
    fun `getAirlineByCode should throw exception when not found`() {
        whenever(airlineDao.findByCode("XX")).thenReturn(null)

        val exception = assertThrows<IllegalArgumentException> {
            airlineManager.getAirlineByCode("XX")
        }
        assertEquals("Airline with code XX not found", exception.message)
    }

    @Test
    fun `getAllAirlines should return all airlines`() {
        val airlines = listOf(
            createValidAirline().copy(id = 1L, code = "AI"),
            createValidAirline().copy(id = 2L, code = "SG", name = "SpiceJet")
        )

        whenever(airlineDao.findAll()).thenReturn(airlines)

        val result = airlineManager.getAllAirlines()

        assertEquals(2, result.size)
        assertEquals(airlines, result)
        verify(airlineDao).findAll()
    }

    @Test
    fun `getActiveAirlines should return only active airlines`() {
        val activeAirlines = listOf(
            createValidAirline().copy(id = 1L, active = true),
            createValidAirline().copy(id = 2L, active = true)
        )

        whenever(airlineDao.findActiveAirlines()).thenReturn(activeAirlines)

        val result = airlineManager.getActiveAirlines()

        assertEquals(2, result.size)
        assertTrue(result.all { it.active })
        verify(airlineDao).findActiveAirlines()
    }

    @Test
    fun `getAirlinesByCountry should return airlines for specific country`() {
        val indianAirlines = listOf(
            createValidAirline().copy(id = 1L, country = "India"),
            createValidAirline().copy(id = 2L, country = "India")
        )

        whenever(airlineDao.findByCountry("India")).thenReturn(indianAirlines)

        val result = airlineManager.getAirlinesByCountry("India")

        assertEquals(2, result.size)
        assertTrue(result.all { it.country == "India" })
        verify(airlineDao).findByCountry("India")
    }

    // Activate/Deactivate Tests
    @Test
    fun `activateAirline should set airline active status to true`() {
        val airline = createValidAirline().copy(id = 1L, active = false)
        val activatedAirline = airline.copy(active = true, updatedAt = LocalDateTime.now())

        whenever(airlineDao.findById(1L)).thenReturn(airline)
        whenever(airlineDao.update(any())).thenReturn(activatedAirline)

        val result = airlineManager.activateAirline(1L)

        assertTrue(result.active)
        assertNotNull(result.updatedAt)
        verify(airlineDao).findById(1L)
        verify(airlineDao).update(any())
    }

    @Test
    fun `deactivateAirline should set airline active status to false`() {
        val airline = createValidAirline().copy(id = 1L, active = true)
        val deactivatedAirline = airline.copy(active = false, updatedAt = LocalDateTime.now())

        whenever(airlineDao.findById(1L)).thenReturn(airline)
        whenever(airlineDao.update(any())).thenReturn(deactivatedAirline)

        val result = airlineManager.deactivateAirline(1L)

        assertTrue(!result.active)
        assertNotNull(result.updatedAt)
        verify(airlineDao).findById(1L)
        verify(airlineDao).update(any())
    }

    // Delete Tests
    @Test
    fun `deleteAirline should successfully delete airline`() {
        val airline = createValidAirline().copy(id = 1L)

        whenever(airlineDao.findById(1L)).thenReturn(airline)
        whenever(airlineDao.delete(1L)).thenReturn(true)

        val result = airlineManager.deleteAirline(1L)

        assertTrue(result)
        verify(airlineDao).findById(1L)
        verify(airlineDao).delete(1L)
    }

    @Test
    fun `deleteAirline should throw exception when airline not found`() {
        whenever(airlineDao.findById(999L)).thenReturn(null)

        val exception = assertThrows<ResourceNotFoundException> {
            airlineManager.deleteAirline(999L)
        }
        assertEquals("Airline with id 999 not found", exception.message)
    }

    private fun createValidAirline() = Airline(
        name = "Air India",
        code = "AI",
        country = "India",
        description = "Flag carrier of India",
        logoUrl = "https://example.com/logo.png",
        website = "https://airindia.com",
        active = true
    )
}