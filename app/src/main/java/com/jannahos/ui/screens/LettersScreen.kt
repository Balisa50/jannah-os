package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.LetterStore
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LettersScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val store = remember { runCatching { LetterStore(context) }.getOrNull() }
    var refresh by remember { mutableIntStateOf(0) }
    val lettersResult = remember(refresh) { runCatching { store?.all().orEmpty() } }
    val letters = lettersResult.getOrDefault(emptyList())
    val now = System.currentTimeMillis()
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    var showCompose by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var daysAhead by remember { mutableIntStateOf(30) }
    var selectedLetter by remember { mutableStateOf<LetterStore.Letter?>(null) }
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(
            title = "Letters",
            eyebrow = "FUTURE SELF",
            subtitle = "Write something for a later version of you.",
            onBack = onBack
        )

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.gutter)
        ) {
            Spacer(Modifier.height(JannahSpacing.l))
            Text(
                "Write honestly. Your letter is encrypted on this device and stays sealed until the date you choose.",
                style = JannahType.body,
                color = JannahColors.Body,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(JannahSpacing.l))
            Box(
                Modifier.fillMaxWidth().height(54.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable { showCompose = true },
                contentAlignment = Alignment.Center
            ) {
                Text("Write a letter", style = JannahType.button, color = JannahColors.OnOrange)
            }
            errorMessage?.let {
                Spacer(Modifier.height(JannahSpacing.s))
                Text(it, style = JannahType.caption, color = JannahColors.Danger)
            }
            Spacer(Modifier.height(JannahSpacing.l))

            if (store == null || lettersResult.isFailure) {
                Text("Letter storage couldn't be opened. Your existing letters have not been changed.", style = JannahType.bodySmall, color = JannahColors.Danger)
            } else if (letters.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nothing sealed yet.", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.height(6.dp))
                    Text("Your first letter can wait a week, a month or a year.", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
            } else {
                Text("YOUR LETTERS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                letters.forEach { letter ->
                    val isSealed = letter.opensAt > now
                    val status = when {
                        letter.opened -> "OPENED"
                        isSealed -> "SEALED UNTIL ${dateFormat.format(Date(letter.opensAt)).uppercase()}"
                        else -> "READY TO OPEN"
                    }
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.Card)
                            .clickable {
                                if (!isSealed && !letter.opened) {
                                    runCatching { store.markOpened(letter.id) }
                                        .onSuccess { refresh++ }
                                        .onFailure { errorMessage = it.message ?: "Could not open this letter." }
                                }
                                selectedLetter = letter
                            }
                            .padding(JannahSpacing.m),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(letter.title, fontFamily = FontFamily.Serif, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                            Spacer(Modifier.height(5.dp))
                            Text(status, style = JannahType.micro, color = if (isSealed) JannahColors.OrangeDeep else JannahColors.Muted)
                        }
                        Text(if (isSealed) "◷" else "→", fontSize = 20.sp, color = JannahColors.OrangeDeep)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "Remove",
                            style = JannahType.micro,
                            color = JannahColors.Muted,
                            modifier = Modifier.clickable { pendingDeleteId = letter.id }.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(JannahSpacing.s))
                }
            }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }

    if (showCompose) {
        AlertDialog(
            onDismissRequest = { showCompose = false },
            title = { Text("A letter to future you", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(80); errorMessage = null },
                        label = { Text("Title") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it.take(12000); errorMessage = null },
                        label = { Text("Your letter") },
                        minLines = 5
                    )
                    Spacer(Modifier.height(14.dp))
                    Text("OPEN IN", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(7, 30, 90, 365).forEach { days ->
                            Box(
                                Modifier.weight(1f).clip(RoundedCornerShape(50))
                                    .background(if (daysAhead == days) JannahColors.Orange else JannahColors.Card)
                                    .clickable { daysAhead = days }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (days) { 7 -> "1 wk"; 30 -> "1 mo"; 90 -> "3 mo"; else -> "1 yr" },
                                    style = JannahType.micro,
                                    color = if (daysAhead == days) JannahColors.OnOrange else JannahColors.Ink
                                )
                            }
                        }
                    }
                    errorMessage?.let { Text(it, style = JannahType.caption, color = JannahColors.Danger, modifier = Modifier.padding(top = 8.dp)) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isBlank() || body.isBlank()) {
                        errorMessage = "Add a title and write your letter before sealing it."
                    } else {
                        runCatching { store?.add(title.trim(), body.trim(), daysAhead) ?: error("Letter storage is unavailable.") }
                            .onSuccess {
                                title = ""
                                body = ""
                                daysAhead = 30
                                showCompose = false
                                errorMessage = null
                                refresh++
                            }
                            .onFailure { errorMessage = it.message ?: "Could not save this letter." }
                    }
                }) { Text("Seal letter") }
            },
            dismissButton = { TextButton(onClick = { showCompose = false }) { Text("Cancel") } },
            containerColor = JannahColors.Surface
        )
    }

    selectedLetter?.let { letter ->
        val locked = letter.opensAt > System.currentTimeMillis()
        AlertDialog(
            onDismissRequest = { selectedLetter = null },
            title = { Text(letter.title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = {
                if (locked) {
                    Text(
                        "This letter is sealed until ${dateFormat.format(Date(letter.opensAt))}. Come back then.",
                        style = JannahType.body,
                        color = JannahColors.Body
                    )
                } else {
                    Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                        Text(letter.body, style = JannahType.body, color = JannahColors.Body, lineHeight = 24.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLetter = null }) { Text("Close") }
            },
            containerColor = JannahColors.Surface
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete this letter?") },
            text = { Text("This permanently removes the letter from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { store?.delete(id) }
                        .onSuccess { pendingDeleteId = null; refresh++ }
                        .onFailure { errorMessage = it.message ?: "Could not delete this letter." }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel") } },
            containerColor = JannahColors.Surface
        )
    }
}
