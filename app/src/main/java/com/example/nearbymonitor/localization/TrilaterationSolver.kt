package com.example.nearbymonitor.localization

import com.example.nearbymonitor.model.LocalizationEstimate
import com.example.nearbymonitor.model.LocalizationMeasurement
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

object TrilaterationSolver {
    fun rssiToDistance(
        rssi: Int,
        referenceRssiAt1m: Double,
        pathLossExponent: Double
    ): Double {
        val exponent = pathLossExponent.coerceAtLeast(1.1)
        val distance = 10.0.pow(
            (referenceRssiAt1m - rssi.toDouble()) / (10.0 * exponent)
        )
        return distance.coerceIn(0.10, 250.0)
    }

    fun estimate(
        measurements: List<LocalizationMeasurement>
    ): LocalizationEstimate? {
        if (measurements.size < 3) return null

        val p0 = measurements.first()
        val x0 = p0.xMeters
        val y0 = p0.yMeters
        val d0 = p0.estimatedDistanceMeters

        var ata00 = 0.0
        var ata01 = 0.0
        var ata11 = 0.0
        var atb0 = 0.0
        var atb1 = 0.0

        for (m in measurements.drop(1)) {
            val a0 = 2.0 * (m.xMeters - x0)
            val a1 = 2.0 * (m.yMeters - y0)

            val b =
                d0 * d0 -
                    m.estimatedDistanceMeters * m.estimatedDistanceMeters +
                    m.xMeters * m.xMeters -
                    x0 * x0 +
                    m.yMeters * m.yMeters -
                    y0 * y0

            ata00 += a0 * a0
            ata01 += a0 * a1
            ata11 += a1 * a1
            atb0 += a0 * b
            atb1 += a1 * b
        }

        val determinant = ata00 * ata11 - ata01 * ata01
        if (abs(determinant) < 1e-7) return null

        val x = (atb0 * ata11 - atb1 * ata01) / determinant
        val y = (ata00 * atb1 - ata01 * atb0) / determinant

        if (!x.isFinite() || !y.isFinite()) return null

        val residuals = measurements.map { m ->
            abs(
                hypot(x - m.xMeters, y - m.yMeters) -
                    m.estimatedDistanceMeters
            )
        }

        val rms = sqrt(
            residuals.sumOf { it * it } / residuals.size.toDouble()
        )

        val averageDistance =
            measurements.map { it.estimatedDistanceMeters }.average()
                .coerceAtLeast(0.25)

        val normalizedError = rms / averageDistance

        val quality = when {
            rms <= 0.75 && normalizedError <= 0.20 -> "Bonne"
            rms <= 1.75 && normalizedError <= 0.40 -> "Moyenne"
            else -> "Faible"
        }

        return LocalizationEstimate(
            xMeters = x,
            yMeters = y,
            rmsErrorMeters = rms,
            measurementCount = measurements.size,
            quality = quality
        )
    }
}
