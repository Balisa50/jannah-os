package com.jannahos.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun CrashReportScreen(trace: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter)
    ) {
        Spacer(Modifier.height(JannahSpacing.xl))

        Text("A crash was logged",
            fontFamily = FontFamily.Serif,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink)

        Spacer(Modifier.height(JannahSpacing.s))

        Text("The app closed unexpectedly. The trace is below. Copy it and send it to the developer.",
            style = JannahType.bodySmall,
            color = JannahColors.Muted,
            lineHeight = 20.sp)

        Spacer(Modifier.height(JannahSpacing.l))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
                .padding(JannahSpacing.l)
        ) {
            Text(
                trace,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = JannahColors.Ink,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Card)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text("Dismiss", style = JannahType.button, color = JannahColors.Muted)
            }
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}
