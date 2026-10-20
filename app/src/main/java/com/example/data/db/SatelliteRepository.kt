package com.example.data.db

import com.example.data.model.Satellite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SatelliteRepository(
    private val satelliteDao: SatelliteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val allSatellites: Flow<List<Satellite>> = satelliteDao.getAllSatellites().map { list ->
        list.map { it.toDomainModel() }
    }

    val favoriteSatellites: Flow<List<Satellite>> = satelliteDao.getFavoriteSatellites().map { list ->
        list.map { it.toDomainModel() }
    }

    suspend fun initializePresetsIfEmpty() = withContext(ioDispatcher) {
        val count = satelliteDao.getCount()
        if (count == 0) {
            val entities = PresetSatellites.PRESET_LIST.map { SatelliteEntity.fromDomainModel(it) }
            satelliteDao.insertSatellites(entities)
        }
    }

    suspend fun getSatelliteById(id: Int): Satellite? = withContext(ioDispatcher) {
        satelliteDao.getSatelliteById(id)?.toDomainModel()
    }

    fun searchSatellites(query: String): Flow<List<Satellite>> {
        return satelliteDao.searchSatellites(query).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) = withContext(ioDispatcher) {
        satelliteDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun saveCustomSatellite(satellite: Satellite) = withContext(ioDispatcher) {
        val entity = SatelliteEntity.fromDomainModel(satellite.copy(isCustom = true))
        satelliteDao.insertSatellite(entity)
    }

    suspend fun deleteSatellite(satellite: Satellite) = withContext(ioDispatcher) {
        satelliteDao.deleteSatellite(SatelliteEntity.fromDomainModel(satellite))
    }
}
