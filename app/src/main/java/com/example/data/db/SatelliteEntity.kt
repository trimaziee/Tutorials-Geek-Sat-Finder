package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Satellite
import com.example.data.model.Transponder

@Entity(tableName = "satellites")
data class SatelliteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val orbitalPosition: Double,
    val band: String,
    val defaultLnbLo: Int,
    val recommendedDishSize: String,
    val popularChannels: String,
    val strongTransponder: String,
    val transpondersSerialized: String, // format: "freq|pol|sr|fec|standard|isStrong|desc;..."
    val coverageNotes: String,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
) {
    fun toDomainModel(): Satellite {
        val tpList = if (transpondersSerialized.isBlank()) {
            emptyList()
        } else {
            transpondersSerialized.split(";").mapNotNull { item ->
                val parts = item.split("|")
                if (parts.size >= 3) {
                    Transponder(
                        frequencyMhz = parts[0].toIntOrNull() ?: 0,
                        polarization = parts[1],
                        symbolRate = parts[2].toIntOrNull() ?: 0,
                        fec = parts.getOrNull(3) ?: "3/4",
                        standard = parts.getOrNull(4) ?: "DVB-S2",
                        isStrong = parts.getOrNull(5)?.toBooleanStrictOrNull() ?: false,
                        description = parts.getOrNull(6) ?: ""
                    )
                } else null
            }
        }

        return Satellite(
            id = id,
            name = name,
            orbitalPosition = orbitalPosition,
            band = band,
            defaultLnbLo = defaultLnbLo,
            recommendedDishSize = recommendedDishSize,
            popularChannels = popularChannels,
            strongTransponder = strongTransponder,
            transponders = tpList,
            coverageNotes = coverageNotes,
            isFavorite = isFavorite,
            isCustom = isCustom
        )
    }

    companion object {
        fun fromDomainModel(satellite: Satellite): SatelliteEntity {
            val serializedTps = satellite.transponders.joinToString(";") {
                "${it.frequencyMhz}|${it.polarization}|${it.symbolRate}|${it.fec}|${it.standard}|${it.isStrong}|${it.description}"
            }
            return SatelliteEntity(
                id = satellite.id,
                name = satellite.name,
                orbitalPosition = satellite.orbitalPosition,
                band = satellite.band,
                defaultLnbLo = satellite.defaultLnbLo,
                recommendedDishSize = satellite.recommendedDishSize,
                popularChannels = satellite.popularChannels,
                strongTransponder = satellite.strongTransponder,
                transpondersSerialized = serializedTps,
                coverageNotes = satellite.coverageNotes,
                isFavorite = satellite.isFavorite,
                isCustom = satellite.isCustom
            )
        }
    }
}
