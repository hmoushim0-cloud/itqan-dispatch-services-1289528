package sa.factory.itqandispatchservi

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import sa.factory.itqandispatchservi.data.repository.ItqanDispatchRepositoryImpl
import sa.factory.itqandispatchservi.engine.GpsEmergencyDispatchEngine
import sa.factory.itqandispatchservi.engine.GpsEmergencyDispatchEngine.EmergencyResponder
import sa.factory.itqandispatchservi.engine.GpsEmergencyDispatchEngine.GeofenceSafeZone
import sa.factory.itqandispatchservi.engine.GpsEmergencyDispatchEngine.SosPriorityLevel
import sa.factory.itqandispatchservi.engine.MadaContactlessEmvL2Engine
import sa.factory.itqandispatchservi.hardware.HardwareSensorDriver
import sa.factory.itqandispatchservi.model.ItqanDispatchItem

class ItqanDispatchRegressionTest {

    // 1. Checkout & Tax Calculation
    @Test
    fun testCheckoutCalculationAndTax() = runBlocking {
        val sensorDriver = HardwareSensorDriver()
        val repository = ItqanDispatchRepositoryImpl(sensorDriver)

        val item1 = ItqanDispatchItem("t1", "خدمة تجريبية أ", "الوصف", 100.0, "معتمد", "عام")
        val item2 = ItqanDispatchItem("t2", "خدمة تجريبية ب", "الوصف", 200.0, "معتمد", "عام")

        repository.addToCart(item1)
        repository.addToCart(item2)

        val order = repository.checkout("Mada EMV")

        assertEquals(300.0, order.subtotalSar, 0.01)
        assertEquals(45.0, order.vatSar, 0.01)
        assertEquals(345.0, order.totalSar, 0.01)
        assertEquals(0, repository.activeCart.value.size)
    }

    // 2. ISO 7816-4 PPSE APDU command generation
    @Test
    fun testSelectPpseApduFormatting() {
        val emvEngine = MadaContactlessEmvL2Engine()
        val apdu = emvEngine.buildSelectPpseApdu()

        assertEquals(0x00.toByte(), apdu[0]) // CLA
        assertEquals(0xA4.toByte(), apdu[1]) // INS: SELECT
        assertEquals(0x04.toByte(), apdu[2]) // P1
        assertEquals(0x00.toByte(), apdu[3]) // P2
        assertEquals(14.toByte(), apdu[4])   // Lc length of "2PAY.SYS.DDF01"
        assertEquals(0x00.toByte(), apdu[apdu.size - 1]) // Le
    }

    // 3. ISO 7816-4 Mada AID APDU generation
    @Test
    fun testSelectMadaAidApdu() {
        val emvEngine = MadaContactlessEmvL2Engine()
        val apdu = emvEngine.buildSelectAidApdu(MadaContactlessEmvL2Engine.MADA_AID_PRIMARY)

        assertEquals(0x00.toByte(), apdu[0])
        assertEquals(0xA4.toByte(), apdu[1])
        assertEquals(7.toByte(), apdu[4]) // MADA AID is 7 bytes (14 hex chars)
        assertEquals(13, apdu.size)
    }

    // 4. BER-TLV parser extraction
    @Test
    fun testBerTlvParser() {
        val emvEngine = MadaContactlessEmvL2Engine()
        // Simulated TLV payload: Tag 9F36 (ATC, 2 bytes: 002A) + Tag 9F26 (AC, 8 bytes: 1122334455667788)
        val payloadHex = "9F3602002A9F26081122334455667788"
        val parsed = emvEngine.parseBerTlv(payloadHex)

        assertEquals("002A", parsed["9F36"])
        assertEquals("1122334455667788", parsed["9F26"])
    }

    // 5. SAMA Mada contactless limit: <= 300 SAR does not require CVM
    @Test
    fun testMadaContactlessNoPinFloorLimitBelow300() {
        val emvEngine = MadaContactlessEmvL2Engine()
        assertFalse(emvEngine.evaluateCvmRequirement(50.0))
        assertFalse(emvEngine.evaluateCvmRequirement(300.00))
    }

    // 6. SAMA Mada contactless limit: > 300 SAR requires CVM PIN/Biometric
    @Test
    fun testMadaContactlessPinRequiredAbove300() {
        val emvEngine = MadaContactlessEmvL2Engine()
        assertTrue(emvEngine.evaluateCvmRequirement(300.01))
        assertTrue(emvEngine.evaluateCvmRequirement(750.00))
    }

    // 7. End-to-end Mada Contactless L2 processing and cryptogram verification
    @Test
    fun testMadaContactlessTransactionApprovalAndVat() {
        val emvEngine = MadaContactlessEmvL2Engine()
        val aids = listOf(MadaContactlessEmvL2Engine.MADA_AID_PRIMARY, "A0000000041010")
        val tlvMap = mapOf(
            "9F36" to "005F",
            "9F26" to "DEADBEEFCAFE1234"
        )

        val result = emvEngine.processContactlessTransaction(
            amountSar = 200.0,
            presentedAids = aids,
            tlvTags = tlvMap
        )

        assertTrue(result.isApproved)
        assertEquals("APPROVED", result.statusCode)
        assertEquals(MadaContactlessEmvL2Engine.MADA_AID_PRIMARY, result.selectedAid)
        assertEquals(95, result.atc)
        assertFalse(result.cvmRequired)
        assertEquals(230.0, result.totalWithVatSar, 0.01)
        assertNotNull(result.verificationHash)
        assertTrue(result.verificationHash.length >= 64)
    }

    // 8. GPS Geodesic distance & bearing calculation via Haversine
    @Test
    fun testGeodesicDistanceAndBearingHaversine() {
        val gpsEngine = GpsEmergencyDispatchEngine()
        // Riyadh Kingdom Centre -> Al Faisaliah Tower
        val kingdomLat = 24.7114
        val kingdomLon = 46.6744
        val faisaliahLat = 24.6902
        val faisaliahLon = 46.6853

        val distanceKm = gpsEngine.calculateDistanceKm(kingdomLat, kingdomLon, faisaliahLat, faisaliahLon)
        val bearing = gpsEngine.calculateBearingDegrees(kingdomLat, kingdomLon, faisaliahLat, faisaliahLon)

        // Distance should be approximately 2.5 - 2.8 km
        assertTrue(distanceKm in 2.2..3.0)
        // Direction is South-South-East (bearing roughly 140 - 170 degrees)
        assertTrue(bearing in 130.0..180.0)
    }

    // 9. Geofence Safe Zone Detection
    @Test
    fun testGeofenceSafeZoneDetection() {
        val gpsEngine = GpsEmergencyDispatchEngine()
        val safeZone = GeofenceSafeZone(
            zoneId = "ZONE_RIYADH_MED_01",
            name = "Riyadh King Fahd Medical City Zone",
            centerLat = 24.6975,
            centerLon = 46.7025,
            radiusMeters = 800.0
        )

        // Inside zone (50m away)
        assertTrue(gpsEngine.isInsideGeofence(24.6977, 46.7026, safeZone))

        // Outside zone (5km away)
        assertFalse(gpsEngine.isInsideGeofence(24.7500, 46.7500, safeZone))
    }

    // 10. Emergency Triage Classification and Nearest Ambulance Dispatch
    @Test
    fun testSosTriageClassificationAndNearestAmbulanceDispatch() {
        val gpsEngine = GpsEmergencyDispatchEngine()

        // Test triage priority logic
        val criticalPriority = gpsEngine.evaluateTriagePriority(
            heartRateBpm = 165,
            isConscious = false,
            hasSevereBleeding = true,
            explicitSosTriggered = true
        )
        assertEquals(SosPriorityLevel.CODE_RED, criticalPriority)

        // Test nearest responder assignment
        val ambulanceUnits = listOf(
            EmergencyResponder("AMB-101", "Al-Olaya Alpha", 24.7000, 46.6800, isAvailable = true),
            EmergencyResponder("AMB-102", "Al-Malaz Bravo", 24.6600, 46.7200, isAvailable = true),
            EmergencyResponder("AMB-103", "Al-Nakheel Charlie", 24.7500, 46.6300, isAvailable = false)
        )

        val patientLat = 24.7050
        val patientLon = 24.6820 // near Olaya

        val assignment = gpsEngine.assignNearestResponder(
            patientId = "PATIENT-8891",
            patientLat = patientLat,
            patientLon = patientLon,
            priority = criticalPriority,
            fleet = ambulanceUnits
        )

        assertNotNull(assignment)
        assertEquals("AMB-101", assignment?.responder?.unitId)
        assertTrue(assignment!!.etaMinutes > 0.0)
        assertNotNull(assignment.securityToken)
        assertTrue(assignment.securityToken.length >= 64)
    }
}
