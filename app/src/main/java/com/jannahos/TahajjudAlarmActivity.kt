package com.jannahos

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TahajjudAlarmActivity : ComponentActivity() {

    private var recorder: MediaRecorder? = null
    private var voiceFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }

        setContent {
            JannahTheme(night = true) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Flow(onDone = { finish() })
                }
            }
        }
    }

    @Composable
    private fun Flow(onDone: () -> Unit) {
        var step by remember { mutableStateOf(0) }
        val log = remember { WakeLog(this@TahajjudAlarmActivity) }

        when (step) {
            0 -> WakeScreen(
                onAwake = { step = 1 },
                onNotNow = {
                    log.record("missed", "not_now")
                    onDone()
                }
            )
            1 -> DuaScreen(
                title = "When you rise",
                dua = Duas.tahajjudOpening,
                buttonLabel = "I am awake",
                onContinue = { step = 2 }
            )
            2 -> WuduScreen(onContinue = { step = 3 })
            3 -> DuaScreen(
                title = "The last third",
                dua = Duas.lastThird,
                buttonLabel = "I am praying now",
                onContinue = { step = 4 }
            )
            4 -> PrayerScreen(
                onRecordStart = { startRecording() },
                onRecordStop = { stopRecording() },
                onDone = { path ->
                    log.record("prayed", voicePath = path)
                    step = 5
                }
            )
            5 -> ClosingScreen(onDone = onDone)
        }
    }

    private fun startRecording() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            return
        }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val f = File(filesDir, "voice_$stamp.3gp")
        voiceFile = f
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(f.absolutePath)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        try { recorder?.stop() } catch (_: Exception) {}
        try { recorder?.release() } catch (_: Exception) {}
        recorder = null
    }

    @Composable
    private fun WakeScreen(onAwake: () -> Unit, onNotNow: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.screenGutter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("It is time.", style = JannahType.screenTitle, color = JannahColors.NightText,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.m))
            Text("Get up. Make wudu. Pray.", style = JannahType.primary, color = JannahColors.NightMuted,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.xxl))
            Button(
                onClick = onAwake,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) { Text("I am awake", style = JannahType.button) }
            Spacer(Modifier.height(JannahSpacing.l))
            TextButton(onClick = onNotNow) {
                Text("Not now", style = JannahType.secondary, color = JannahColors.NightMuted)
            }
        }
    }

    @Composable
    private fun DuaScreen(title: String, dua: Duas.Dua, buttonLabel: String, onContinue: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.screenGutter)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))
            Text(title, style = JannahType.section, color = JannahColors.NightMuted)
            Spacer(Modifier.height(JannahSpacing.l))
            Text(dua.arabic, style = JannahType.arabic, color = JannahColors.NightText,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.l))
            Text(dua.transliteration, style = JannahType.secondary, color = JannahColors.NightMuted,
                textAlign = TextAlign.Center, lineHeight = 22.sp)
            Spacer(Modifier.height(JannahSpacing.m))
            Text(dua.translation, style = JannahType.primary, color = JannahColors.NightText,
                textAlign = TextAlign.Center, lineHeight = 24.sp)
            Spacer(Modifier.height(JannahSpacing.m))
            Text(dua.source, style = JannahType.tertiary, color = JannahColors.NightMuted,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.xxl))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) { Text(buttonLabel, style = JannahType.button) }
            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }

    @Composable
    private fun WuduScreen(onContinue: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.screenGutter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wudu", style = JannahType.screenTitle, color = JannahColors.NightText)
            Spacer(Modifier.height(JannahSpacing.xl))
            Text("Hands. Mouth. Nose.\nFace. Arms. Head. Feet.",
                style = JannahType.primary, color = JannahColors.NightMuted,
                textAlign = TextAlign.Center, lineHeight = 28.sp)
            Spacer(Modifier.height(JannahSpacing.xxl))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) { Text("Done", style = JannahType.button) }
        }
    }

    @Composable
    private fun PrayerScreen(onRecordStart: () -> Unit, onRecordStop: () -> Unit, onDone: (String?) -> Unit) {
        var recording by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.screenGutter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pray.", style = JannahType.screenTitle, color = JannahColors.NightText)
            Spacer(Modifier.height(JannahSpacing.m))
            Text("Two rak'ah. Take your time.", style = JannahType.primary, color = JannahColors.NightMuted)

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("Optional: record your Fatihah as a private record.",
                style = JannahType.tertiary, color = JannahColors.NightMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.m))

            Button(
                onClick = {
                    if (recording) { onRecordStop(); recording = false }
                    else { onRecordStart(); recording = true }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (recording) JannahColors.Danger else JannahColors.NightSurface,
                    contentColor = JannahColors.NightText
                )
            ) {
                Text(if (recording) "Stop recording" else "Start recording", style = JannahType.secondary)
            }

            Spacer(Modifier.height(JannahSpacing.xl))
            Button(
                onClick = {
                    if (recording) { onRecordStop(); recording = false }
                    onDone(voiceFile?.absolutePath)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) { Text("I have prayed", style = JannahType.button) }
        }
    }

    @Composable
    private fun ClosingScreen(onDone: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.screenGutter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("May Allah accept it.", style = JannahType.screenTitle, color = JannahColors.NightText,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.m))
            Text("Recorded. Sleep with intention.",
                style = JannahType.secondary, color = JannahColors.NightMuted)
            Spacer(Modifier.height(JannahSpacing.xxl))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) { Text("Close", style = JannahType.button) }
        }
    }
}
