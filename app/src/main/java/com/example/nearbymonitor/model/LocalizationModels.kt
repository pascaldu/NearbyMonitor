package com.example.nearbymonitor.model

data class LocalizationMeasurement(
    val id: Long,
    val deviceId: String,
    val xMeters: Double,
    val yMeters: Double,
    val rssi: Int,
    val referenceRssiAt1m: Double,
    val pathLossExponent: Double,
    val estimatedDistanceMeters: Double,
    val createdAt: Long
)

data class LocalizationEstimate(
    val xMeters: Double,
    val yMeters: Double,
    val rmsErrorMeters: Double,
    val measurementCount: Int,
    val quality: String
)

data class LocalizationData(
    val measurements: List<LocalizationMeasurement> = emptyList(),
    val estimate: LocalizationEstimate? = null
)
