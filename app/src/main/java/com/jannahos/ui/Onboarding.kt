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
                repeat(4) { i ->
                    Box(Modifier.size(if (i == step) 24.dp else 6.dp, 6.dp).clip(CircleShape).background(if (i <= step) JannahColors.Orange else JannahColors.Border))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (step) {
                    0 -> Welcome()
                    1 -> AskName(name) { name = it }
                    2 -> AskCity(city) { city = it }
                    else -> DoneScreen(name.ifBlank { "friend" })
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable {
                    if (step == 3) {
                        Settings.setDisplayName(ctx, name)
                        if (city.isNotBlank()) {
                            // Note: we will use default coords for now, real city search comes later
                        }
                        onDone()
                    } else {
                        step += 1
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                Text(if (step == 3) "Begin" else "Continue", style = JannahType.button, color = JannahColors.OnOrange)
            }
            Spacer(Modifier.height(JannahSpacing.l))
        }
    }
}

@Composable
private fun Welcome() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Jannah", fontFamily = FontFamily.Serif, fontSize = 44.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("A quiet system for worship.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 17.sp, color = JannahColors.Orange, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.xxl))
        Text("The operating system for your worship.", style = JannahType.body, color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 24.sp)
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
        OutlinedTextField(value = current, onValueChange = onChange, label = { Text("City (optional)") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun DoneScreen(name: String) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("You're set, $name.", fontFamily = FontFamily.Serif, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.m))
        Text("Come back tonight. Set the alarm.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 17.sp, color = JannahColors.Orange, textAlign = TextAlign.Center)
    }
}
