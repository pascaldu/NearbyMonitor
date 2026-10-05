package com.example.nearbymonitor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitorDao {
    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun observeDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :deviceId LIMIT 1")
    suspend fun getDevice(deviceId: String): DeviceEntity?

    @Query("""
        SELECT * FROM devices
        WHERE identityKey = :identityKey
        ORDER BY known DESC, lastSeen DESC
        LIMIT 1
    """)
    suspend fun getDeviceByIdentityKey(identityKey: String): DeviceEntity?

    @Query("SELECT * FROM devices")
    suspend fun getAllDevices(): List<DeviceEntity>

    @Upsert
    suspend fun upsertDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObservation(observation: ObservationEntity)

    @Insert
    suspend fun insertLocationMeasurement(measurement: LocationMeasurementEntity): Long

    @Query("SELECT * FROM location_measurements WHERE deviceId = :deviceId ORDER BY createdAt ASC")
    suspend fun locationMeasurements(deviceId: String): List<LocationMeasurementEntity>

    @Query("DELETE FROM location_measurements WHERE deviceId = :deviceId")
    suspend fun clearLocationMeasurements(deviceId: String)

    @Query("UPDATE location_measurements SET deviceId = :targetId WHERE deviceId = :sourceId")
    suspend fun moveLocationMeasurements(sourceId: String, targetId: String)

    @Query("SELECT MAX(seenAt) FROM observations WHERE deviceId = :deviceId")
    suspend fun latestObservationTime(deviceId: String): Long?

    @Query("SELECT * FROM observations WHERE deviceId = :deviceId ORDER BY seenAt DESC LIMIT :limit")
    suspend fun recentObservations(deviceId: String, limit: Int): List<ObservationEntity>

    @Query("UPDATE devices SET known = :known WHERE id = :deviceId")
    suspend fun setKnown(deviceId: String, known: Boolean)

    @Query("UPDATE devices SET alias = :alias, notes = :notes WHERE id = :deviceId")
    suspend fun updateProfile(deviceId: String, alias: String, notes: String)

    @Query("UPDATE observations SET deviceId = :targetId WHERE deviceId = :sourceId")
    suspend fun moveObservations(sourceId: String, targetId: String)

    @Query("DELETE FROM devices WHERE id = :deviceId")
    suspend fun deleteDevice(deviceId: String)

    @Query("DELETE FROM observations WHERE deviceId = :deviceId")
    suspend fun clearObservations(deviceId: String)

    @Query("DELETE FROM observations")
    suspend fun clearAllObservations()
}
