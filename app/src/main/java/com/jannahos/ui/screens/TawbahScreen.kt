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
import com.jannahos.Mercy
import com.jannahos.ReflectionStore
import com.jannahos.StruggleLog
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun TawbahScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val mercy = remember { Mercy.random() }
    val reflectionStore = remember { ReflectionStore(ctx) }
    val struggle = remember { StruggleLog(ctx) }

    var step by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        // Banner
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.OrangeSoft)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("TAWBAH",
                    style = JannahType.eyebrow,
                    color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("Return ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.Ink)
                    Text("to Allah.", style = JannahType.titleItalic.copy(fontSize = 24.sp),
                        color = JannahColors.Orange)
                }
                Spacer(Modifier.height(4.dp))
                Text("No shame. No exposure. Just you and Him.",
                    style = JannahType.caption, color = JannahColors.Muted)
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            when (step) {
                0 -> {
                    // Show mercy first
                    Text("BEFORE YOU BEGIN", style = JannahType.eyebrow,
                        color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.Card)
                            .padding(JannahSpacing.l)
                    ) {
                        Column {
                            Text(mercy.text,
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                fontStyle = FontStyle.Italic,
                                color = JannahColors.Ink,
                                lineHeight = 24.sp)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text(mercy.source, style = JannahType.micro,
                                color = JannahColors.Faint)
                        }
                    }

                    Spacer(Modifier.height(JannahSpacing.l))

                    Text("Three steps. In any order.",
                        style = JannahType.body, color = JannahColors.Ink)
                    Spacer(Modifier.height(JannahSpacing.s))
                    StepRow("1", "Acknowledge it to Allah.")
                    StepRow("2", "Intend, sincerely, to leave it.")
                    StepRow("3", "If it involves someone else, plan to make it right.")

                    Spacer(Modifier.height(JannahSpacing.xl))

                    PillButton("Continue") { step = 1 }
                }
                1 -> {
                    Text("REFLECT (OPTIONAL)",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("What triggered this? What will help next time? Write it here. " +
                        "It stays on your phone, encrypted. Do not include details you would not want read aloud.",
                        style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 20.sp)

                    Spacer(Modifier.height(JannahSpacing.m))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        placeholder = { Text("Write anything that helps you understand the trigger...",
                            style = JannahType.bodySmall, color = JannahColors.Faint) }
                    )

                    Spacer(Modifier.height(JannahSpacing.l))

                    PillButton("Continue") { step = 2 }
                    Spacer(Modifier.height(JannahSpacing.s))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clickable { step = 2 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Skip reflection", style = JannahType.caption,
                            color = JannahColors.Muted)
                    }
                }
                2 -> {
                    // Final: say astaghfirullah, commit
                    Text("SAY IT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.l))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                            .background(JannahColors.Card)
                            .padding(JannahSpacing.l)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()) {
                            Text("أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                                style = JannahType.arabicLarge, color = JannahColors.Ink,
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(JannahSpacing.m))
                            Text("Astaghfirullaha wa atubu ilayh",
                                style = JannahType.bodySmall, color = JannahColors.Muted)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text("I seek the forgiveness of Allah and turn to Him in repentance.",
                                style = JannahType.body, color = JannahColors.Body,
                                textAlign = TextAlign.Center, lineHeight = 22.sp)
                            Spacer(Modifier.height(JannahSpacing.s))
                            Text("Sahih al-Bukhari 6307, Sahih Muslim 6799",
                                style = JannahType.micro, color = JannahColors.Faint)
                        }
                    }

                    Spacer(Modifier.height(JannahSpacing.xl))

                    PillButton("I have repented") {
                        val ts = System.currentTimeMillis()
                        struggle.record("tawbah")
                        if (note.isNotBlank()) {
                            reflectionStore.save(ts, note)
                        }
                        saved = true
                        step = 3
                    }
                }
                3 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(JannahSpacing.xl))
                        Text("Recorded.",
                            fontFamily = FontFamily.Serif,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.m))
                        Text("Allah knows what you did, and you know. That is enough.",
                            style = JannahType.body, color = JannahColors.Body,
                            textAlign = TextAlign.Center, lineHeight = 22.sp)
                        Spacer(Modifier.height(JannahSpacing.l))
                        Text(mercy.text,
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            color = JannahColors.Ink,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(mercy.source, style = JannahType.micro,
                            color = JannahColors.Faint, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())

                        Spacer(Modifier.height(JannahSpacing.xl))
                        PillButton("Close") { onBack() }
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun StepRow(num: String, text: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(JannahColors.Orange)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(num, style = JannahType.micro, color = JannahColors.OnOrange)
        }
        Spacer(Modifier.padding(horizontal = 6.dp))
        Text(text, style = JannahType.bodySmall, color = JannahColors.Body)
    }
}

@Composable
private fun PillButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(JannahColors.Ink)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(label, style = JannahType.button, color = JannahColors.OnOrange)
    }
}
