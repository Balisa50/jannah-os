package com.jannahos.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.FocusAccessibilityService
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun GuardianSetupScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val isAccessibilityEnabled = isAccessibilityServiceEnabled(ctx)

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar("App Protection", onBack = onBack)

        Column(Modifier.padding(JannahSpacing.gutter)) {
            Text("To block distracting apps during prayer and Quran time, Jannah needs Accessibility permission.", style = JannahType.body, color = JannahColors.Ink)
            
            Spacer(Modifier.height(JannahSpacing.xl))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .pressClickable {
                        if (!isAccessibilityEnabled) {
                            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        } else {
                            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    }
                    .padding(JannahSpacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("App Blocker (Accessibility)", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JannahColors.Ink)
                    Spacer(Modifier.height(4.dp))
                    Text("Intercepts distracting apps and redirects you to Jannah OS.", style = JannahType.caption, color = JannahColors.Muted)
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isAccessibilityEnabled) JannahColors.Orange else JannahColors.Border)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(if (isAccessibilityEnabled) "Active" else "Enable", style = JannahType.micro, color = if (isAccessibilityEnabled) JannahColors.OnOrange else JannahColors.Muted)
                }
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/${FocusAccessibilityService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(service)
}
