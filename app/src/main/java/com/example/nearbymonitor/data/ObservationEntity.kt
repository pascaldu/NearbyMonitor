package com.example.nearbymonitor.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "observations",
    indices = [Index(value = ["deviceId"]), Index(value = ["seenAt"])]
)
data class ObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: String,
    val seenAt: Long,
    val rssi: Int?,
    val ipAddress: String?,
    val discoverySource: String?
)
