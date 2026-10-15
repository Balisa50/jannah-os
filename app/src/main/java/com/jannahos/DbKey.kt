package com.jannahos

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

// Generates and stores a random 256-bit passphrase in EncryptedSharedPreferences.
// SQLCipher uses this to encrypt the databases. Never leaves the device.
object DbKey {

    private const val PREFS = "jannah_db_key"

    @Volatile private var cached: String? = null

    fun get(context: Context): String {
        cached?.let { return it }

        synchronized(this) {
            cached?.let { return it }

            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val existing = prefs.getString("k", null)
            if (existing != null) {
                cached = existing
                return existing
            }

            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            val s = bytes.joinToString("") { "%02x".format(it) }
            prefs.edit().putString("k", s).apply()
            cached = s
            return s
        }
    }
}
