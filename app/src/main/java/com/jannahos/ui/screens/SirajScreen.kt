package com.jannahos.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.QiblaActivity
import com.jannahos.QuranCache
import com.jannahos.QuranStore
import com.jannahos.SettingsActivity
import com.jannahos.siraj.core.SirajAction
import com.jannahos.siraj.core.SirajCommandParser
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.launch

private data class SirajMessage(
    val text: String,
    val fromUser: Boolean
)

@Composable
fun SirajScreen(onNavigate: (String) -> Unit = {}) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            SirajMessage(
                "I can open a Surah, show prayer times, find the Qibla, start Quran search, or take you to adhkar, notes and settings. I won't invent religious answers or claim a source I haven't checked.",
                false
            )
        )
    }

    fun submit(raw: String) {
        val request = raw.trim()
        if (request.isBlank()) return
        messages.add(SirajMessage(request, true))
        input = ""

        val result = SirajCommandParser.parse(request, QuranCache.SURAHS)
        messages.add(SirajMessage(result.reply, false))

        when (val action = result.action) {
            is SirajAction.OpenSurah ->
                onNavigate("mushaf/${QuranStore.firstPageOfSurah(action.number)}/604")
            SirajAction.OpenQuran -> onNavigate("quran")
            SirajAction.OpenPrayerTimes -> onNavigate("pray")
            SirajAction.OpenQibla ->
                context.startActivity(Intent(context, QiblaActivity::class.java))
            SirajAction.OpenQuranSearch -> onNavigate("search")
            SirajAction.OpenAdhkar -> onNavigate("adhkar")
            SirajAction.OpenNotes -> onNavigate("notes")
            SirajAction.OpenSettings ->
                context.startActivity(Intent(context, SettingsActivity::class.java))
            null -> Unit
        }

        scope.launch {
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spokenRequest = if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
        } else null

        if (!spokenRequest.isNullOrBlank()) {
            submit(spokenRequest)
        }
    }

    fun startVoiceInput() {
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a request for SIRAJ")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        runCatching { speechLauncher.launch(speechIntent) }
            .onFailure {
                messages.add(
                    SirajMessage(
                        "Voice input isn't available through the speech service on this device. You can still type a request.",
                        false
                    )
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
            .padding(horizontal = 22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "SIRAJ",
                    fontFamily = FontFamily.Serif,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "سِرَاج",
                    fontSize = 15.sp,
                    color = JannahColors.Orange,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            Text(
                "LOCAL COMMANDS",
                style = JannahType.micro,
                color = JannahColors.OrangeDeep,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(JannahColors.OrangeSoft)
                    .padding(horizontal = 11.dp, vertical = 8.dp)
            )
        }

        Text(
            "What do you need?",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink
        )
        Text(
            "Make a clear request. SIRAJ will take you there.",
            style = JannahType.bodySmall,
            color = JannahColors.Muted,
            modifier = Modifier.padding(top = 7.dp, bottom = 18.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Suggestion("Al-Kahf") { submit("Open Surah Al-Kahf") }
            Suggestion("Prayer times") { submit("Show prayer times") }
            Suggestion("Qibla") { submit("Find Qibla") }
        }

        Spacer(Modifier.height(18.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(JannahColors.Divider)
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(messages) { _, message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        message.text,
                        modifier = Modifier
                            .widthIn(max = 310.dp)
                            .then(
                                if (message.fromUser) {
                                    Modifier
                                        .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                                        .background(JannahColors.OrangeSoft)
                                        .padding(horizontal = 15.dp, vertical = 12.dp)
                                } else {
                                    Modifier.padding(end = 20.dp, top = 4.dp, bottom = 4.dp)
                                }
                            ),
                        style = JannahType.body,
                        color = if (message.fromUser) JannahColors.Ink else JannahColors.Body,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Text(
            "Voice recognition is provided by your Android speech service.",
            style = JannahType.micro,
            color = JannahColors.Muted,
            modifier = Modifier.padding(bottom = 7.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask SIRAJ to open…", color = JannahColors.Muted) },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { submit(input) })
            )
            TextButton(
                onClick = { startVoiceInput() },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, JannahColors.Border, RoundedCornerShape(16.dp))
            ) {
                Text("Speak", color = JannahColors.Ink, fontWeight = FontWeight.Medium)
            }
            TextButton(
                onClick = { submit(input) },
                enabled = input.isNotBlank(),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (input.isNotBlank()) JannahColors.Orange else JannahColors.Border)
            ) {
                Text("Go", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun Suggestion(label: String, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, JannahColors.Border, RoundedCornerShape(50))
            .background(JannahColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        style = JannahType.caption,
        color = JannahColors.Ink
    )
}
