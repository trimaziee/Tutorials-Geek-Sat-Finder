package com.example

import com.example.data.model.CityLocation
import com.example.data.model.Satellite
import com.example.util.SatMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ExampleUnitTest {

    @Test
    fun testPakSatAzimuthFromIslamabad() {
        // Islamabad: 33.6844° N, 73.0479° E
        // PakSat 1R: 38.0° E
        val azimuth = SatMath.calculateTrueAzimuth(33.6844, 73.0479, 38.0)
        // Expected: South-West (~231.7°)
        assertTrue("Azimuth should be between 230° and 233°, was $azimuth", azimuth in 230.0..233.0)
    }

    @Test
    fun testAsiaSat7AzimuthFromIslamabad() {
        // Islamabad: 33.6844° N, 73.0479° E
        // AsiaSat 7: 105.5° E
        val azimuth = SatMath.calculateTrueAzimuth(33.6844, 73.0479, 105.5)
        // Expected: South-East (~131.1°)
        assertTrue("Azimuth should be between 130° and 133°, was $azimuth", azimuth in 130.0..133.0)
    }

    @Test
    fun testAngleDifferenceShortestPath() {
        // Current: 350°, Target: 10° -> Turn Right 20° (+20)
        val diff1 = SatMath.calculateAngleDifference(350f, 10.0)
        assertEquals(20f, diff1, 0.1f)

        // Current: 10°, Target: 350° -> Turn Left 20° (-20)
        val diff2 = SatMath.calculateAngleDifference(10f, 350.0)
        assertEquals(-20f, diff2, 0.1f)

        // Current: 180°, Target: 180° -> Exact 0
        val diff3 = SatMath.calculateAngleDifference(180f, 180.0)
        assertEquals(0f, diff3, 0.1f)
    }

    @Test
    fun testSkewClockTextFormatting() {
        val clockStr = SatMath.formatSkewClock(-30.0)
        assertTrue("Clock string should indicate ~7:00 o'clock, got $clockStr", clockStr.contains("7:00") || clockStr.contains("7:"))
    }

    @Test
    fun testMultiLnbSeparationCalculation() {
        val separation = SatMath.calculateMultiLnbSeparationCm(
            primaryAzimuth = 131.0,
            primaryElevation = 42.0,
            secondaryAzimuth = 145.0,
            secondaryElevation = 45.0,
            focalLengthCm = 68.0
        )
        assertTrue("Multi-LNB separation should be positive realistic cm (10cm - 30cm), was $separation", separation in 5.0..40.0)
    }
}
