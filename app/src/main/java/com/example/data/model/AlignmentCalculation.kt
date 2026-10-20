package com.example.data.model

data class AlignmentCalculation(
    val satelliteName: String,
    val orbitalPositionFormatted: String,
    val trueAzimuth: Double,
    val magneticAzimuth: Double,
    val magneticDeclination: Double,
    val elevation: Double,
    val lnbSkew: Double,
    val skewClockText: String,
    val dishPhysicalTilt: Double,
    val offsetAngleUsed: Double,
    val distanceKm: Double,
    val isVisible: Boolean
) {
    val cardinalDirection: String
        get() = getCardinal(trueAzimuth)

    companion object {
        fun getCardinal(azimuth: Double): String {
            val directions = arrayOf(
                "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
            )
            val index = (((azimuth % 360) + 360) % 360 / 22.5 + 0.5).toInt() % 16
            return directions[index]
        }
    }
}
