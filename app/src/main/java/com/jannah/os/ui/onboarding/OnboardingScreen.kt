package com.jannah.os.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Cream = Color(0xFFFBF6EE)
private val Orange = Color(0xFFE07A3F)
private val Ink = Color(0xFF1F1B17)
private val InkSoft = Color(0xFF8A8078)
private val Hairline = Color(0xFFE7DED1)

private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private const val TOTAL_STEPS = 5

@Composable
fun OnboardingScreen(
    initialName: String = "",
    onComplete: (name: String) -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(initialName) }
    var accessibilityGranted by rememberSaveable { mutableStateOf(false) }
    var batteryGranted by rememberSaveable { mutableStateOf(false) }
    var dndGranted by rememberSaveable { mutableStateOf(false) }

    val slideSpec = tween<Int>(durationMillis = 340, easing = FastOutSlowInEasing)
    val fadeSpec = tween<Float>(durationMillis = 260, easing = FastOutSlowInEasing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(56.dp))
            StepIndicator(step = step, total = TOTAL_STEPS)
            Spacer(Modifier.height(40.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val forward = targetState > initialState
                    val enterX = if (forward) 1 else -1
                    val exitX = if (forward) -1 else 1
                    (
                        slideInHorizontally(slideSpec) { full -> enterX * full / 6 } + fadeIn(fadeSpec)
                    ).togetherWith(
                        slideOutHorizontally(slideSpec) { full -> exitX * full / 6 } + fadeOut(fadeSpec)
                    )
                },
                label = "onboarding_step",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { current ->
                when (current) {
                    0 -> WelcomeStep()
                    1 -> NameStep(name = name, onNameChange = { name = it })
                    2 -> PermissionStep(
                        title = "Accessibility",
                        body = "Lets Jannah see when a lock screen should appear over the current app.",
                        granted = accessibilityGranted,
                        onGranted = { accessibilityGranted = true }
                    )
                    3 -> PermissionStep(
                        title = "Battery & Alarms",
                        body = "Ensures prayer notifications fire on time, even in deep sleep.",
                        granted = batteryGranted,
                        onGranted = { batteryGranted = true }
                    )
                    4 -> PermissionStep(
                        title = "Do Not Disturb",
                        body = "Silences noise automatically when a lock screen opens.",
                        granted = dndGranted,
                        onGranted = { dndGranted = true }
                    )
                }
            }

            PrimaryButton(
                label = if (step == TOTAL_STEPS - 1) "Enter" else "Continue",
                enabled = when (step) {
                    1 -> name.trim().length >= 1
                    else -> true
                },
                onClick = {
                    if (step == TOTAL_STEPS - 1) {
                        onComplete(name.trim())
                    } else {
                        step += 1
                    }
                }
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun StepIndicator(step: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            val active = i <= step
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(if (i == step) 22.dp else 10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (active) Orange else Hairline)
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column {
        Text(
            text = "Jannah",
            fontFamily = Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 44.sp,
            color = Ink
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "A quiet space for prayer, reading, and reflection.",
            fontFamily = Sans,
            fontSize = 16.sp,
            color = InkSoft,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    Column {
        Text(
            text = "What should we call you?",
            fontFamily = Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = Ink
        )
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, Hairline, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            if (name.isEmpty()) {
                Text("Your name", color = InkSoft, fontSize = 16.sp, fontFamily = Sans)
            }
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                textStyle = TextStyle(
                    color = Ink,
                    fontSize = 16.sp,
                    fontFamily = Sans
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionStep(
    title: String,
    body: String,
    granted: Boolean,
    onGranted: () -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = Ink
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            fontFamily = Sans,
            fontSize = 15.sp,
            color = InkSoft,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(28.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (granted) Color(0xFFF3EADF) else Color.White)
                .border(1.dp, Hairline, RoundedCornerShape(12.dp))
                .clickable { onGranted() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (granted) Orange else Color.White)
                    .border(1.dp, if (granted) Orange else Hairline, CircleShape)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = if (granted) "Enabled" else "Enable",
                fontFamily = Sans,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Ink
            )
        }
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) Orange else Hairline)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = Sans,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) Color.White else InkSoft,
            textAlign = TextAlign.Center
        )
    }
}
