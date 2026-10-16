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
import androidx.compose.runtime.remember
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
import com.jannahos.GraduationCheck
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun GraduationScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val verdict = remember { GraduationCheck.evaluate(ctx) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(JannahSpacing.xxl))

        Text("GRADUATION",
            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)

        Spacer(Modifier.height(JannahSpacing.l))

        if (verdict.ready) {
            Text("You do not need us.",
                fontFamily = FontFamily.Serif,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp)

            Spacer(Modifier.height(JannahSpacing.m))

            Text("The habit holds.",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 18.sp,
                color = JannahColors.Orange,
                textAlign = TextAlign.Center)

            Spacer(Modifier.height(JannahSpacing.xxl))

            // The numbers, honestly
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("THE RECORD",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.m))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${verdict.stats.verified + verdict.stats.claimed}",
                                fontFamily = FontFamily.Serif,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                            Text("nights you showed up",
                                style = JannahType.caption, color = JannahColors.Muted,
                                textAlign = TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text(
                        "${verdict.stats.verified} of those were recited aloud and recognised.",
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // The offer
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("WHAT THIS MEANS",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "The app was never the point. Your practice was. Everything you wrote stays on your phone. Uninstall tomorrow and the habit stays. Come back in a year and it is all still here.",
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "The door is unlocked.",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.sp,
                        color = JannahColors.Ink,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            // The du'a
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("A DUA FOR YOU",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "\"Our Lord, let not our hearts deviate after You have guided us. Grant us mercy from Yourself, for You are the Bestower.\"",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        color = JannahColors.Ink,
                        lineHeight = 26.sp
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("Qur'an 3:8",
                        style = JannahType.micro, color = JannahColors.Faint)
                }
            }
        } else {
            Text("Not yet.",
                fontFamily = FontFamily.Serif,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink,
                textAlign = TextAlign.Center)

            Spacer(Modifier.height(JannahSpacing.m))

            Text(verdict.reason,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 17.sp,
                color = JannahColors.Orange,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp)

            Spacer(Modifier.height(JannahSpacing.xl))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("WHERE YOU ARE",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.m))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCol("${verdict.stats.total}", "nights logged")
                        StatCol("${verdict.nightsConsidered}", "nights you showed up")
                        StatCol("${(verdict.rate * 100).toInt()}%", "consistency")
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Text(
                    "This is not a judgment. Most Muslims take years. Come back tomorrow.",
                    style = JannahType.bodySmall,
                    color = JannahColors.Body,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(JannahSpacing.xl))

        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(JannahColors.Ink)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("Return to the app", style = JannahType.button, color = JannahColors.OnOrange)
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}

@Composable
private fun StatCol(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value,
            fontFamily = FontFamily.Serif,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink)
        Spacer(Modifier.height(2.dp))
        Text(label,
            style = JannahType.caption, color = JannahColors.Muted,
            textAlign = TextAlign.Center)
    }
}
