package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.Duas
import com.jannahos.IstighfarStore
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun DhikrScreen() {
    val ctx = LocalContext.current
    val count = remember { mutableStateOf(IstighfarStore.todayCount(ctx)) }
    val dua = Duas.istighfar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.m),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("Dhikr", style = JannahType.screenTitle, color = JannahColors.DayText)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text("Today: ${count.value}", style = JannahType.secondary, color = JannahColors.DayMuted)

        Spacer(Modifier.height(JannahSpacing.m))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(JannahColors.DaySurface)
        ) {
            Column(
                Modifier.padding(JannahSpacing.m).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dua.arabic, style = JannahType.arabic, color = JannahColors.DayText,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(JannahSpacing.m))
                Text(dua.transliteration, style = JannahType.secondary, color = JannahColors.DayMuted,
                    textAlign = TextAlign.Center, lineHeight = 22.sp)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(dua.translation, style = JannahType.primary, color = JannahColors.DayText,
                    textAlign = TextAlign.Center, lineHeight = 24.sp)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(dua.source, style = JannahType.tertiary, color = JannahColors.DayMuted)
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Button(
            onClick = { count.value = IstighfarStore.increment(ctx) },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = JannahColors.Accent,
                contentColor = JannahColors.AccentInk
            )
        ) { Text("Recite one", style = JannahType.button) }

        Spacer(Modifier.height(JannahSpacing.m))

        Text(
            "The Prophet ﷺ sought forgiveness more than seventy times a day.",
            style = JannahType.tertiary, color = JannahColors.DayMuted,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(JannahSpacing.s))

        TextButton(
            onClick = {
                IstighfarStore.reset(ctx)
                count.value = 0
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Reset today", style = JannahType.tertiary, color = JannahColors.DayMuted) }

        Spacer(Modifier.height(JannahSpacing.l))
    }
}
