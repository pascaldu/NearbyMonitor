package com.example.nearbymonitor.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location_measurements",
    indices = [
        Index(value = ["deviceId"]),
        Index(value = ["createdAt"])
    ]
)
data class LocationMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: String,
    val xMeters: Double,
    val yMeters: Double,
    val rssi: Int,
    val referenceRssiAt1m: Double,
    val pathLossExponent: Double,
    val estimatedDistanceMeters: Double,
    val createdAt: Long
)
