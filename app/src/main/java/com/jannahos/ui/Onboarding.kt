package com.jannahos.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.jannahos.ui.screens.PermissionSetupScreen
import com.jannahos.ui.theme.*

@Composable
fun OnboardingFlow(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }

    if (step == 2) {
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
            
            Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)) {
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
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius)).background(JannahColors.Ink).clickable {
                    if (step == 1) step = 2 else step = 1
                },
                contentAlignment = Alignment.Center
            ) {
                Text(if (step == 1) "Next" else "Continue", style = JannahType.button, color = JannahColors.OnOrange)
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
        Text("A quiet space for worship.", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 18.sp, color = JannahColors.Orange, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AskName(current: String, onChange: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("What should we call you?", fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.l))
        OutlinedTextField(value = current, onValueChange = onChange, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
    }
}
