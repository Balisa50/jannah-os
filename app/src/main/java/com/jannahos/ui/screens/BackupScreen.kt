package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.BackupManager
import com.jannahos.BackupCrypto
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun BackupScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var showImportConfirmation by remember { mutableStateOf(false) }
    var showExportPassphraseDialog by remember { mutableStateOf(false) }
    var showImportPassphraseDialog by remember { mutableStateOf(false) }
    var exportPassphrase by remember { mutableStateOf("") }
    var importPassphrase by remember { mutableStateOf("") }
    var pendingExportPassphrase by remember { mutableStateOf<String?>(null) }
    var pendingImportPassphrase by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val passphrase = pendingExportPassphrase.orEmpty()
            scope.launch {
                working = true
                status = null
                val result = withContext(Dispatchers.IO) {
                    val jsonTemp = File(context.cacheDir, "siraj-backup-export.json")
                    val encryptedTemp = File(context.cacheDir, "siraj-backup-export.siraj")
                    try {
                        val backup = BackupManager.export(context, jsonTemp)
                        if (!backup.ok) {
                            backup
                        } else {
                            val encrypted = BackupCrypto.encryptFile(jsonTemp, encryptedTemp, passphrase)
                            if (!encrypted.ok) {
                                BackupManager.Result(false, "Nothing was exported because encryption failed: ${encrypted.message}")
                            } else {
                                val output = context.contentResolver.openOutputStream(uri)
                                    ?: error("Could not open the selected destination")
                                output.use { stream ->
                                    encryptedTemp.inputStream().use { it.copyTo(stream) }
                                }
                                BackupManager.Result(
                                    true,
                                    backup.message
                                        .replace("Backup payload prepared", "Encrypted backup created")
                                )
                            }
                        }
                    } catch (error: Exception) {
                        BackupManager.Result(false, "Could not export encrypted backup: ${error.message}")
                    } finally {
                        jsonTemp.delete()
                        encryptedTemp.delete()
                    }
                }
                status = result.message
                working = false
                pendingExportPassphrase = null
            }
        } else {
            pendingExportPassphrase = null
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val passphrase = pendingImportPassphrase.orEmpty()
            scope.launch {
                working = true
                status = null
                val result = withContext(Dispatchers.IO) {
                    val encryptedTemp = File(context.cacheDir, "siraj-backup-import.siraj")
                    val jsonTemp = File(context.cacheDir, "siraj-backup-import.json")
                    try {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: error("Could not read the selected file")
                        input.use { source ->
                            encryptedTemp.outputStream().use { destination ->
                                val buffer = ByteArray(8192)
                                var total = 0L
                                while (true) {
                                    val count = source.read(buffer)
                                    if (count < 0) break
                                    total += count
                                    if (total > 25L * 1024L * 1024L + 64L) {
                                        error("Encrypted backup exceeds the 25 MB limit.")
                                    }
                                    destination.write(buffer, 0, count)
                                }
                            }
                        }
                        val decrypted = BackupCrypto.decryptFile(
                            encryptedTemp, jsonTemp, passphrase
                        )
                        if (!decrypted.ok) BackupManager.Result(false, decrypted.message)
                        else BackupManager.import(context, jsonTemp)
                    } catch (error: Exception) {
                        BackupManager.Result(false, "Could not restore encrypted backup: ${error.message}")
                    } finally {
                        encryptedTemp.delete()
                        jsonTemp.delete()
                    }
                }
                status = result.message
                working = false
                pendingImportPassphrase = null
            }
        } else {
            pendingImportPassphrase = null
        }
    }

    if (showExportPassphraseDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportPassphraseDialog = false
                exportPassphrase = ""
            },
            title = { Text("Encrypt backup") },
            text = {
                Column {
                    Text("Choose a passphrase of at least 12 characters. You will need the same passphrase to restore this backup. SIRAJ cannot recover it.")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = exportPassphrase,
                        onValueChange = { exportPassphrase = it.take(128) },
                        label = { Text("Backup passphrase") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (exportPassphrase.length < 12) {
                        status = "Use at least 12 characters for the backup passphrase."
                    } else {
                        pendingExportPassphrase = exportPassphrase
                        exportPassphrase = ""
                        showExportPassphraseDialog = false
                        exportLauncher.launch("siraj-backup.siraj")
                    }
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportPassphraseDialog = false
                    exportPassphrase = ""
                }) { Text("Cancel") }
            }
        )
    }

    if (showImportPassphraseDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportPassphraseDialog = false
                importPassphrase = ""
            },
            title = { Text("Unlock backup") },
            text = {
                Column {
                    Text("Enter the passphrase used when this backup was created.")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importPassphrase,
                        onValueChange = { importPassphrase = it.take(128) },
                        label = { Text("Backup passphrase") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (importPassphrase.length < 12) {
                        status = "Enter the passphrase used when this backup was created."
                    } else {
                        pendingImportPassphrase = importPassphrase
                        importPassphrase = ""
                        showImportPassphraseDialog = false
                        importLauncher.launch(arrayOf("application/octet-stream"))
                    }
                }) { Text("Choose backup") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportPassphraseDialog = false
                    importPassphrase = ""
                }) { Text("Cancel") }
            }
        )
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text("Replace backed-up data?") },
            text = {
                Text("Import replaces the prayer, sleep, fasting, struggle, bookmark, Notes and Diary records covered by this backup. Export a copy of your current data first.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirmation = false
                    showImportPassphraseDialog = true
                }) { Text("Choose backup") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(title = "Backup", eyebrow = "YOUR DATA", subtitle = "Export a copy. Restore when needed.", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))
            Text("CURRENT EXPORT SCOPE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            val items = listOf(
                "Prayer location and calculation preferences",
                "Tahajjud and Adhan preferences",
                "Wake history and struggle log",
                "Fasting and sleep records",
                "Quran bookmarks and bookmark notes",
                "Notes and Diary entries"
            )
            items.forEachIndexed { index, item ->
                Text(item, style = JannahType.body, color = JannahColors.Ink, modifier = Modifier.padding(vertical = 11.dp))
                if (index < items.lastIndex) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                "This is a partial backup, not a full device backup. Letters, private memory, action history, finance, adhkar counters and some other SIRAJ data are not included yet.",
                style = JannahType.bodySmall,
                color = JannahColors.Danger,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(JannahSpacing.l))
            Box(
                Modifier.fillMaxWidth().height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(if (working) JannahColors.Border else JannahColors.Ink)
                    .pressClickable(enabled = !working) { showExportPassphraseDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(if (working) "Working…" else "Export backup", style = JannahType.button, color = JannahColors.OnOrange)
            }
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier.fillMaxWidth().height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Card)
                    .pressClickable(enabled = !working) { showImportConfirmation = true },
                contentAlignment = Alignment.Center
            ) {
                Text("Import backup", style = JannahType.button, color = JannahColors.Ink)
            }

            status?.let {
                Spacer(Modifier.height(JannahSpacing.m))
                Text(it, style = JannahType.bodySmall, color = JannahColors.OrangeDeep, lineHeight = 20.sp)
            }

            Spacer(Modifier.height(JannahSpacing.xl))
            Text("IMPORTANT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(4.dp))
            Text(
                "This is a partial backup encrypted with your passphrase. Keep that passphrase safe; SIRAJ cannot recover it. Letters, private memory, action history, finance, adhkar counters and some other app data are not included yet.",
                style = JannahType.bodySmall,
                color = JannahColors.Muted,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
