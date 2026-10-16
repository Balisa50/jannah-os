package com.jannahos.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.BackupManager
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.io.File

@Composable
fun BackupScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var statusMsg by remember { mutableStateOf<String?>(null) }
    var statusOk by remember { mutableStateOf(true) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val tmp = File(ctx.cacheDir, "jannah_backup.json")
                val result = BackupManager.export(ctx, tmp)
                if (result.ok) {
                    ctx.contentResolver.openOutputStream(uri)?.use { os ->
                        tmp.inputStream().copyTo(os)
                    }
                    statusMsg = result.message
                    statusOk = true
                } else {
                    statusMsg = result.message
                    statusOk = false
                }
                tmp.delete()
            } catch (e: Exception) {
                statusMsg = "Save failed: ${e.message}"
                statusOk = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val tmp = File(ctx.cacheDir, "jannah_restore.json")
                ctx.contentResolver.openInputStream(uri)?.use { ins ->
                    tmp.outputStream().use { os -> ins.copyTo(os) }
                }
                val result = BackupManager.import(ctx, tmp)
                statusMsg = result.message
                statusOk = result.ok
                tmp.delete()
            } catch (e: Exception) {
                statusMsg = "Restore failed: ${e.message}"
                statusOk = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("BACKUP",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Your data, ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("yours.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Export everything to a file. Import it anywhere.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("WHAT IS BACKED UP",
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    ListItem("Prayer & tahajjud history")
                    ListItem("Reflections and tawbah notes")
                    ListItem("Fasts and sleep records")
                    ListItem("Quran bookmarks and last read")
                    ListItem("Settings, reciter, adhan choice")
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Export pill
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        exportLauncher.launch("jannah-backup.json")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Export backup",
                    style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.s))

            // Import pill
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Card)
                    .clickable {
                        importLauncher.launch(arrayOf("application/json"))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Import backup",
                    style = JannahType.button, color = JannahColors.Ink)
            }

            if (statusMsg != null) {
                Spacer(Modifier.height(JannahSpacing.m))
                Text(
                    statusMsg!!,
                    style = JannahType.bodySmall,
                    color = if (statusOk) JannahColors.OrangeDeep else JannahColors.Danger,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("PRIVACY",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "The file is written only where you choose. Nothing is ever uploaded. If you restore on another phone, your new device inherits your data — but nothing leaves your control.",
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun ListItem(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(JannahColors.Orange)
        ) {}
        Spacer(Modifier.padding(horizontal = 6.dp))
        Text("· " + text, style = JannahType.bodySmall, color = JannahColors.Body)
    }
}
