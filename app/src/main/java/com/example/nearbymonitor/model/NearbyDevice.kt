package com.example.nearbymonitor.model

enum class DeviceType {
    WIFI,
    BLE,
    LAN
}

data class NearbyDevice(
    val id: String,
    val type: DeviceType,
    val name: String,
    val address: String,
    val rssi: Int? = null,
    val firstSeen: Long = System.currentTimeMillis(),
    val lastSeen: Long = System.currentTimeMillis(),
    val known: Boolean = false,
    val alias: String = "",
    val notes: String = "",
    val detectionCount: Long = 0,
    val frequencyMhz: Int? = null,
    val channel: Int? = null,
    val security: String? = null,
    val manufacturer: String? = null,
    val manufacturerSource: String? = null,
    val manufacturerConfidence: Int = 0,
    val manufacturerPrefix: String? = null,
    val macAddress: String? = null,
    val macLocallyAdministered: Boolean = false,
    val services: List<String> = emptyList(),
    val ipAddress: String? = null,
    val hostname: String? = null,
    val serviceType: String? = null,
    val port: Int? = null,
    val discoverySource: String? = null,
    val deviceCategory: String = "Inconnu",
    val identificationConfidence: Int = 0,
    val identificationEvidence: List<String> = emptyList(),
    val identityKey: String? = null,
    val identitySource: String? = null,
    val identityConfidence: Int = 0,
    val addressRandomized: Boolean = false,
    val identityAggregated: Boolean = false,
    val observedAddresses: List<String> = emptyList(),
    val identityKeyHint: String? = null,
    val identityHintConfidence: Int = 0
) {
    val displayName: String
        get() = alias.trim().ifBlank { name }
}

data class DeviceObservation(
    val id: Long,
    val deviceId: String,
    val seenAt: Long,
    val rssi: Int?,
    val ipAddress: String?,
    val discoverySource: String?
)
