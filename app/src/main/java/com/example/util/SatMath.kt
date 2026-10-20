package com.example.util

import android.hardware.GeomagneticField
import com.example.data.model.AlignmentCalculation
import com.example.data.model.CityLocation
import com.example.data.model.Satellite
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object SatMath {

    private const val EARTH_RADIUS_KM = 6378.137
    private const val GEO_ORBIT_RADIUS_KM = 42164.14
    private const val RATIO = EARTH_RADIUS_KM / GEO_ORBIT_RADIUS_KM // ~0.15127

    /**
     * Calculates complete alignment parameters for a satellite from observer's location.
     */
    fun calculateAlignment(
        satellite: Satellite,
        location: CityLocation,
        offsetAngle: Double = 22.5
    ): AlignmentCalculation {
        val userLatRad = Math.toRadians(location.latitude)
        val userLonRad = Math.toRadians(location.longitude)
        val satLonRad = Math.toRadians(satellite.orbitalPosition)

        val deltaLonRad = satLonRad - userLonRad

        // 1. Central angle beta
        val cosBeta = cos(userLatRad) * cos(deltaLonRad)
        val betaRad = acos(cosBeta.coerceIn(-1.0, 1.0))
        val sinBeta = sin(betaRad)

        // 2. Elevation angle
        val elevationRad = if (sinBeta == 0.0) {
            if (cosBeta >= RATIO) PI / 2.0 else -PI / 2.0
        } else {
            atan((cosBeta - RATIO) / sinBeta)
        }
        val elevationDeg = Math.toDegrees(elevationRad)
        val isVisible = elevationDeg > 0.0

        // 3. True Azimuth angle
        val trueAzimuthDeg = calculateTrueAzimuth(location.latitude, location.longitude, satellite.orbitalPosition)

        // 4. Magnetic Declination
        val geoField = try {
            GeomagneticField(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                location.altitudeMeters.toFloat().coerceAtLeast(0f),
                System.currentTimeMillis()
            )
        } catch (_: Exception) {
            null
        }
        val declination = geoField?.declination?.toDouble() ?: 0.0

        // Magnetic Azimuth = True Azimuth - Magnetic Declination
        val magneticAzimuthDeg = (trueAzimuthDeg - declination + 360.0) % 360.0

        // 5. LNB Polarization / Skew
        val skewDeg = calculateLnbSkew(location.latitude, location.longitude, satellite.orbitalPosition)
        val skewClockText = formatSkewClock(skewDeg)

        // 6. Dish Physical Face Tilt (Offset Dish correction)
        // Offset dish physical tilt = Elevation - Offset Angle
        val dishPhysicalTilt = (elevationDeg - offsetAngle).coerceAtLeast(0.0)

        // 7. Distance in KM
        val distanceKm = sqrt(
            EARTH_RADIUS_KM * EARTH_RADIUS_KM +
                GEO_ORBIT_RADIUS_KM * GEO_ORBIT_RADIUS_KM -
                2 * EARTH_RADIUS_KM * GEO_ORBIT_RADIUS_KM * cosBeta
        )

        return AlignmentCalculation(
            satelliteName = satellite.name,
            orbitalPositionFormatted = satellite.orbitalPositionFormatted,
            trueAzimuth = trueAzimuthDeg,
            magneticAzimuth = magneticAzimuthDeg,
            magneticDeclination = declination,
            elevation = elevationDeg,
            lnbSkew = skewDeg,
            skewClockText = skewClockText,
            dishPhysicalTilt = dishPhysicalTilt,
            offsetAngleUsed = offsetAngle,
            distanceKm = distanceKm,
            isVisible = isVisible
        )
    }

    /**
     * Calculates True Azimuth (0° to 360° clockwise from True North).
     */
    fun calculateTrueAzimuth(userLat: Double, userLon: Double, satLon: Double): Double {
        val userLatRad = Math.toRadians(userLat)
        val deltaLonRad = Math.toRadians(satLon - userLon)

        val tanDelta = tan(deltaLonRad)
        val sinLat = sin(userLatRad)

        var azimuthDeg = if (abs(sinLat) < 0.0001) {
            // Very close to equator
            if (satLon >= userLon) 90.0 else 270.0
        } else {
            val a = Math.toDegrees(atan(tanDelta / sinLat))
            if (userLat >= 0) {
                // Northern hemisphere: dish points southward
                180.0 - a
            } else {
                // Southern hemisphere: dish points northward
                (360.0 - a) % 360.0
            }
        }

        return (azimuthDeg + 360.0) % 360.0
    }

    /**
     * Calculates LNB Skew in degrees.
     * Negative values indicate clockwise rotation (facing the front of the dish towards LNB).
     * Positive values indicate counter-clockwise rotation.
     */
    fun calculateLnbSkew(userLat: Double, userLon: Double, satLon: Double): Double {
        val userLatRad = Math.toRadians(userLat)
        val deltaLonRad = Math.toRadians(satLon - userLon)

        val tanLat = tan(userLatRad)
        if (abs(tanLat) < 0.0001) return 0.0

        val skewRad = atan(sin(deltaLonRad) / tanLat)
        return Math.toDegrees(skewRad)
    }

    /**
     * Translates LNB Skew degrees into intuitive clock dial orientation
     * (looking at the LNB from front of dish, where 6 o'clock is cable straight down).
     */
    fun formatSkewClock(skewDeg: Double): String {
        // Standard LNB has F-connector at 6 o'clock (0° skew) or 12 o'clock.
        // If cable points down (6 o'clock = 180°):
        // Each hour is 30 degrees, each minute is 0.5 degrees.
        // Skew of -30° rotates to 7 o'clock.
        // Skew of +30° rotates to 5 o'clock.
        val rotationDeg = -skewDeg // Clockwise is positive time progression
        val minuteShift = (rotationDeg / 30.0) * 60.0
        val baseMinutes = 6 * 60 // 6:00 is 360 minutes
        var totalMinutes = (baseMinutes + minuteShift.roundToInt()) % (12 * 60)
        if (totalMinutes < 0) totalMinutes += 12 * 60

        val hours = (totalMinutes / 60).let { if (it == 0) 12 else it }
        val minutes = totalMinutes % 60
        val direction = if (skewDeg < -0.5) "Clockwise" else if (skewDeg > 0.5) "Counter-Clockwise" else "Centered"

        return String.format("%d:%02d o'clock (%s)", hours, minutes, direction)
    }

    /**
     * Calculates shortest angle difference between current heading and target azimuth (-180..+180).
     * Positive means turn right (clockwise), negative means turn left (counter-clockwise).
     */
    fun calculateAngleDifference(currentHeading: Float, targetAzimuth: Double): Float {
        var diff = (targetAzimuth.toFloat() - currentHeading) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }

    /**
     * Calculates multi-LNB separation distance in centimeters for a dual LNB bracket.
     * Formula: Separation = Dish Focal Length * tan(Angular Separation)
     * Standard 6ft C-band dish focal length ~68cm, 60cm Ku dish ~38cm.
     */
    fun calculateMultiLnbSeparationCm(
        primaryAzimuth: Double,
        primaryElevation: Double,
        secondaryAzimuth: Double,
        secondaryElevation: Double,
        focalLengthCm: Double = 50.0
    ): Double {
        val azDiffRad = Math.toRadians(secondaryAzimuth - primaryAzimuth)
        val elDiffRad = Math.toRadians(secondaryElevation - primaryElevation)
        val angularSeparationRad = sqrt(azDiffRad * azDiffRad + elDiffRad * elDiffRad)
        return focalLengthCm * tan(angularSeparationRad)
    }
}
