package com.jannahos

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.AtomicMoveNotSupportedException
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Portable passphrase encryption for user-selected backup files.
 * The Android Keystore is intentionally not used: backups must be restorable
 * on another device using the same passphrase.
 */
object BackupCrypto {
    data class Result(val ok: Boolean, val message: String)

    private val MAGIC = "SIRAJBK1".toByteArray(Charsets.US_ASCII)
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private const val KEY_BITS = 256
    private const val ITERATIONS = 210_000
    private const val MAX_PLAINTEXT_BYTES = 25L * 1024L * 1024L
    private const val HEADER_BYTES = 8 + SALT_BYTES + IV_BYTES
    private const val MAX_ENCRYPTED_BYTES = MAX_PLAINTEXT_BYTES + HEADER_BYTES + 16

    fun encryptFile(inputFile: File, outputFile: File, passphrase: String): Result {
        if (!validPassphrase(passphrase)) {
            return Result(false, "Use a backup passphrase of at least 12 characters.")
        }
        if (!inputFile.isFile || inputFile.length() !in 1L..MAX_PLAINTEXT_BYTES) {
            return Result(false, "Backup payload is missing, empty or exceeds 25 MB.")
        }

        val salt = ByteArray(SALT_BYTES)
        val iv = ByteArray(IV_BYTES)
        SecureRandom().nextBytes(salt)
        SecureRandom().nextBytes(iv)
        val password = passphrase.toCharArray()
        var keyBytes: ByteArray? = null
        var plaintext: ByteArray? = null

        return try {
            val plain = inputFile.readBytes()
            plaintext = plain
            val derivedKey = deriveKey(password, salt)
            keyBytes = derivedKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(derivedKey, "AES"), GCMParameterSpec(TAG_BITS, iv))
            val ciphertext = cipher.doFinal(plain)
            val output = ByteArray(MAGIC.size + salt.size + iv.size + ciphertext.size)
            var offset = 0
            MAGIC.copyInto(output, offset); offset += MAGIC.size
            salt.copyInto(output, offset); offset += salt.size
            iv.copyInto(output, offset); offset += iv.size
            ciphertext.copyInto(output, offset)
            writeAtomically(outputFile, output)
            Result(true, "Backup encrypted successfully.")
        } catch (error: Exception) {
            Result(false, "Could not encrypt backup: ${error.message ?: error.javaClass.simpleName}")
        } finally {
            password.fill('\u0000')
            keyBytes?.fill(0)
            plaintext?.fill(0)
        }
    }

    fun decryptFile(inputFile: File, outputFile: File, passphrase: String): Result {
        if (!validPassphrase(passphrase)) {
            return Result(false, "Enter the passphrase used when this backup was created.")
        }
        if (!inputFile.isFile || inputFile.length() !in (HEADER_BYTES + 16).toLong()..MAX_ENCRYPTED_BYTES) {
            return Result(false, "This is not a valid SIRAJ encrypted backup or it exceeds 25 MB.")
        }

        val password = passphrase.toCharArray()
        var keyBytes: ByteArray? = null
        var plaintext: ByteArray? = null
        return try {
            val encrypted = inputFile.readBytes()
            if (!encrypted.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) {
                return Result(false, "This file is not a SIRAJ encrypted backup.")
            }
            val saltStart = MAGIC.size
            val ivStart = saltStart + SALT_BYTES
            val dataStart = ivStart + IV_BYTES
            val salt = encrypted.copyOfRange(saltStart, ivStart)
            val iv = encrypted.copyOfRange(ivStart, dataStart)
            val ciphertext = encrypted.copyOfRange(dataStart, encrypted.size)
            val derivedKey = deriveKey(password, salt)
            keyBytes = derivedKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(derivedKey, "AES"), GCMParameterSpec(TAG_BITS, iv))
            val plain = cipher.doFinal(ciphertext)
            plaintext = plain
            if (plain.size.toLong() > MAX_PLAINTEXT_BYTES) {
                return Result(false, "Decrypted backup exceeds the 25 MB limit.")
            }
            writeAtomically(outputFile, plain)
            Result(true, "Backup decrypted successfully.")
        } catch (_: AEADBadTagException) {
            Result(false, "The passphrase is incorrect or the backup file has been altered.")
        } catch (error: Exception) {
            Result(false, "Could not decrypt backup: ${error.message ?: error.javaClass.simpleName}")
        } finally {
            password.fill('\u0000')
            keyBytes?.fill(0)
            plaintext?.fill(0)
        }
    }

    private fun validPassphrase(value: String): Boolean =
        value.length in 12..128 && value.any { !it.isWhitespace() }

    private fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun writeAtomically(destinationFile: File, bytes: ByteArray) {
        val destination = destinationFile.absoluteFile
        val parent = destination.parentFile ?: error("Backup destination has no parent directory.")
        if (!parent.exists() && !parent.mkdirs()) error("Could not create the backup directory.")
        val temporary = File(parent, destination.name + ".siraj-tmp")
        try {
            FileOutputStream(temporary).use { stream ->
                stream.write(bytes)
                stream.fd.sync()
            }
            try {
                Files.move(temporary.toPath(), destination.toPath(), ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporary.toPath(), destination.toPath(), REPLACE_EXISTING)
            }
        } finally {
            if (temporary.exists()) temporary.delete()
        }
    }
}
