package com.example.nearbymonitor.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    indices = [Index(value = ["identityKey"])]
)
data class DeviceEntity(
    @PrimaryKey val id: String,
    val type: String,
    val name: String,
    val address: String,
    val known: Boolean,
    val alias: String,
    val notes: String,
    val firstSeen: Long,
    val lastSeen: Long,
    val detectionCount: Long,
    val rssi: Int?,
    val frequencyMhz: Int?,
    val channel: Int?,
    val security: String?,
    val manufacturer: String?,
    val manufacturerSource: String?,
    val manufacturerConfidence: Int,
    val manufacturerPrefix: String?,
    val macAddress: String?,
    val macLocallyAdministered: Boolean,
    val servicesCsv: String,
    val ipAddress: String?,
    val hostname: String?,
    val serviceType: String?,
    val port: Int?,
    val discoverySource: String?,
    val deviceCategory: String,
    val identificationConfidence: Int,
    val identificationEvidenceCsv: String,
    val identityKey: String?,
    val identitySource: String?,
    val identityConfidence: Int,
    val addressRandomized: Boolean,
    val identityAggregated: Boolean,
    val observedAddressesCsv: String
)
