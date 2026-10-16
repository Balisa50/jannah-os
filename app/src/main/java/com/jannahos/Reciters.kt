package com.jannahos

import android.content.Context

object Reciters {

    data class Reciter(val id: String, val label: String, val cdnSlug: String)

    // cdnSlug is used with https://cdn.islamic.network/quran/audio/128/{slug}/{globalAyah}.mp3
    val all = listOf(
        Reciter("alafasy", "Mishary Alafasy", "ar.alafasy"),
        Reciter("abdulbasit", "Abdul Basit (Murattal)", "ar.abdulbasitmurattal"),
        Reciter("husary", "Mahmoud Khalil Al-Husary", "ar.husary"),
        Reciter("minshawi", "Mohamed Siddiq El-Minshawi", "ar.minshawi"),
        Reciter("sudais", "Abdurrahman As-Sudais", "ar.abdurrahmaansudais")
    )

    private const val PREFS = "jannah_reciter"
    private const val KEY = "id"

    fun current(context: Context): Reciter {
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "alafasy") ?: "alafasy"
        return all.firstOrNull { it.id == id } ?: all.first()
    }

    fun set(context: Context, reciter: Reciter) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, reciter.id).apply()
    }

    // globalAyah is 1..6236, the Quran-wide ayah index returned by the API.
    fun audioUrl(reciter: Reciter, globalAyah: Int, bitrate: Int = 128): String =
        "https://cdn.islamic.network/quran/audio/$bitrate/${reciter.cdnSlug}/$globalAyah.mp3"
}
