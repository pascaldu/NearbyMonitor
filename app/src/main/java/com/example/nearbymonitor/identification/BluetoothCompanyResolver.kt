package com.example.nearbymonitor.identification

object BluetoothCompanyResolver {
    private val companies = mapOf(
        6 to "Microsoft",
        76 to "Apple",
        89 to "Nordic Semiconductor",
        117 to "Samsung Electronics",
        224 to "Google"
    )

    fun resolve(id: Int): String? = companies[id]
}
