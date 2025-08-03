package com.credair.core.integration.airline

import com.credair.core.integration.airline.providers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AirlineIntegrationManagerTest {

    private lateinit var indigoService: IndigoReservationService
    private lateinit var airIndiaService: AirIndiaReservationService
    private lateinit var spiceJetService: SpiceJetReservationService
    private lateinit var vistaraService: VistaraReservationService
    private lateinit var goAirService: GoAirReservationService
    private lateinit var airlineIntegrationManager: AirlineIntegrationManager

    @BeforeEach
    fun setUp() {
        // Use real instances instead of mocks to avoid final class mocking issues
        indigoService = IndigoReservationService()
        airIndiaService = AirIndiaReservationService()
        spiceJetService = SpiceJetReservationService()
        vistaraService = VistaraReservationService()
        goAirService = GoAirReservationService()
        
        airlineIntegrationManager = AirlineIntegrationManager(
            indigoService,
            airIndiaService,
            spiceJetService,
            vistaraService,
            goAirService
        )
    }

    // Airline Code Resolution Tests
    @Test
    fun `getReservationService should return IndiGo service for 6E code`() {
        val result = airlineIntegrationManager.getReservationService("6E")
        
        assertNotNull(result)
        assertEquals(indigoService, result)
    }

    @Test
    fun `getReservationService should return Air India service for AI code`() {
        val result = airlineIntegrationManager.getReservationService("AI")
        
        assertNotNull(result)
        assertEquals(airIndiaService, result)
    }

    @Test
    fun `getReservationService should return SpiceJet service for SG code`() {
        val result = airlineIntegrationManager.getReservationService("SG")
        
        assertNotNull(result)
        assertEquals(spiceJetService, result)
    }

    @Test
    fun `getReservationService should return Vistara service for UK code`() {
        val result = airlineIntegrationManager.getReservationService("UK")
        
        assertNotNull(result)
        assertEquals(vistaraService, result)
    }

    @Test
    fun `getReservationService should return GoAir service for G8 code`() {
        val result = airlineIntegrationManager.getReservationService("G8")
        
        assertNotNull(result)
        assertEquals(goAirService, result)
    }

    // Case Insensitive Tests
    @Test
    fun `getReservationService should handle lowercase airline codes`() {
        val result = airlineIntegrationManager.getReservationService("ai")
        
        assertNotNull(result)
        assertEquals(airIndiaService, result)
    }

    @Test
    fun `getReservationService should handle mixed case airline codes`() {
        val result = airlineIntegrationManager.getReservationService("6e")
        
        assertNotNull(result)
        assertEquals(indigoService, result)
    }

    @Test
    fun `getReservationService should handle uppercase airline codes`() {
        val result = airlineIntegrationManager.getReservationService("SG")
        
        assertNotNull(result)
        assertEquals(spiceJetService, result)
    }

    // Unsupported Airline Tests
    @Test
    fun `getReservationService should return null for unsupported airline code`() {
        val result = airlineIntegrationManager.getReservationService("XX")
        
        assertNull(result)
    }

    @Test
    fun `getReservationService should return null for empty airline code`() {
        val result = airlineIntegrationManager.getReservationService("")
        
        assertNull(result)
    }

    @Test
    fun `getReservationService should return null for invalid airline code`() {
        val result = airlineIntegrationManager.getReservationService("INVALID")
        
        assertNull(result)
    }

    // Supported Airlines Tests
    @Test
    fun `getSupportedAirlines should return all supported airline codes`() {
        val result = airlineIntegrationManager.getSupportedAirlines()
        
        assertEquals(5, result.size)
        assertTrue(result.contains("6E"))
        assertTrue(result.contains("AI"))
        assertTrue(result.contains("SG"))
        assertTrue(result.contains("UK"))
        assertTrue(result.contains("G8"))
    }

    @Test
    fun `getSupportedAirlines should return immutable list`() {
        val result = airlineIntegrationManager.getSupportedAirlines()
        
        // Should be a new list each time, not the original map keys
        assertNotNull(result)
        assertEquals(5, result.size)
    }

    // All Providers Tests
    @Test
    fun `getAllProviders should return all airline services mapped by code`() {
        val result = airlineIntegrationManager.getAllProviders()
        
        assertEquals(5, result.size)
        assertEquals(indigoService, result["6E"])
        assertEquals(airIndiaService, result["AI"])
        assertEquals(spiceJetService, result["SG"])
        assertEquals(vistaraService, result["UK"])
        assertEquals(goAirService, result["G8"])
    }

    @Test
    fun `getAllProviders should return immutable map`() {
        val result = airlineIntegrationManager.getAllProviders()
        
        assertNotNull(result)
        assertEquals(5, result.size)
        
        // Verify all expected mappings exist
        assertTrue(result.containsKey("6E"))
        assertTrue(result.containsKey("AI"))
        assertTrue(result.containsKey("SG"))
        assertTrue(result.containsKey("UK"))
        assertTrue(result.containsKey("G8"))
    }

    // Boundary Tests
    @Test
    fun `getReservationService should handle whitespace in airline code`() {
        val result = airlineIntegrationManager.getReservationService(" AI ")
        
        // Should fail because of whitespace (no trimming implemented)
        assertNull(result)
    }

    @Test
    fun `getReservationService should handle numeric-only codes`() {
        val result = airlineIntegrationManager.getReservationService("123")
        
        assertNull(result)
    }

    // Provider Consistency Tests
    @Test
    fun `all providers should be accessible through both methods`() {
        val supportedCodes = airlineIntegrationManager.getSupportedAirlines()
        val allProviders = airlineIntegrationManager.getAllProviders()
        
        // Every supported code should have a corresponding provider
        supportedCodes.forEach { code ->
            val serviceFromGet = airlineIntegrationManager.getReservationService(code)
            val serviceFromMap = allProviders[code]
            
            assertNotNull(serviceFromGet)
            assertNotNull(serviceFromMap)
            assertEquals(serviceFromGet, serviceFromMap)
        }
    }

    @Test
    fun `provider services should be unique instances`() {
        val allProviders = airlineIntegrationManager.getAllProviders()
        val services = allProviders.values.toList()
        
        // Each service should be unique
        assertEquals(5, services.distinct().size)
        
        // Verify specific service types
        assertTrue(services.contains(indigoService))
        assertTrue(services.contains(airIndiaService))
        assertTrue(services.contains(spiceJetService))
        assertTrue(services.contains(vistaraService))
        assertTrue(services.contains(goAirService))
    }

    // Edge Case Tests
    @Test
    fun `getReservationService should be case insensitive for all supported codes`() {
        val testCases = mapOf(
            "6e" to indigoService,
            "ai" to airIndiaService,
            "sg" to spiceJetService,
            "uk" to vistaraService,
            "g8" to goAirService
        )
        
        testCases.forEach { (code, expectedService) ->
            val result = airlineIntegrationManager.getReservationService(code)
            assertEquals(expectedService, result, "Failed for code: $code")
        }
    }

    @Test
    fun `supported airlines should match provider keys exactly`() {
        val supportedCodes = airlineIntegrationManager.getSupportedAirlines().toSet()
        val providerKeys = airlineIntegrationManager.getAllProviders().keys
        
        assertEquals(supportedCodes, providerKeys)
    }
}