package com.example.nearbymonitor.identification

import com.example.nearbymonitor.model.DeviceType
import com.example.nearbymonitor.model.NearbyDevice
import java.security.MessageDigest
import java.util.Locale

/**
 * V0.7 : corrélation prudente des équipements utilisant des adresses privées.
 *
 * Une adresse BLE RPA ne peut pas être "résolue" par une application quelconque
 * sans Identity Resolving Key (IRK). Cette classe ne prétend donc pas retrouver
 * l'identité cryptographique : elle construit une empreinte à partir de signaux
 * annoncés qui ont de bonnes chances de rester stables.
 *
 * Les cas sans indice suffisamment discriminant sont agrégés afin d'éviter
 * l'explosion de l'inventaire à chaque rotation de MAC.
 */
object DeviceIdentityResolver {
    data class Identity(
        val key: String?,
        val source: String?,
        val confidence: Int,
        val randomized: Boolean,
        val aggregated: Boolean
    )

    fun resolve(device: NearbyDevice): Identity {
        if (device.type == DeviceType.WIFI) {
            return Identity(null, null, 0, randomized = false, aggregated = false)
        }

        val randomized = device.addressRandomized ||
            device.macLocallyAdministered ||
            isLocallyAdministeredMac(device.macAddress)

        if (!randomized) {
            return Identity(null, null, 0, randomized = false, aggregated = false)
        }

        device.identityKeyHint?.takeIf { it.isNotBlank() }?.let { hint ->
            return Identity(
                key = hint,
                source = "Empreinte stable annoncée",
                confidence = device.identityHintConfidence.coerceIn(0, 100),
                randomized = true,
                aggregated = false
            )
        }

        val name = normalizedDistinctiveName(device.name)
        val components = buildList {
            name?.let { add("name:$it") }
            device.hostname?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() }?.let {
                add("host:$it")
            }
            device.manufacturer?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() }?.let {
                add("vendor:$it")
            }
            device.serviceType?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() }?.let {
                add("service:$it")
            }
            device.services
                .map { it.trim().lowercase(Locale.ROOT) }
                .filter { it.isNotBlank() }
                .sorted()
                .take(8)
                .forEach { add("uuid:$it") }
        }.distinct()

        val confidence = when (device.type) {
            DeviceType.BLE -> when {
                components.size >= 3 -> 88
                components.size == 2 -> 76
                components.size == 1 && name != null -> 68
                else -> 0
            }
            DeviceType.LAN -> when {
                !device.hostname.isNullOrBlank() && !device.serviceType.isNullOrBlank() -> 92
                !device.hostname.isNullOrBlank() -> 84
                !device.serviceType.isNullOrBlank() && name != null -> 78
                components.size >= 2 -> 72
                else -> 0
            }
            DeviceType.WIFI -> 0
        }

        if (confidence >= 65 && components.isNotEmpty()) {
            return Identity(
                key = fingerprint(device.type.name.lowercase(Locale.ROOT), components),
                source = when (device.type) {
                    DeviceType.BLE -> "Corrélation BLE : nom / Company ID / services"
                    DeviceType.LAN -> "Corrélation LAN : hostname / mDNS / services"
                    DeviceType.WIFI -> null
                },
                confidence = confidence,
                randomized = true,
                aggregated = false
            )
        }

        val bucket = when (device.type) {
            DeviceType.BLE -> "ble-private-unresolved"
            DeviceType.LAN -> "lan-private-unresolved:${ipv4Subnet(device.ipAddress ?: device.address)}"
            DeviceType.WIFI -> "wifi"
        }

        return Identity(
            key = fingerprint("aggregate", listOf(bucket)),
            source = "Agrégation MAC privée : identité insuffisante",
            confidence = 20,
            randomized = true,
            aggregated = true
        )
    }

    fun fingerprint(prefix: String, components: List<String>): String {
        val normalized = components
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .joinToString("|")

        val digest = MessageDigest.getInstance("SHA-256")
            .digest(normalized.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
            .take(20)

        return "$prefix:$digest"
    }

    fun isLocallyAdministeredMac(mac: String?): Boolean {
        val normalized = mac
            ?.filter { it.isLetterOrDigit() }
            ?.takeIf { it.length >= 2 }
            ?: return false

        val firstByte = normalized.take(2).toIntOrNull(16) ?: return false
        return (firstByte and 0x02) != 0
    }

    private fun normalizedDistinctiveName(value: String?): String? {
        val v = value?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() } ?: return null
        val generic = setOf(
            "ble inconnu",
            "appareil ble",
            "équipement lan",
            "equipement lan",
            "service réseau",
            "service reseau",
            "inconnu",
            "unknown"
        )
        return v.takeUnless { it in generic }
    }

    private fun ipv4Subnet(value: String?): String {
        val parts = value.orEmpty().split(".")
        return if (parts.size == 4) {
            "${parts[0]}.${parts[1]}.${parts[2]}.0/24"
        } else {
            "unknown"
        }
    }
}
