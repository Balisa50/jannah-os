package com.jannahos.siraj.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts provider credentials with a non-exportable Android Keystore key. */
object SirajProviderSettings {
    private const val PREFS = "siraj_provider_settings"
    private const val KEY_ENDPOINT = "endpoint"
    private const val KEY_MODEL = "model"
    private const val KEY_CREDENTIAL = "credential"
    private const val KEYSTORE = "AndroidKeyStore"
    private const val ALIAS = "siraj.provider.credential.v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    data class Config(val endpoint: String, val model: String, val apiKey: String?)

    fun save(context: Context, endpoint: String, model: String, apiKey: String?) {
        val safeEndpoint = endpoint.trim().trimEnd('/')
        val safeModel = model.trim()
        require(SirajProviderEndpointPolicy.isAllowed(safeEndpoint)) {
            "Use a valid HTTPS endpoint without embedded credentials, query parameters or fragments."
        }
        require(safeModel.isNotBlank() && safeModel.length <= 160) {
            "Enter a valid model identifier."
        }
        require(apiKey == null || apiKey.length <= 4096) {
            "The provider credential is too long."
        }

        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ENDPOINT, safeEndpoint)
            .putString(KEY_MODEL, safeModel)
        if (apiKey.isNullOrBlank()) editor.remove(KEY_CREDENTIAL)
        else editor.putString(KEY_CREDENTIAL, encrypt(apiKey))
        check(editor.commit()) { "Provider settings could not be saved." }
    }

    fun load(context: Context): Config? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val endpoint = prefs.getString(KEY_ENDPOINT, null) ?: return null
        val model = prefs.getString(KEY_MODEL, null) ?: return null
        if (!SirajProviderEndpointPolicy.isAllowed(endpoint) || model.isBlank()) return null
        val encrypted = prefs.getString(KEY_CREDENTIAL, null)
        val key = encrypted?.let { runCatching { decrypt(it) }.getOrNull() }
        return Config(endpoint, model, key)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().commit()
        runCatching {
            KeyStore.getInstance(KEYSTORE).apply { load(null) }.deleteEntry(ALIAS)
        }
    }


    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return Base64.encodeToString(
            cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )
    }

    private fun decrypt(value: String): String {
        val payload = Base64.decode(value, Base64.NO_WRAP)
        require(payload.size > 12)
        val iv = payload.copyOfRange(0, 12)
        val encrypted = payload.copyOfRange(12, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted).toString(Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}
