package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors

@Composable
fun TodayScreen(onNavigate: (String) -> Unit = {}) {
    Column(Modifier.fillMaxSize().background(JannahColors.Cream).verticalScroll(rememberScrollState()).padding(24.dp)) {
        Spacer(Modifier.height(64.dp))
        Text("SAFE MODE", fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        Spacer(Modifier.height(16.dp))
        Text("The app is running in safe mode. Tap the tabs at the bottom to test navigation.", color = JannahColors.Muted, fontSize = 18.sp)
    }
}
