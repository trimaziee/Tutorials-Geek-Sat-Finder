package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SatelliteDao {

    @Query("SELECT * FROM satellites ORDER BY orbitalPosition ASC")
    fun getAllSatellites(): Flow<List<SatelliteEntity>>

    @Query("SELECT * FROM satellites WHERE isFavorite = 1 ORDER BY orbitalPosition ASC")
    fun getFavoriteSatellites(): Flow<List<SatelliteEntity>>

    @Query("SELECT * FROM satellites WHERE id = :id LIMIT 1")
    suspend fun getSatelliteById(id: Int): SatelliteEntity?

    @Query("SELECT * FROM satellites WHERE name LIKE '%' || :query || '%' OR popularChannels LIKE '%' || :query || '%' ORDER BY orbitalPosition ASC")
    fun searchSatellites(query: String): Flow<List<SatelliteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSatellite(satellite: SatelliteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSatellites(satellites: List<SatelliteEntity>)

    @Update
    suspend fun updateSatellite(satellite: SatelliteEntity)

    @Query("UPDATE satellites SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Delete
    suspend fun deleteSatellite(satellite: SatelliteEntity)

    @Query("SELECT COUNT(*) FROM satellites")
    suspend fun getCount(): Int
}
