package com.jannahos

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.FocusSession
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

class SalahCommitmentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }

        val prayer = intent.getStringExtra("prayer") ?: "fajr"

        if (FocusSession.enabled(this)) {
            FocusSession.start(this, prayer, 10)
        }

        setContent {
            JannahTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CommitmentScreen(prayer)
                }
            }
        }
    }

    @Composable
    private fun CommitmentScreen(prayer: String) {
        val ctx = LocalContext.current
        val label = SalahTracker.label(prayer)
        val windowSeconds = 10 * 60
        var remaining by remember { mutableStateOf(windowSeconds) }

        LaunchedEffect(Unit) {
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
            }
        }

        val mm = remaining / 60
        val ss = remaining % 60
        val timeText = String.format(java.util.Locale.US, "%02d:%02d", mm, ss)
        val progress = 1f - (remaining.toFloat() / windowSeconds.toFloat())
        val animProgress by animateFloatAsState(progress, tween(900), label = "p")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(JannahColors.Cream)
                .padding(horizontal = JannahSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            Text("IT IS TIME FOR", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.xs))

            Text(
                label,
                fontFamily = FontFamily.Serif,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                letterSpacing = (-1.5).sp,
                lineHeight = 62.sp
            )

            Spacer(Modifier.height(JannahSpacing.xs))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Make wudu. ", style = JannahType.body, color = JannahColors.Body)
                Text("Pray.",
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Orange)
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // Countdown ring
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1.1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(260.dp)) {
                    val stroke = 4f
                    drawArc(
                        color = JannahColors.Border,
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = Size(size.width - stroke * 2, size.height - stroke * 2),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = JannahColors.Orange,
                        startAngle = -90f, sweepAngle = 360f * animProgress, useCenter = false,
                        topLeft = Offset(stroke, stroke),
                        size = Size(size.width - stroke * 2, size.height - stroke * 2),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(timeText,
                        fontFamily = FontFamily.Serif,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        letterSpacing = (-2).sp)
                    Spacer(Modifier.height(4.dp))
                    Text("WINDOW REMAINING",
                        style = JannahType.eyebrow,
                        color = JannahColors.Muted)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // Primary: prayed
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Orange)
                    .clickable {
                        FocusSession.stop(ctx)
                        SalahTracker.toggle(ctx, prayer)
                        // Ensure it's marked done (toggle flips, so check)
                        if (SalahTracker.today(ctx)[prayer] != true) {
                            SalahTracker.toggle(ctx, prayer)
                        }
                        finish()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("I have prayed", style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.s))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .clickable { FocusSession.stop(ctx); finish() },
                contentAlignment = Alignment.Center
            ) {
                Text("I will pray in a moment",
                    style = JannahType.button,
                    color = JannahColors.Muted)
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Text(
                "The window is a prompt, not a rule. Allah accepts prayer after it, always.",
                style = JannahType.caption,
                color = JannahColors.Faint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}
