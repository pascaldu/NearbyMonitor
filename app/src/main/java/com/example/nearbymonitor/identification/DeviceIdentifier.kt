package com.example.nearbymonitor.identification

import com.example.nearbymonitor.model.DeviceType
import com.example.nearbymonitor.model.NearbyDevice

data class IdentificationResult(
    val manufacturer: String?,
    val category: String,
    val confidence: Int,
    val evidence: List<String>
)

object DeviceIdentifier {
    fun identify(device: NearbyDevice): IdentificationResult {
        val evidence = mutableListOf<String>()
        val corpus = buildString {
            append(device.name).append(' ')
            append(device.alias).append(' ')
            append(device.hostname.orEmpty()).append(' ')
            append(device.manufacturer.orEmpty()).append(' ')
            append(device.serviceType.orEmpty()).append(' ')
            append(device.services.joinToString(" "))
        }.lowercase()

        val manufacturer = inferManufacturer(device, corpus, evidence)
        val category = inferCategory(device, corpus, evidence)
        val confidence = confidence(category, manufacturer, evidence)

        return IdentificationResult(
            manufacturer = manufacturer,
            category = category,
            confidence = confidence,
            evidence = evidence.distinct().take(8)
        )
    }

    private fun inferManufacturer(
        device: NearbyDevice,
        corpus: String,
        evidence: MutableList<String>
    ): String? {
        val existing = device.manufacturer
            ?.takeIf { it.isNotBlank() && !it.startsWith("ID ") && !it.startsWith("Bluetooth Company ID") }
        if (existing != null) {
            evidence += "Fabricant annoncé : $existing"
            return existing
        }

        val rules = listOf(
            listOf("iphone", "ipad", "apple tv", "airplay", "homepod", "apple") to "Apple",
            listOf("samsung", "galaxy", "smartthings") to "Samsung",
            listOf("pixel", "googlecast", "chromecast", "google nest", "nest") to "Google",
            listOf("freebox", "freebox server", "freebox player") to "Free",
            listOf("livebox", "orange") to "Orange",
            listOf("bbox", "bouygues") to "Bouygues Telecom",
            listOf("sfr box", "sfr") to "SFR",
            listOf("synology") to "Synology",
            listOf("qnap") to "QNAP",
            listOf("raspberry", "raspberrypi") to "Raspberry Pi",
            listOf("espressif", "esp32", "esp8266") to "Espressif",
            listOf("sonos") to "Sonos",
            listOf("garmin") to "Garmin",
            listOf("fitbit") to "Fitbit",
            listOf("xiaomi", "redmi", "poco") to "Xiaomi",
            listOf("huawei") to "Huawei",
            listOf("oneplus") to "OnePlus",
            listOf("lenovo", "thinkpad") to "Lenovo",
            listOf("dell") to "Dell",
            listOf("asus") to "ASUS",
            listOf("acer") to "Acer",
            listOf("netgear") to "NETGEAR",
            listOf("ubiquiti", "unifi") to "Ubiquiti",
            listOf("tp-link", "tplink", "deco") to "TP-Link",
            listOf("hp ", "hewlett", "laserjet", "officejet") to "HP",
            listOf("epson") to "Epson",
            listOf("brother") to "Brother",
            listOf("canon") to "Canon",
            listOf("sony") to "Sony",
            listOf("philips") to "Philips",
            listOf("reolink") to "Reolink",
            listOf("arlo") to "Arlo",
            listOf("ring") to "Ring / Amazon",
            listOf("amazon echo", "echo dot", "alexa", "fire tv") to "Amazon"
        )

        for ((needles, vendor) in rules) {
            val match = needles.firstOrNull { it in corpus }
            if (match != null) {
                evidence += "Mot-clé « $match »"
                return vendor
            }
        }

        return existing
    }

    private fun inferCategory(
        device: NearbyDevice,
        corpus: String,
        evidence: MutableList<String>
    ): String {
        fun hit(label: String, vararg needles: String): String? {
            val found = needles.firstOrNull { it in corpus }
            if (found != null) {
                evidence += "$label : « $found »"
                return label
            }
            return null
        }

        if (device.discoverySource?.contains("Route", ignoreCase = true) == true ||
            device.name.contains("Passerelle", ignoreCase = true) ||
            device.name.contains("routeur", ignoreCase = true)
        ) {
            evidence += "Détecté comme passerelle réseau"
            return "Routeur / passerelle"
        }

        hit("Imprimante", "_ipp._tcp", "_printer._tcp", "printer", "laserjet", "officejet")?.let { return it }
        hit("Caméra / sonnette", "camera", "caméra", "reolink", "arlo", "doorbell", "ring")?.let { return it }
        hit("NAS / stockage", "synology", "qnap", "nas", "diskstation")?.let { return it }
        hit("TV / streaming", "chromecast", "googlecast", "apple tv", "fire tv", " smart tv", " television", "_airplay._tcp")?.let { return it }
        hit("Enceinte / audio", "sonos", "homepod", "echo dot", "speaker", "enceinte")?.let { return it }
        hit("Montre / wearable", "watch", "garmin", "fitbit", "wear os", "wearable")?.let { return it }
        hit("Smartphone / tablette", "iphone", "ipad", "galaxy s", "galaxy a", "pixel ", "redmi", "poco", "phone", "tablet")?.let { return it }
        hit("IoT / domotique", "esp32", "esp8266", "espressif", "shelly", "tasmota", "zigbee", "smart plug", "sensor")?.let { return it }
        hit("Ordinateur / serveur", "_workstation._tcp", "thinkpad", "macbook", "windows", "ubuntu", "linux", "desktop", "laptop")?.let { return it }

        if ("_smb._tcp" in corpus) {
            evidence += "Service SMB"
            return "Ordinateur / NAS"
        }
        if ("_ssh._tcp" in corpus || device.port == 22) {
            evidence += "Service SSH"
            return "Serveur / équipement réseau"
        }
        if ("_http._tcp" in corpus || "_https._tcp" in corpus || device.port?.let { it in setOf(80, 443, 8080, 8443) } == true) {
            evidence += "Interface HTTP(S)"
            return "Équipement réseau / IoT"
        }

        return when (device.type) {
            DeviceType.WIFI -> "Point d'accès Wi-Fi"
            DeviceType.BLE -> "Périphérique Bluetooth"
            DeviceType.LAN -> "Équipement réseau"
        }
    }

    private fun confidence(
        category: String,
        manufacturer: String?,
        evidence: List<String>
    ): Int {
        var score = 20
        if (category !in setOf("Point d'accès Wi-Fi", "Périphérique Bluetooth", "Équipement réseau")) score += 35
        if (!manufacturer.isNullOrBlank()) score += 25
        score += minOf(20, evidence.size * 5)
        return score.coerceIn(0, 100)
    }
}
