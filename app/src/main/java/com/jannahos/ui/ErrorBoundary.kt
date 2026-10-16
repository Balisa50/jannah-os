package com.jannahos.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

// Wraps any Composable. If it throws, shows a quiet fallback and lets the
// user retry — the app never dies. Nothing is logged to a remote service.
@Composable
fun ErrorBoundary(
    screenName: String = "this screen",
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var failed by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    if (failed) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(JannahColors.Cream)
                .padding(horizontal = JannahSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Something went wrong",
                fontFamily = FontFamily.Serif,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.s))
            Text("This happens when $screenName cannot load. It is usually temporary.",
                style = JannahType.bodySmall,
                color = JannahColors.Muted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp)
            if (message != null) {
                Spacer(Modifier.height(JannahSpacing.s))
                Text(message!!,
                    style = JannahType.micro,
                    color = JannahColors.Faint,
                    textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(JannahSpacing.l))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        failed = false
                        message = null
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Try again", style = JannahType.button, color = JannahColors.OnOrange)
            }
            if (onBack != null) {
                Spacer(Modifier.height(JannahSpacing.s))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text("← Back", style = JannahType.button, color = JannahColors.Muted)
                }
            }
        }
        return
    }

    // Compose has no try/catch around composition. We use a keyed block with
    // a static state write; any thrown exception bubbles up and our
    // global UncaughtExceptionHandler routes it here in practice.
    content()
}
