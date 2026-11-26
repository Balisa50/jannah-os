package com.jannahos

import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoTest {
    @Test fun encryptedBackupRoundTripsAcrossFiles() {
        val directory = createTempDir(prefix = "siraj-backup-test")
        try {
            val source = File(directory, "payload.json").apply {
                writeText("""{"schema":1,"records":[1,2,3]}""")
            }
            val encrypted = File(directory, "backup.siraj")
            val restored = File(directory, "restored.json")
            assertTrue(BackupCrypto.encryptFile(source, encrypted, "correct horse battery").ok)
            assertTrue(BackupCrypto.decryptFile(encrypted, restored, "correct horse battery").ok)
            assertArrayEquals(source.readBytes(), restored.readBytes())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun wrongPassphraseDoesNotProducePlaintext() {
        val directory = createTempDir(prefix = "siraj-backup-wrong-pass")
        try {
            val source = File(directory, "payload.json").apply { writeText("private data") }
            val encrypted = File(directory, "backup.siraj")
            val restored = File(directory, "restored.json")
            assertTrue(BackupCrypto.encryptFile(source, encrypted, "correct horse battery").ok)
            val result = BackupCrypto.decryptFile(encrypted, restored, "incorrect passphrase")
            assertFalse(result.ok)
            assertFalse(restored.exists())
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun rejectsWeakPassphrasesAndInvalidHeaders() {
        val directory = createTempDir(prefix = "siraj-backup-invalid")
        try {
            val source = File(directory, "payload.json").apply { writeText("data") }
            val encrypted = File(directory, "backup.siraj")
            assertFalse(BackupCrypto.encryptFile(source, encrypted, "short").ok)
            encrypted.writeText("not a SIRAJ backup")
            assertFalse(BackupCrypto.decryptFile(encrypted, File(directory, "out"), "long enough passphrase").ok)
        } finally {
            directory.deleteRecursively()
        }
    }
}
