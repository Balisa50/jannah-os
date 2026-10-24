package com.jannahos.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jannahos.FocusAccessibilityService
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun PermissionSetupScreen(onAllSet: () -> Unit) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to track permissions
    var accessibilityOn by remember { mutableStateOf(isAccessibilityEnabled(ctx)) }
    var batteryOn by remember { mutableStateOf(isBatteryOptimizationDisabled(ctx)) }
    var alarmsOn by remember { mutableStateOf(canScheduleExactAlarms(ctx)) }

    // Re-check permissions every time the user returns to this screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityOn = isAccessibilityEnabled(ctx)
                batteryOn = isBatteryOptimizationDisabled(ctx)
                alarmsOn = canScheduleExactAlarms(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream).verticalScroll(rememberScrollState()).padding(JannahSpacing.gutter)) {
        Spacer(Modifier.height(JannahSpacing.xxl))
        
        Text("One last thing.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        Spacer(Modifier.height(8.dp))
        Text("To keep you on track, Jannah needs a few system permissions.", style = JannahType.body, color = JannahColors.Body, lineHeight = 24.sp)

        Spacer(Modifier.height(JannahSpacing.xl))

        // 1. Accessibility
        PermissionCard(
            title = "App Protection",
            description = "Required to lock distracting apps during prayer and Quran time.",
            isGranted = accessibilityOn,
            actionText = if (accessibilityOn) "Enabled" else "Enable Protection"
        ) {
            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        Spacer(Modifier.height(JannahSpacing.m))

        // 2. Battery Optimization
        PermissionCard(
            title = "Background Alarms",
            description = "Required so Android doesn't kill the Adhan alarm when your phone is asleep.",
            isGranted = batteryOn,
            actionText = if (batteryOn) "Allowed" else "Allow Background"
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                }
                ctx.startActivity(intent)
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        // 3. Exact Alarms
        PermissionCard(
            title = "Exact Timings",
            description = "Required to schedule the Adhan at the exact minute.",
            isGranted = alarmsOn,
            actionText = if (alarmsOn) "Allowed" else "Allow Alarms"
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                }
                ctx.startActivity(intent)
            }
        }

        Spacer(Modifier.height(JannahSpacing.xxl))

        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(if (accessibilityOn) JannahColors.Ink else JannahColors.Border)
                .pressClickable(enabled = accessibilityOn) { onAllSet() },
            contentAlignment = Alignment.Center
        ) {
            Text(if (accessibilityOn) "Continue" else "Enable App Protection to continue", style = JannahType.button, color = JannahColors.OnOrange)
        }
    }
}

@Composable
private fun PermissionCard(title: String, description: String, isGranted: Boolean, actionText: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.Card).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                Spacer(Modifier.height(4.dp))
                Text(description, style = JannahType.caption, color = JannahColors.Muted, lineHeight = 18.sp)
            }
            Spacer(Modifier.width(16.dp))
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(if (isGranted) JannahColors.OrangeSoft else JannahColors.Orange)
                    .pressClickable(enabled = !isGranted) { onClick() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(actionText, style = JannahType.micro, color = if (isGranted) JannahColors.OrangeDeep else JannahColors.OnOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun isAccessibilityEnabled(context: Context): Boolean {
    val service = "${context.packageName}/${FocusAccessibilityService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    return enabledServices.contains(service)
}

private fun isBatteryOptimizationDisabled(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

private fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return am.canScheduleExactAlarms()
}
