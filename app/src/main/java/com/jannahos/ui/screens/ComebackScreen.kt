package com.jannahos.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ComebackCheck
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay

@Composable
fun ComebackScreen(state: ComebackCheck.State, onDismiss: () -> Unit) {
    var stage by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(100); stage = 1
        delay(400); stage = 2
    }
    val alpha by animateFloatAsState(if (stage >= 1) 1f else 0f, tween(700), label = "a")
    val offset by animateFloatAsState(if (stage >= 1) 0f else 16f, tween(700), label = "o")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(JannahSpacing.xxl))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("skip",
                style = JannahType.caption, color = JannahColors.Muted,
                modifier = Modifier.clickable(onClick = onDismiss).padding(6.dp))
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Text("COME BACK", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)

        Spacer(Modifier.height(JannahSpacing.m))

        Text(
            state.headline,
            fontFamily = FontFamily.Serif,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
            modifier = Modifier.graphicsLayer { this.alpha = alpha }
        )

        Spacer(Modifier.height(JannahSpacing.l))

        // The evidence — direct and specific
        if (state.recentPrayers + state.priorPrayers > 0) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                EvidenceTile(
                    Modifier.weight(1f),
                    "LAST 7 DAYS",
                    "${state.recentPrayers}/35"
                )
                EvidenceTile(
                    Modifier.weight(1f),
                    "WEEK BEFORE",
                    "${state.priorPrayers}/35"
                )
            }
            Spacer(Modifier.height(JannahSpacing.m))
        }

        Box(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { this.alpha = alpha; translationY = offset }
        ) {
            Text(
                state.body,
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                color = JannahColors.Body,
                lineHeight = 28.sp
            )
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.OrangeSoft)
                .padding(JannahSpacing.l)
                .graphicsLayer { this.alpha = alpha }
        ) {
            Column {
                Text(state.verse,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 16.sp,
                    color = JannahColors.Ink,
                    lineHeight = 26.sp)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(state.verseSource,
                    style = JannahType.micro, color = JannahColors.Faint)
            }
        }

        Spacer(Modifier.height(JannahSpacing.xl))

        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(JannahColors.Orange)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Text(state.cta,
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.OnOrange)
        }

        Spacer(Modifier.height(JannahSpacing.s))

        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Text("Later", style = JannahType.button, color = JannahColors.Muted)
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}

@Composable
private fun EvidenceTile(modifier: Modifier, label: String, value: String) {
    Box(
        modifier
            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
            .background(JannahColors.Card)
            .padding(JannahSpacing.l)
    ) {
        Column {
            Text(label, style = JannahType.micro, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(4.dp))
            Text(value,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink)
        }
    }
}
