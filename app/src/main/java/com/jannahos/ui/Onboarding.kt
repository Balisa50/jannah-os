package com.jannahos.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.CityStore
import com.jannahos.Settings
import com.jannahos.ui.screens.CityPickerScreen
import com.jannahos.ui.screens.PermissionSetupScreen
import com.jannahos.ui.theme.*

@Composable
fun OnboardingFlow(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var showCityPicker by remember { mutableStateOf(false) }

    if (showCityPicker) {
        CityPickerScreen(onBack = { showCityPicker = false }, onPicked = { c, cn ->
            CityStore.set(ctx, c, cn)
            showCityPicker = false
            step = 2
        })
        return
    }

    if (step == 3) {
        PermissionSetupScreen(onAllSet = {
            Settings.setDisplayName(ctx, name)
            onDone()
        })
        return
    }

    ScreenScaffold {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = JannahSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            // Step Indicator (Dots)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    Box(Modifier.size(if (i == step) 24.dp else 6.dp, 6.dp).clip(CircleShape).background(if (i <= step) JannahColors.Orange else JannahColors.Border))
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(350)) + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(350)) + fadeOut(tween(350)))
                        } else {
                            (slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(350)) + fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(350)) + fadeOut(tween(350)))
                        }
                    },
                    label = "onboarding_transition"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> Welcome()
                        1 -> AskName(name) { name = it }
                        2 -> AskLocation { showCityPicker = true }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink)
                    .pressClickable {
                        if (step == 2) step = 3
                        else step++
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(if (step == 2) "Continue" else "Next", style = JannahType.button, color = JannahColors.OnOrange)
            }
            Spacer(Modifier.height(JannahSpacing.l))
        }
    }
}

@Composable
private fun Welcome() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Jannah", fontFamily = FontFamily.Serif, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("A quiet space for worship.\nBuilt to be used less.", fontFamily = FontFamily.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 20.sp, color = JannahColors.Orange, textAlign = TextAlign.Center, lineHeight = 28.sp)
    }
}

@Composable
private fun AskName(current: String, onChange: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("What should we call you?", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.xl))
        OutlinedTextField(value = current, onValueChange = onChange, label = { Text("Your Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    }
}

@Composable
private fun AskLocation(onPick: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Where are you?", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("Used only to calculate accurate prayer times.", style = JannahType.body, color = JannahColors.Muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.xl))
        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.OrangeSoft).pressClickable { onPick() },
            contentAlignment = Alignment.Center
        ) {
            Text("Select City", style = JannahType.button, color = JannahColors.OrangeDeep)
        }
    }
}
