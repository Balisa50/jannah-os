package com.jannahos.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.theme.ScreenScaffold

@Composable
fun OnboardingFlow(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var step by remember { mutableStateOf(0) }

    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("13.4549") }
    var lon by remember { mutableStateOf("-16.5790") }
    var method by remember { mutableStateOf("MWL") }
    var level by remember { mutableStateOf(1) }

    ScreenScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            // Step indicator
            Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)) {
                repeat(5) { i ->
                    Box(
                        Modifier
                            .size(if (i == step) 24.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) JannahColors.Orange
                                else JannahColors.Border
                            )
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            // Forward: slide in from right, slide out to left
                            (slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = androidx.compose.animation.core.tween(350)) + fadeIn(androidx.compose.animation.core.tween(350))) togetherWith
                            (slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = androidx.compose.animation.core.tween(350)) + fadeOut(androidx.compose.animation.core.tween(350)))
                        } else {
                            // Backward: slide in from left, slide out to right
                            (slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = androidx.compose.animation.core.tween(350)) + fadeIn(androidx.compose.animation.core.tween(350))) togetherWith
                            (slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = androidx.compose.animation.core.tween(350)) + fadeOut(androidx.compose.animation.core.tween(350)))
                        }
                    },
                    label = "onboarding_transition"
                ) { currentStep ->
                    when (currentStep) {
                    0 -> Welcome()
                    1 -> TheFourPillars()
                    2 -> AskName(name) { name = it }
                    3 -> Location(lat, lon, method, { lat = it }, { lon = it }, { method = it })else -> DoneScreen(name.ifBlank { "friend" })