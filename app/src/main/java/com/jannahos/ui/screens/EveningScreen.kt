package com.jannahos.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.EveningReckoning
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

@Composable
fun EveningScreen(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val summary = remember { EveningReckoning.summarise(ctx) }

    var stage by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(150); stage = 1
        delay(400); stage = 2
    }
    val alpha by animateFloatAsState(if (stage >= 1) 1f else 0f, tween(800), label = "a")
    val offset by animateFloatAsState(if (stage >= 1) 0f else 20f, tween(900), label = "o")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(JannahSpacing.xxl))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
            Text("skip", style = JannahType.caption, color = JannahColors.Muted,
                modifier = Modifier
                    .clickable {
                        EveningReckoning.markSeen(ctx)
                        onDismiss()
                    }
                    .padding(6.dp))
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Text("THE DAY IS CLOSING",
            style = JannahType.eyebrow,
            color = JannahColors.OrangeDeep)

        Spacer(Modifier.height(JannahSpacing.m))

        Text("Before you sleep.",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp,
            modifier = Modifier.graphicsLayer { this.alpha = alpha })

        Spacer(Modifier.height(JannahSpacing.s))

        Text("Not a score. A witness.",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 16.sp,
            color = JannahColors.Orange,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { this.alpha = alpha })

        Spacer(Modifier.height(JannahSpacing.xxl))

        // Four stat tiles
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(JannahSpacing.s)
        ) {
            StatTile(
                Modifier.weight(1f),
                "PRAYERS",
                "${summary.prayersMarked}/${summary.prayersTotal}"
            )
            StatTile(
                Modifier.weight(1f),
                "DHIKR",
                "${summary.dhikrTotal}"
            )
        }
        Spacer(Modifier.height(JannahSpacing.s))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(JannahSpacing.s)
        ) {
            StatTile(
                Modifier.weight(1f),
                "QURAN",
                if (summary.quranRead) "opened" else "—"
            )
            StatTile(
                Modifier.weight(1f),
                "GAVE",
                if (summary.gifted) "yes" else "—"
            )
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Observations
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
                .padding(JannahSpacing.l)
                .graphicsLayer { this.alpha = alpha; translationY = offset }
        ) {
            Column {
                summary.lines.forEach { line ->
                    Text(line,
                        fontFamily = FontFamily.Serif,
                        fontSize = 15.sp,
                        color = JannahColors.Body,
                        lineHeight = 24.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Closing — the compassionate line
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.OrangeSoft)
                .padding(JannahSpacing.l)
                .graphicsLayer { this.alpha = alpha }
        ) {
            Text(summary.closing,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 17.sp,
                color = JannahColors.Ink,
                lineHeight = 26.sp)
        }

        Spacer(Modifier.height(JannahSpacing.xl))

        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(JannahColors.Ink)
                .clickable {
                    EveningReckoning.markSeen(ctx)
                    onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Sleep with intention",
                style = JannahType.button, color = JannahColors.OnOrange)
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, label: String, value: String) {
    Box(
        modifier
            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
            .background(JannahColors.Card)
            .padding(JannahSpacing.l)
    ) {
        Column {
            Text(label,
                style = JannahType.micro, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(4.dp))
            Text(value,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink)
        }
    }
}
