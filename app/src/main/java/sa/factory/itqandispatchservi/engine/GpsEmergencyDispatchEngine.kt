package sa.factory.itqandispatchservi.engine

import java.security.MessageDigest
import java.util.Locale
import kotlin.math.*

/**
 * GpsEmergencyDispatchEngine
 * Production domain engine for Real-Time GPS SOS dispatch, geodesic triage matching,
 * and SLA route calculations for ambulance and health guardian services.
 *
 * Strict zero-emoji compliance.
 */
class GpsEmergencyDispatchEngine {

    companion object {
        const val EARTH_RADIUS_KM = 6371.0
        const val DEFAULT_EMERGENCY_SPEED_KMH = 65.0
    }

    enum class SosPriorityLevel(val code: String, val maxSlaMinutes: Int) {
        CODE_RED("CRITICAL_SOS", 8),
        CODE_YELLOW("URGENT_TRIAGE", 20),
        CODE_GREEN("STANDARD_ADVISORY", 60)
    }

    data class GeofenceSafeZone(
        val zoneId: String,
        val name: String,
        val centerLat: Double,
        val centerLon: Double,
        val radiusMeters: Double
    )

    data class EmergencyResponder(
        val unitId: String,
        val callSign: String,
        val lat: Double,
        val lon: Double,
        val isAvailable: Boolean,
        val unitType: String = "AMBULANCE"
    )

    data class DispatchAssignment(
        val patientId: String,
        val responder: EmergencyResponder,
        val distanceKm: Double,
        val etaMinutes: Double,
        val priority: SosPriorityLevel,
        val routeBearingDeg: Double,
        val securityToken: String,
        val dispatchStatus: String
    )

    /**
     * Calculates geodesic distance between two coordinate pairs using the Haversine formula.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Calculates initial forward bearing from point 1 to point 2 in degrees (0..359).
     */
    fun calculateBearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLonRad = Math.toRadians(lon2 - lon1)

        val y = sin(dLonRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLonRad)
        val bearingRad = atan2(y, x)
        return (Math.toDegrees(bearingRad) + 360.0) % 360.0
    }

    /**
     * Determines whether the patient coordinates reside within a registered Geofence safe zone.
     */
    fun isInsideGeofence(patientLat: Double, patientLon: Double, zone: GeofenceSafeZone): Boolean {
        val distanceKm = calculateDistanceKm(patientLat, patientLon, zone.centerLat, zone.centerLon)
        val distanceMeters = distanceKm * 1000.0
        return distanceMeters <= zone.radiusMeters
    }

    /**
     * Triages incoming health telemetry and distress signals into Saudi Red Crescent urgency levels.
     */
    fun evaluateTriagePriority(
        heartRateBpm: Int,
        isConscious: Boolean,
        hasSevereBleeding: Boolean,
        explicitSosTriggered: Boolean
    ): SosPriorityLevel {
        if (!isConscious || hasSevereBleeding || (explicitSosTriggered && (heartRateBpm < 40 || heartRateBpm > 140))) {
            return SosPriorityLevel.CODE_RED
        }
        if (explicitSosTriggered || heartRateBpm < 50 || heartRateBpm > 120) {
            return SosPriorityLevel.CODE_YELLOW
        }
        return SosPriorityLevel.CODE_GREEN
    }

    /**
     * Finds the closest available emergency responder, projects arrival ETA, and computes SHA-256 dispatch token.
     */
    fun assignNearestResponder(
        patientId: String,
        patientLat: Double,
        patientLon: Double,
        priority: SosPriorityLevel,
        fleet: List<EmergencyResponder>,
        vehicleSpeedKmh: Double = DEFAULT_EMERGENCY_SPEED_KMH
    ): DispatchAssignment? {
        val availableUnits = fleet.filter { it.isAvailable }
        if (availableUnits.isEmpty()) return null

        var bestUnit: EmergencyResponder? = null
        var minDistanceKm = Double.MAX_VALUE

        for (unit in availableUnits) {
            val dist = calculateDistanceKm(patientLat, patientLon, unit.lat, unit.lon)
            if (dist < minDistanceKm) {
                minDistanceKm = dist
                bestUnit = unit
            }
        }

        val responder = bestUnit ?: return null
        val bearing = calculateBearingDegrees(responder.lat, responder.lon, patientLat, patientLon)
        val effectiveSpeed = if (priority == SosPriorityLevel.CODE_RED) vehicleSpeedKmh * 1.2 else vehicleSpeedKmh
        val etaMinutes = (minDistanceKm / effectiveSpeed) * 60.0

        val digestPayload = "$patientId:${responder.unitId}:$minDistanceKm:${priority.code}:${System.currentTimeMillis()}"
        val digest = MessageDigest.getInstance("SHA-256")
        val tokenBytes = digest.digest(digestPayload.toByteArray(Charsets.UTF_8))
        val token = tokenBytes.joinToString("") { "%02X".format(it) }

        val status = if (etaMinutes <= priority.maxSlaMinutes) "[OK] SLA_MET" else "[WARN] SLA_RISK"

        return DispatchAssignment(
            patientId = patientId,
            responder = responder,
            distanceKm = minDistanceKm,
            etaMinutes = etaMinutes,
            priority = priority,
            routeBearingDeg = bearing,
            securityToken = token,
            dispatchStatus = status
        )
    }
}
