package com.example.nearbymonitor.identification

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

/**
 * Résolution locale du fabricant à partir d'une adresse MAC.
 *
 * La base complète est générée par tools/update_oui.py dans
 * app/src/main/assets/manuf/oui_lookup.tsv. Aucune requête réseau n'est
 * effectuée depuis l'application Android.
 */
class MacManufacturerResolver(context: Context) {
    data class Match(
        val manufacturer: String?,
        val source: String,
        val confidence: Int,
        val prefix: String? = null,
        val registry: String? = null,
        val locallyAdministered: Boolean = false
    )

    private data class Entry(
        val registry: String,
        val manufacturer: String
    )

    private val appContext = context.applicationContext

    @Volatile
    private var loaded = false

    private val prefixes24 = HashMap<String, Entry>()
    private val prefixes28 = HashMap<String, Entry>()
    private val prefixes36 = HashMap<String, Entry>()

    fun resolve(mac: String?): Match? {
        val normalized = normalize(mac) ?: return null
        val firstByte = normalized.substring(0, 2).toIntOrNull(16) ?: return null

        if ((firstByte and 0x01) != 0) return null

        if ((firstByte and 0x02) != 0) {
            return Match(
                manufacturer = null,
                source = "Adresse MAC privée/randomisée",
                confidence = 0,
                locallyAdministered = true
            )
        }

        ensureLoaded()

        val candidates = listOf(
            9 to prefixes36,
            7 to prefixes28,
            6 to prefixes24
        )

        for ((length, table) in candidates) {
            val prefix = normalized.take(length)
            val entry = table[prefix] ?: continue
            if (entry.manufacturer.equals("IEEE Registration Authority", ignoreCase = true)) continue

            val bits = when (length) { 9 -> 36; 7 -> 28; else -> 24 }
            val confidence = when (bits) { 36 -> 99; 28 -> 98; else -> 95 }

            return Match(
                manufacturer = entry.manufacturer,
                source = "IEEE ${entry.registry} · $bits bits",
                confidence = confidence,
                prefix = prefix,
                registry = entry.registry,
                locallyAdministered = false
            )
        }
        return null
    }

    fun databaseEntryCount(): Int {
        ensureLoaded()
        return prefixes24.size + prefixes28.size + prefixes36.size
    }

    private fun ensureLoaded() {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            runCatching { loadDatabase() }
            loaded = true
        }
    }

    private fun loadDatabase() {
        val stream = appContext.assets.open(ASSET_PATH)
        BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).useLines { lines ->
            lines.forEach { raw ->
                val line = raw.trimEnd()
                if (line.isBlank() || line.startsWith("#")) return@forEach
                val parts = line.split('\t', limit = 3)
                if (parts.size < 3) return@forEach
                val prefix = parts[0].trim().uppercase(Locale.ROOT)
                val registry = parts[1].trim().ifBlank { "OUI" }
                val manufacturer = parts[2].trim()
                if (manufacturer.isBlank()) return@forEach
                val entry = Entry(registry = registry, manufacturer = manufacturer)
                when (prefix.length) {
                    6 -> prefixes24[prefix] = entry
                    7 -> prefixes28[prefix] = entry
                    9 -> prefixes36[prefix] = entry
                }
            }
        }
    }

    companion object {
        private const val ASSET_PATH = "manuf/oui_lookup.tsv"

        fun normalize(mac: String?): String? = mac
            ?.filter { it.isLetterOrDigit() }
            ?.uppercase(Locale.ROOT)
            ?.takeIf { it.length == 12 && it.all { c -> c in '0'..'9' || c in 'A'..'F' } }

        fun format(mac: String?): String? {
            val normalized = normalize(mac) ?: return null
            return normalized.chunked(2).joinToString(":")
        }
    }
}
