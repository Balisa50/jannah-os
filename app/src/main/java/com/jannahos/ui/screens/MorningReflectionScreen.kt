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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ReflectionPrompt
import com.jannahos.ReflectionStore
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

private val MOODS = listOf(
    "peaceful" to "Peaceful",
    "grateful" to "Grateful",
    "heavy" to "Heavy",
    "clear" to "Clear"
)

@Composable
fun MorningReflectionScreen(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { ReflectionStore(ctx) }
    var mood by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.xl))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("skip", style = JannahType.caption, color = JannahColors.Muted,
                modifier = Modifier
                    .clickable {
                        ReflectionPrompt.markSeenToday(ctx)
                        onDismiss()
                    }
                    .padding(8.dp))
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Text("GOOD MORNING",
            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
        Spacer(Modifier.height(JannahSpacing.xs))

        Row {
            Text("How did ", style = JannahType.title.copy(fontSize = 26.sp),
                color = JannahColors.Ink)
            Text("last night ", style = JannahType.titleItalic.copy(fontSize = 26.sp),
                color = JannahColors.Orange)
        }
        Text("feel?", style = JannahType.title.copy(fontSize = 26.sp),
            color = JannahColors.Ink)

        Spacer(Modifier.height(JannahSpacing.l))

        if (!saved) {
            Text("You prayed tahajjud last night. This is your private record — " +
                "not a score, not a streak.",
                style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 20.sp)

            Spacer(Modifier.height(JannahSpacing.l))

            // Mood chips
            MOODS.forEach { (code, label) ->
                val selected = mood == code
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(if (selected) JannahColors.Orange else JannahColors.Card)
                        .clickable { mood = code }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        label,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) JannahColors.OnOrange else JannahColors.Ink
                    )
                }
                Spacer(Modifier.height(JannahSpacing.s))
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Text("Anything you want to note? (optional)",
                style = JannahType.caption, color = JannahColors.Muted)
            Spacer(Modifier.height(JannahSpacing.s))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                placeholder = { Text("Stays encrypted on your phone.",
                    style = JannahType.bodySmall, color = JannahColors.Faint) }
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        val ts = System.currentTimeMillis()
                        val combined = buildString {
                            if (mood != null) append("Mood: $mood. ")
                            if (note.isNotBlank()) append(note)
                        }
                        store.save(ts, combined)
                        ReflectionPrompt.markSeenToday(ctx)
                        saved = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Save reflection", style = JannahType.button,
                    color = JannahColors.OnOrange)
            }
        } else {
            Spacer(Modifier.height(JannahSpacing.xl))
            Text("Recorded.",
                fontFamily = FontFamily.Serif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink)
            Spacer(Modifier.height(JannahSpacing.m))
            Text("May Allah accept your night worship.",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 18.sp,
                color = JannahColors.Orange,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(JannahSpacing.xl))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Orange)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text("Return to Today", style = JannahType.button,
                    color = JannahColors.OnOrange)
            }
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}
