package com.jannahos

import android.content.Context

object CityStore {
    private const val PREFS = "jannah_city"

    fun name(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("name", "") ?: ""

    fun country(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("country", "") ?: ""

    fun useCity(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean("use_city", false)

    fun set(context: Context, name: String, country: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("name", name.trim())
            .putString("country", country.trim())
            .putBoolean("use_city", true)
            .apply()
    }

    // Common cities so the user doesn't have to type.
    val suggestions = listOf(
        "Banjul" to "Gambia",
        "Serrekunda" to "Gambia",
        "Brikama" to "Gambia",
        "Lagos" to "Nigeria",
        "Abuja" to "Nigeria",
        "Kano" to "Nigeria",
        "Cairo" to "Egypt",
        "Istanbul" to "Turkey",
        "Makkah" to "Saudi Arabia",
        "Madinah" to "Saudi Arabia",
        "Riyadh" to "Saudi Arabia",
        "Dubai" to "United Arab Emirates",
        "Kuala Lumpur" to "Malaysia",
        "Jakarta" to "Indonesia",
        "Karachi" to "Pakistan",
        "Lahore" to "Pakistan",
        "Dhaka" to "Bangladesh",
        "London" to "United Kingdom",
        "Manchester" to "United Kingdom",
        "Paris" to "France",
        "Marseille" to "France",
        "Berlin" to "Germany",
        "New York" to "United States",
        "Chicago" to "United States",
        "Houston" to "United States",
        "Toronto" to "Canada",
        "Sydney" to "Australia",
        "Melbourne" to "Australia",
        "Johannesburg" to "South Africa",
        "Nairobi" to "Kenya",
        "Accra" to "Ghana",
        "Dakar" to "Senegal"
    )
}
