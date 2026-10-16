package com.jannahos.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jannahos.Duas
import com.jannahos.IstighfarStore
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType

class DhikrActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DhikrScreen()
                }
            }
        }
    }
}

// ============================================================
// THE DIFFERENTIATOR: voice-driven dhikr.
// Say "astaghfirullah" — the counter increments.
// Uses Android's built-in SpeechRecognizer with offline preference.
// No network. No Google account required if the device has an
// offline recognizer installed (most Android phones do).
// ============================================================

// The recognizer returns natural text; we match any of these stems.
// Different recognizers spell it differently — Google often returns
// "astaghfirullah" or "astagfirullah" or "I seek forgiveness".
private val ISTIGHFAR_STEMS = listOf(
    "astaghfir", "astagfir", "astaqfir", "astaghfar",
    "istighfar", "istigfar",
    "forgiveness", "forgive me"
)

private fun containsIstighfar(text: String): Boolean {
    val lower = text.lowercase()
    return ISTIGHFAR_STEMS.any { lower.contains(it) }
}

@Composable
fun DhikrScreen() {
    val ctx = LocalContext.current

    var count by remember { mutableStateOf(IstighfarStore.todayCount(ctx)) }
    var listening by remember { mutableStateOf(false) }
    var lastHeard by remember { mutableStateOf<String?>(null) }
    var recognitionAvailable by remember {
        mutableStateOf(SpeechRecognizer.isRecognitionAvailable(ctx))
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    // The actual recognizer. Recreated each time we start listening
    // so that the session is clean.
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun stopListening() {
        try { recognizer?.stopListening() } catch (_: Exception) {}
        try { recognizer?.cancel() } catch (_: Exception) {}
        try { recognizer?.destroy() } catch (_: Exception) {}
        recognizer = null
        listening = false
    }

    fun startListening() {
        if (!recognitionAvailable || !hasAudioPermission) return

        try { recognizer?.destroy() } catch (_: Exception) {}

        val sr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(ctx)
        } else {
            SpeechRecognizer.createSpeechRecognizer(ctx)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                // Restart unless we were explicitly stopped.
                if (listening) {
                    try { sr.cancel() } catch (_: Exception) {}
                    try { sr.startListening(intent) } catch (_: Exception) {}
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    lastHeard = text
                    if (containsIstighfar(text)) {
                        count = IstighfarStore.increment(ctx)
                    }
                }
                if (listening) {
                    try { sr.startListening(intent) } catch (_: Exception) {}
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    lastHeard = text
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer = sr

        try {
            sr.startListening(intent)
            listening = true
        } catch (e: Exception) {
            listening = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { stopListening() }
    }

    val dua = Duas.istighfar

    // Subtle pulse while listening.
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseValue"
    )
    val ringAlpha by animateFloatAsState(
        targetValue = if (listening) 1f else 0.5f,
        label = "ringAlpha"
    )
    val ringColor by animateColorAsState(
        targetValue = if (listening) JannahColors.Orange else JannahColors.Border,
        label = "ringColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("Dhikr", style = JannahType.title, color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text("Today · $count", style = JannahType.caption, color = JannahColors.Muted)

        Spacer(Modifier.height(JannahSpacing.l))

        // The dua card
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
        ) {
            Column(
                Modifier.padding(JannahSpacing.l).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dua.arabic, style = JannahType.arabicLarge,
                    color = JannahColors.Ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(dua.transliteration, style = JannahType.bodySmall,
                    color = JannahColors.Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(dua.translation, style = JannahType.body,
                    color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 22.sp)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(dua.source, style = JannahType.caption, color = JannahColors.Faint)
            }
        }

        Spacer(Modifier.height(JannahSpacing.xl))

        // The counter ring — tap to increment, or use voice
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1.1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(if (listening) (240 * pulse).dp else 240.dp)
            ) {
                val stroke = 3f
                drawArc(
                    color = ringColor.copy(alpha = ringAlpha),
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2, size.height - stroke * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    count.toString(),
                    color = JannahColors.Ink,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    if (listening) "LISTENING" else "TAP TO RECITE",
                    style = JannahType.eyebrow,
                    color = if (listening) JannahColors.Orange else JannahColors.Muted
                )
            }

            // Invisible tap target
            Box(
                Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .clickable { count = IstighfarStore.increment(ctx) }
            )
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Voice control — the differentiator
        if (recognitionAvailable && hasAudioPermission) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.buttonRadius))
                    .background(if (listening) JannahColors.OrangeDeep else JannahColors.Orange)
                    .clickable {
                        if (listening) stopListening() else startListening()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (listening) "Stop listening" else "Start voice dhikr",
                    style = JannahType.button,
                    color = JannahColors.OnOrange
                )
            }
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                if (listening)
                    "Say “Astaghfirullah”. Counts automatically."
                else
                    "Voice counts every time you say Astaghfirullah.",
                style = JannahType.caption,
                color = JannahColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (lastHeard != null && listening) {
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(
                    "Heard: “$lastHeard”",
                    style = JannahType.caption,
                    color = JannahColors.Faint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Text(
                when {
                    !recognitionAvailable ->
                        "Voice recognition is not available on this device."
                    !hasAudioPermission ->
                        "Grant microphone permission to use voice dhikr."
                    else -> ""
                },
                style = JannahType.caption,
                color = JannahColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.buttonRadius))
                    .clickable {
                        IstighfarStore.reset(ctx)
                        count = 0
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Reset today", style = JannahType.caption, color = JannahColors.Muted)
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Text(
            "The Prophet ﷺ sought forgiveness more than seventy times a day. " +
                "This counter is a private reminder. It is not a score.",
            style = JannahType.caption, color = JannahColors.Faint,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}
