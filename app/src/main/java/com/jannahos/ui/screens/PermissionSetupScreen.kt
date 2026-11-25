package com.jannahos.ui.screens

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

    var accessibilityOn by remember { mutableStateOf(isAccessibilityEnabled(ctx)) }
    var batteryOn by remember { mutableStateOf(isBatteryOptimizationDisabled(ctx)) }
    var alarmsOn by remember { mutableStateOf(canScheduleExactAlarms(ctx)) }
    var dndOn by remember { mutableStateOf(isDndAllowed(ctx)) }
    var notificationsOn by remember { mutableStateOf(areNotificationsEnabled(ctx)) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationsOn = isGranted
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityOn = isAccessibilityEnabled(ctx)
                batteryOn = isBatteryOptimizationDisabled(ctx)
                alarmsOn = canScheduleExactAlarms(ctx)
                dndOn = isDndAllowed(ctx)
                notificationsOn = areNotificationsEnabled(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream).verticalScroll(rememberScrollState()).padding(JannahSpacing.gutter)) {
        Spacer(Modifier.height(JannahSpacing.xxl))

        Text("Final steps.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        Spacer(Modifier.height(8.dp))
        Text("Notifications are needed for reminders. App protection, exact timing and quiet-mode controls are optional capabilities you can enable when you want them.", style = JannahType.body, color = JannahColors.Body, lineHeight = 24.sp)

        Spacer(Modifier.height(JannahSpacing.xl))

        // 1. Notifications
        PermissionCard(
            title = "Notifications",
            description = "Required for Adhan alarms and prayer reminders.",
            isGranted = notificationsOn,
            actionText = if (notificationsOn) "Enabled" else "Enable"
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificationsOn = true
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        // 2. Accessibility
        PermissionCard(
            title = "App Protection",
            description = "Optional: blocks distracting apps during prayer and Quran time.",
            isGranted = accessibilityOn,
            actionText = if (accessibilityOn) "Enabled" else "Enable"
        ) {
            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        Spacer(Modifier.height(JannahSpacing.m))

        // 3. Battery
        PermissionCard(
            title = "Background Alarms",
            description = "Optional: may improve delivery on devices with aggressive battery management.",
            isGranted = batteryOn,
            actionText = if (batteryOn) "Allowed" else "Allow"
        ) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${ctx.packageName}")
            }
            ctx.startActivity(intent)
        }

        Spacer(Modifier.height(JannahSpacing.m))

        // 4. Exact Alarms
        PermissionCard(
            title = "Exact Timings",
            description = "Optional: requests exact alarm access. Without it, Android may delay reminders.",
            isGranted = alarmsOn,
            actionText = if (alarmsOn) "Allowed" else "Allow"
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                } catch (_: Throwable) {
                    ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${ctx.packageName}")
                    })
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        // 5. DND
        PermissionCard(
            title = "Digital Sanctuary",
            description = "Optional: lets you enable Do Not Disturb during supported sessions.",
            isGranted = dndOn,
            actionText = if (dndOn) "Allowed" else "Allow"
        ) {
            ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }

        Spacer(Modifier.height(JannahSpacing.xxl))

        val allRequiredGranted = notificationsOn
        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(JannahSpacing.pillRadius))
                .background(if (allRequiredGranted) JannahColors.Ink else JannahColors.Border)
                .pressClickable(enabled = allRequiredGranted) { onAllSet() },
            contentAlignment = Alignment.Center
        ) {
            Text(if (allRequiredGranted) "Finish Setup" else "Enable notifications to continue", style = JannahType.button, color = JannahColors.OnOrange, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
    return try { am.canScheduleExactAlarms() } catch (_: Throwable) { false }
}

private fun isDndAllowed(context: Context): Boolean {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.isNotificationPolicyAccessGranted
}

private fun areNotificationsEnabled(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return nm.areNotificationsEnabled()
    }
    return true
}
