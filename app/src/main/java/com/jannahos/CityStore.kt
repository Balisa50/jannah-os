package com.jannahos

import android.content.Context

object CityStore {
    private const val PREFS = "jannah_city"

    fun name(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("name", "") ?: ""

    fun country(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("country", "") ?: ""

    fun useCity(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("use_city", false)

    fun set(context: Context, name: String, country: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("name", name.trim())
            .putString("country", country.trim())
            .putBoolean("use_city", true)
            .apply()
    }

    data class City(val name: String, val country: String, val lat: Double, val lon: Double)

    // Full list with coordinates for instant selection (no search needed)
    val suggestions = listOf(
        City("Accra", "Ghana", 5.6037, -0.1870),
        City("Kumasi", "Ghana", 6.6885, -1.6244),
        City("Tamale", "Ghana", 9.4008, -0.8393),
        City("Takoradi", "Ghana", 4.8984, -1.7603),
        City("Cape Coast", "Ghana", 5.1053, -1.2466),
        City("Banjul", "Gambia", 13.4549, -16.5790),
        City("Serrekunda", "Gambia", 13.4383, -16.6781),
        City("Lagos", "Nigeria", 6.5244, 3.3792),
        City("Abuja", "Nigeria", 9.0765, 7.3986),
        City("Kano", "Nigeria", 12.0022, 8.5919),
        City("Cairo", "Egypt", 30.0444, 31.2357),
        City("Istanbul", "Turkey", 41.0082, 28.9784),
        City("Makkah", "Saudi Arabia", 21.3891, 39.8579),
        City("Madinah", "Saudi Arabia", 24.5247, 39.5692),
        City("Riyadh", "Saudi Arabia", 24.7136, 46.6753),
        City("Dubai", "UAE", 25.2048, 55.2708),
        City("Kuala Lumpur", "Malaysia", 3.1390, 101.6869),
        City("Jakarta", "Indonesia", -6.2088, 106.8456),
        City("Karachi", "Pakistan", 24.8607, 67.0011),
        City("Lahore", "Pakistan", 31.5497, 74.3436),
        City("Dhaka", "Bangladesh", 23.8103, 90.4125),
        City("London", "UK", 51.5074, -0.1278),
        City("Paris", "France", 48.8566, 2.3522),
        City("Berlin", "Germany", 52.5200, 13.4050),
        City("New York", "USA", 40.7128, -74.0060),
        City("Toronto", "Canada", 43.6532, -79.3832),
        City("Sydney", "Australia", -33.8688, 151.2093),
        City("Johannesburg", "South Africa", -26.2041, 28.0473),
        City("Nairobi", "Kenya", -1.2921, 36.8219),
        City("Dakar", "Senegal", 14.7167, -17.4677)
    )

    fun findCity(name: String, country: String): City? {
        return suggestions.find { it.name.equals(name, ignoreCase = true) && it.country.equals(country, ignoreCase = true) }
    }
}
