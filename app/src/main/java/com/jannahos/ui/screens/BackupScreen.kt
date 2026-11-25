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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.BackupManager
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

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                working = true
                status = null
                val result = withContext(Dispatchers.IO) {
                    val temp = File(context.cacheDir, "siraj-backup-export.json")
                    val backup = BackupManager.export(context, temp)
                    if (!backup.ok) {
                        temp.delete()
                        backup
                    } else {
                        try {
                            val output = context.contentResolver.openOutputStream(uri)
                                ?: error("Could not open the selected destination")
                            output.use { stream -> temp.inputStream().use { it.copyTo(stream) } }
                            temp.delete()
                            backup
                        } catch (error: Exception) {
                            temp.delete()
                            BackupManager.Result(false, "Could not write backup: ${error.message}")
                        }
                    }
                }
                status = result.message
                working = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                working = true
                status = null
                val result = withContext(Dispatchers.IO) {
                    val temp = File(context.cacheDir, "siraj-backup-import.json")
                    try {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: error("Could not read the selected file")
                        input.use { stream -> temp.outputStream().use { stream.copyTo(it) } }
                        BackupManager.import(context, temp)
                    } catch (error: Exception) {
                        BackupManager.Result(false, "Could not read backup: ${error.message}")
                    } finally {
                        temp.delete()
                    }
                }
                status = result.message
                working = false
            }
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text("Replace backed-up data?") },
            text = {
                Text("Import replaces the prayer, sleep, fasting, struggle and bookmark records covered by this backup. Export a copy of your current data first.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirmation = false
                    importLauncher.launch(arrayOf("application/json", "text/json"))
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
                "Quran bookmarks and bookmark notes"
            )
            items.forEachIndexed { index, item ->
                Text(item, style = JannahType.body, color = JannahColors.Ink, modifier = Modifier.padding(vertical = 11.dp))
                if (index < items.lastIndex) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))
            Box(
                Modifier.fillMaxWidth().height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(if (working) JannahColors.Border else JannahColors.Ink)
                    .pressClickable(enabled = !working) { exportLauncher.launch("siraj-backup.json") },
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
                "This export is currently partial and is not encrypted. It contains private personal records. Store it somewhere secure and do not share it. Diary entries, letters, finance and some other app data are not included yet.",
                style = JannahType.bodySmall,
                color = JannahColors.Muted,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
