package com.jannahos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.TopBar
import com.jannahos.ui.screens.CityPickerScreen
import com.jannahos.ui.screens.GuardianSetupScreen
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.jannahos.ui.theme.JannahTheme {
                SwipeBackScreen(onBack = { finish() }) { SettingsScreen(onBack = { finish() }) }
            }
        }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var showCityPicker by remember { mutableStateOf(false) }
    var showGuardian by remember { mutableStateOf(false) }

    if (showCityPicker) {
        CityPickerScreen(onBack = { showCityPicker = false }, onPicked = { c, cn -> CityStore.set(ctx, c, cn); showCityPicker = false })
        return
    }
    if (showGuardian) {
        GuardianSetupScreen(onBack = { showGuardian = false })
        return
    }

    var prayerLockOn by remember { mutableStateOf(PrayerLock.enabled(ctx)) }
    var quranLockOn by remember { mutableStateOf(QuranReadingLock.enabled(ctx)) }
    var bedtimeLockOn by remember { mutableStateOf(Settings.isBedtimeLockEnabled(ctx)) }
    var silentTahajjud by remember { mutableStateOf(Settings.isTahajjudSilent(ctx)) }
    var cityName by remember { mutableStateOf(CityStore.name(ctx)) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)) {
        TopBar("Settings", onBack = onBack)

        Spacer(Modifier.height(JannahSpacing.l))
        SectionHeader("Prayer")
        ListRow("City", if (cityName.isBlank()) "Not set" else cityName) { showCityPicker = true }
        Divider()

        Spacer(Modifier.height(JannahSpacing.l))
        SectionHeader("Locks & Focus")
        ListRow("Prayer Lock", if (prayerLockOn) "Locks ALL apps during Fard prayers" else "Off", prayerLockOn) {
            prayerLockOn = !prayerLockOn
            PrayerLock.setEnabled(ctx, prayerLockOn)
            if (prayerLockOn && !isAccessibilityServiceEnabled(ctx)) showGuardian = true
        }
        Divider()
        ListRow("Quran reading lock", if (quranLockOn) "Random time before midnight" else "Off", quranLockOn) {
            quranLockOn = !quranLockOn
            QuranReadingLock.setEnabled(ctx, quranLockOn)
        }
        Divider()
        ListRow("Bedtime lock (Al-Mulk)", if (bedtimeLockOn) "5 mins before sleep" else "Off", bedtimeLockOn) {
            bedtimeLockOn = !bedtimeLockOn
            Settings.setBedtimeLockEnabled(ctx, bedtimeLockOn)
        }
        Divider()
        ListRow("Silent tahajjud", if (silentTahajjud) "Vibrate only" else "Sound", silentTahajjud) {
            silentTahajjud = !silentTahajjud
            Settings.setTahajjudSilent(ctx, silentTahajjud)
        }
        
        Spacer(Modifier.height(JannahSpacing.l))
        SectionHeader("App")
        ListRow("Send test notification", "Check if notifications work") { 
            // Trigger test notification
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "test"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                nm.createNotificationChannel(android.app.NotificationChannel(channelId, "Test", android.app.NotificationManager.IMPORTANCE_HIGH))
            }
            val notif = androidx.core.app.NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Jannah")
                .setContentText("Notifications are working.")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .build()
            nm.notify(1, notif)
        }
        Divider()
        ListRow("Restart onboarding", "See the welcome screens again") { 
            Settings.resetOnboarding(ctx)
        }
        
        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/${FocusAccessibilityService::class.java.canonicalName}"
    val enabledServices = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    return enabledServices.contains(service)
}

@Composable
private fun SectionHeader(text: String) {
    Text(text.uppercase(), style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s))
}

@Composable
private fun ListRow(title: String, subtitle: String, toggle: Boolean? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = JannahSpacing.gutter, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = JannahType.caption, color = JannahColors.Muted)
        }
        if (toggle != null) {
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(if (toggle) JannahColors.Orange else JannahColors.Border).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(if (toggle) "ON" else "OFF", style = JannahType.micro, color = if (toggle) JannahColors.OnOrange else JannahColors.Muted)
            }
        } else {
            Text("→", style = JannahType.title, color = JannahColors.Orange)
        }
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter).height(1.dp).background(JannahColors.Border))
}
