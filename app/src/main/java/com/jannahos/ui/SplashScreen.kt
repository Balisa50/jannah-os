package com.jannahos.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.R
import com.jannahos.ui.theme.JannahColors
import kotlinx.coroutines.delay

@Composable
fun JannahSplashScreen(onTimeout: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(800), label = "splashAlpha")
    val scale by animateFloatAsState(if (visible) 1f else 0.9f, tween(800), label = "splashScale")

    LaunchedEffect(Unit) {
        visible = true
        delay(2500)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JannahColors.Cream),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Jannah",
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Jannah",
                fontFamily = FontFamily.Serif,
                fontSize = 32.sp,
                color = JannahColors.OrangeDeep
            )
        }
    }
}
