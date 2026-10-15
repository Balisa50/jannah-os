package com.jannahos

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Private notes stored encrypted on-device. Never transmitted.
class ReflectionStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "jannah_reflections",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(timestamp: Long, note: String) {
        prefs.edit().putString("note_$timestamp", note).apply()
    }

    fun load(timestamp: Long): String = prefs.getString("note_$timestamp", "") ?: ""

    fun all(): List<Pair<Long, String>> {
        val out = mutableListOf<Pair<Long, String>>()
        for ((k, v) in prefs.all) {
            if (k.startsWith("note_") && v is String) {
                val ts = k.removePrefix("note_").toLongOrNull() ?: continue
                out.add(ts to v)
            }
        }
        return out.sortedByDescending { it.first }
    }

    fun delete(timestamp: Long) {
        prefs.edit().remove("note_$timestamp").apply()
    }
}
