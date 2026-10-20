package com.example.data.model

data class Transponder(
    val frequencyMhz: Int,
    val polarization: String, // "H" or "V"
    val symbolRate: Int,
    val fec: String = "3/4",
    val standard: String = "DVB-S2",
    val isStrong: Boolean = false,
    val description: String = ""
) {
    fun toDisplayText(): String = "$frequencyMhz $polarization $symbolRate"
}

data class Satellite(
    val id: Int,
    val name: String,
    val orbitalPosition: Double, // positive for East, negative for West
    val band: String, // "C-Band", "Ku-Band", "Dual C/Ku"
    val defaultLnbLo: Int, // 5150 for C-Band, 9750/10600 for Ku
    val recommendedDishSize: String,
    val popularChannels: String,
    val strongTransponder: String,
    val transponders: List<Transponder> = emptyList(),
    val coverageNotes: String,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
) {
    val orbitalPositionFormatted: String
        get() {
            val direction = if (orbitalPosition >= 0) "E" else "W"
            val absVal = kotlin.math.abs(orbitalPosition)
            return if (absVal % 1.0 == 0.0) {
                "${absVal.toInt()}°$direction"
            } else {
                "${absVal}°$direction"
            }
        }
}
