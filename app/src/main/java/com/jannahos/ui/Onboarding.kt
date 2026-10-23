package com.jannahos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.jannahos.Settings
import com.jannahos.ui.theme.*

@Composable
fun OnboardingFlow(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    ScreenScaffold {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = JannahSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))
            
            Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)) {
                repeat(5) { i ->
                    Box(Modifier.size(if (i == step) 24.dp else 6.dp, 6.dp).clip(CircleShape).background(if (i <= step) JannahColors.Orange else JannahColors.Border))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (step) {
                    0 -> Welcome()
                    1 -> WhatYouGet()
                    2 -> AskName(name) { name = it }
                    3 -> AskCity(city) { city = it }
                    else -> DoneScreen(name.ifBlank { "friend" })
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable {
                    if (step == 4) {
                        Settings.setDisplayName(ctx, name)
                        if (city.isNotBlank()) {
                            // City will be set in settings later
                        }
                        onDone()
                    } else {
                        step += 1
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                Text(if (step == 4) "Begin" else "Continue", style = JannahType.button, color = JannahColors.OnOrange)
            }
            Spacer(Modifier.height(JannahSpacing.l))
        }
    }
}

@Composable
private fun Welcome() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Jannah", fontFamily = FontFamily.Serif, fontSize = 48.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("The operating system for your worship.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Orange, textAlign = TextAlign.Center, lineHeight = 26.sp)
        Spacer(Modifier.height(JannahSpacing.xxl))
        Text("Not another app you have to open. A guardian that comes for you.", style = JannahType.body, color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 24.sp)
    }
}

@Composable
private fun WhatYouGet() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("What you get.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.l))
        
        Feature("Prayer times", "Calculated for your exact city. Adhan that actually rings.")
        Feature("Quran reader", "604 pages. Locked ranges so you actually finish.")
        Feature("Reading Plan", "Pick your pace. Get exact pages every day.")
        Feature("Locks & Focus", "Your phone locks during Fard prayer. It won't let you skip.")
        Feature("Diary", "Write how you feel. The dashboard gives you verses to heal.")
    }
}

@Composable
private fun Feature(title: String, desc: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(title, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        Spacer(Modifier.height(2.dp))
        Text(desc, style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 22.sp)
    }
}

@Composable
private fun AskName(current: String, onChange: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("What should we call you?", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.l))
        OutlinedTextField(value = current, onValueChange = onChange, label = { Text("Your name") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun AskCity(current: String, onChange: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Where are you?", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.l))
        OutlinedTextField(value = current, onValueChange = onChange, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun DoneScreen(name: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("You're set, $name.", fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.m))
        Text("Come back tonight. Sleep with intention.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 17.sp, color = JannahColors.Orange, textAlign = TextAlign.Center)
    }
}
