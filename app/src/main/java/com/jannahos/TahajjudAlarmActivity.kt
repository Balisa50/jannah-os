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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WakeBg = Color(0xFF07090D)
private val WakeText = Color(0xFFE8E5DC)
private val WakeMuted = Color(0xFF8B8F98)
private val WakeGold = Color(0xFFC9A961)

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
            Surface(modifier = Modifier.fillMaxSize(), color = WakeBg) {
                Flow(onDone = { finish() })
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("It is time.", color = WakeText, fontSize = 30.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)
            Spacer(Modifier.height(20.dp))
            Text("Get up. Make wudu. Pray.", color = WakeMuted, fontSize = 15.sp)
            Spacer(Modifier.height(72.dp))
            Button(onClick = onAwake, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WakeGold, contentColor = WakeBg)) {
                Text("I am awake", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onNotNow) { Text("Not now", color = WakeMuted, fontSize = 14.sp) }
        }
    }

    @Composable
    private fun DuaScreen(title: String, dua: Duas.Dua, buttonLabel: String, onContinue: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            Text(title, color = WakeMuted, fontSize = 13.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(32.dp))
            Text(dua.arabic, color = WakeText, fontSize = 22.sp, textAlign = TextAlign.Center,
                lineHeight = 40.sp)
            Spacer(Modifier.height(24.dp))
            Text(dua.transliteration, color = WakeMuted, fontSize = 14.sp,
                textAlign = TextAlign.Center, lineHeight = 22.sp)
            Spacer(Modifier.height(16.dp))
            Text(dua.translation, color = WakeText, fontSize = 15.sp,
                textAlign = TextAlign.Center, lineHeight = 24.sp)
            Spacer(Modifier.height(24.dp))
            Text(dua.source, color = WakeMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(60.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WakeGold, contentColor = WakeBg)) {
                Text(buttonLabel, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    @Composable
    private fun WuduScreen(onContinue: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wudu", color = WakeText, fontSize = 26.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
            Spacer(Modifier.height(32.dp))
            Text("Hands. Mouth. Nose.\nFace. Arms. Head. Feet.",
                color = WakeMuted, fontSize = 16.sp, textAlign = TextAlign.Center, lineHeight = 28.sp)
            Spacer(Modifier.height(72.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WakeGold, contentColor = WakeBg)) {
                Text("Done", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    @Composable
    private fun PrayerScreen(onRecordStart: () -> Unit, onRecordStop: () -> Unit, onDone: (String?) -> Unit) {
        var recording by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pray.", color = WakeText, fontSize = 30.sp, fontWeight = FontWeight.Light, letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            Text("Two rak'ah. Take your time.", color = WakeMuted, fontSize = 15.sp)
            Spacer(Modifier.height(56.dp))

            Text("Optional: record your Fatihah as a private record.",
                color = WakeMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (recording) { onRecordStop(); recording = false }
                    else { onRecordStart(); recording = true }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (recording) Color(0xFF3A1F1F) else Color(0xFF161A22),
                    contentColor = if (recording) Color(0xFFE8A0A0) else WakeText
                )
            ) {
                Text(if (recording) "Stop recording" else "Start recording", fontSize = 14.sp)
            }

            Spacer(Modifier.height(48.dp))
            Button(
                onClick = {
                    if (recording) { onRecordStop(); recording = false }
                    onDone(voiceFile?.absolutePath)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WakeGold, contentColor = WakeBg)
            ) {
                Text("I have prayed", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    @Composable
    private fun ClosingScreen(onDone: () -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("May Allah accept it.", color = WakeText, fontSize = 22.sp,
                fontWeight = FontWeight.Light, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("Recorded. Sleep with intention.", color = WakeMuted, fontSize = 14.sp)
            Spacer(Modifier.height(72.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WakeGold, contentColor = WakeBg)) {
                Text("Close", fontSize = 15.sp)
            }
        }
    }
}
