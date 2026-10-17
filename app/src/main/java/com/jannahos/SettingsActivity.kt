package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.jannahos.ui.theme.JannahTheme {
                Box(Modifier.fillMaxSize().background(JannahColors.Cream)) {
                    SettingsScreen(onBack = { finish() })
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf(Settings.displayName(ctx)) }
    var lat by remember { mutableStateOf(Settings.lat(ctx).toString()) }
    var lon by remember { mutableStateOf(Settings.lon(ctx).toString()) }
    var method by remember { mutableStateOf(Settings.method(ctx)) }
    var adhan by remember { mutableStateOf(AdhanSettings.current(ctx)) }
    var focusOn by remember { mutableStateOf(FocusSession.enabled(ctx)) }
    var lockOn by remember { mutableStateOf(PrayerLock.enabled(ctx)) }
    var quranLockOn by remember { mutableStateOf(QuranReadingLock.enabled(ctx)) }
    var lockMinutes by remember { mutableStateOf(PrayerLock.durationMinutes(ctx)) }
    var toast by remember { mutableStateOf<String?>(null) }
    var nameExpanded by remember { mutableStateOf(false) }
    var locExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }
    var adhanExpanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        TopBar("Settings", onBack = onBack)

        Spacer(Modifier.height(JannahSpacing.s))

        // ─── Section: You ─────────────────────────────────
        SectionHeader("You")

        ListRow(
            title = "Name",
            subtitle = if (name.isBlank()) "Not set" else name,
            onClick = { nameExpanded = !nameExpanded }
        )
        if (nameExpanded) {
            InsetField(name, "First name") {
                name = it
                Settings.setDisplayName(ctx, it)
            }
        }

        // ─── Section: Prayer ──────────────────────────────
        Spacer(Modifier.height(JannahSpacing.l))
        SectionHeader("Prayer")

        ListRow(
            title = "Location",
            subtitle = "%.4f, %.4f".format(Settings.lat(ctx), Settings.lon(ctx)),
            onClick = { locExpanded = !locExpanded }
        )
        if (locExpanded) {
            InsetField(lat, "Latitude") { lat = it; persistLocation(ctx, lat, lon, method) { toast = it } }
            InsetField(lon, "Longitude") { lon = it; persistLocation(ctx, lat, lon, method) { toast = it } }
        }

        Divider()

        ListRow(
            title = "Calculation method",
            subtitle = Settings.METHODS.firstOrNull { it.first == method }?.second ?: method,
            onClick = { methodExpanded = !methodExpanded }
        )
        if (methodExpanded) {
            Settings.METHODS.forEach { (code, label) ->
                SelectRow(label, method == code) {
                    method = code
                    Settings.set(ctx, Settings.lat(ctx), Settings.lon(ctx), code)
                    toast = "Method: $label"
                }
            }
        }

        Divider()

        ListRow(
            title = "Adhan sound",
            subtitle = AdhanSettings.options.firstOrNull { it.id == adhan }?.label ?: "",
            onClick = { adhanExpanded = !adhanExpanded }
        )
        if (adhanExpanded) {
            AdhanSettings.options.forEach { opt ->
                SelectRow(opt.label, adhan == opt.id) {
                    adhan = opt.id
                    AdhanSettings.set(ctx, opt.id)
                    toast = "Adhan: ${opt.label}"
                }
            }
        }

        Divider()

        ListRow(
            title = "Prayer lock",
            subtitle = if (lockOn) "$lockMinutes minutes at prayer time" else "Off",
            toggle = lockOn
        ) {
            lockOn = !lockOn
            PrayerLock.setEnabled(ctx, lockOn)
            toast = if (lockOn) "Prayer lock on. Enable accessibility service to arm it." else "Prayer lock off."
        }
        if (lockOn) {
            DurationPicker(lockMinutes) { mins ->
                lockMinutes = mins
                PrayerLock.setDurationMinutes(ctx, mins)
                toast = "Lock duration: $mins minutes"
            }
            Inset("Requires Settings > Accessibility > Jannah to be enabled. The phone returns you to the prayer screen if you open any other app during the window.") {
                ctx.startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        Divider()

        ListRow(
            title = "Quran reading lock",
            subtitle = if (quranLockOn) "A page before the day ends" else "Off",
            toggle = quranLockOn
        ) {
            quranLockOn = !quranLockOn
            QuranReadingLock.setEnabled(ctx, quranLockOn)
            toast = if (quranLockOn) "Quran lock on. Enable accessibility service to arm it."
                    else "Quran lock off."
        }

        Divider()

        ListRow(
            title = "Focus mode",
            subtitle = if (focusOn) "On" else "Off",
            toggle = focusOn
        ) {
            focusOn = !focusOn
            FocusSession.setEnabled(ctx, focusOn)
            toast = if (focusOn) "Focus mode on." else "Focus mode off."
        }

        // ─── Section: App ─────────────────────────────────
        Spacer(Modifier.height(JannahSpacing.l))
        SectionHeader("App")

        ListRow(
            title = "Send test notification",
            onClick = {
                sendTestNotification(ctx)
                toast = "Notification sent."
            }
        )
        Divider()
        ListRow(
            title = "Restart onboarding",
            onClick = {
                Settings.resetOnboarding(ctx)
                toast = "Reopen the app to restart."
            }
        )

        // ─── Section: Data ────────────────────────────────
        Spacer(Modifier.height(JannahSpacing.l))
        SectionHeader("Data")

        ListRow(
            title = "Clear istighfar today",
            onClick = {
                IstighfarStore.reset(ctx)
                toast = "Istighfar count cleared."
            }
        )
        Divider()
        ListRow(
            title = "Clear planner tasks",
            onClick = {
                PlannerStore(ctx).writableDatabase.delete("tasks", null, null)
                toast = "Planner cleared."
            }
        )
        Divider()
        ListRow(
            title = "Erase prayer & tahajjud history",
            onClick = {
                Stores.wake(ctx).writableDatabase.delete("events", null, null)
                toast = "History erased."
            }
        )

        if (toast != null) {
            Spacer(Modifier.height(JannahSpacing.m))
            Text(
                toast!!,
                style = JannahType.caption,
                color = JannahColors.Orange,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = JannahSpacing.gutter)
            )
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}

private fun persistLocation(
    ctx: Context,
    latS: String,
    lonS: String,
    method: String,
    onDone: (String) -> Unit
) {
    val latD = latS.toDoubleOrNull()
    val lonD = lonS.toDoubleOrNull()
    if (latD == null || lonD == null) {
        onDone("Latitude and longitude must be numbers.")
        return
    }
    Settings.set(ctx, latD, lonD, method)
    onDone("Location saved.")
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text.uppercase(),
        style = JannahType.eyebrow,
        color = JannahColors.OrangeDeep,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s)
    )
}

@Composable
private fun ListRow(
    title: String,
    subtitle: String? = null,
    toggle: Boolean? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = JannahSpacing.gutter, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title,
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle,
                    style = JannahType.caption,
                    color = JannahColors.Muted)
            }
        }
        if (toggle != null) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (toggle) JannahColors.Orange else JannahColors.Border)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (toggle) "ON" else "OFF",
                    style = JannahType.micro,
                    color = if (toggle) JannahColors.OnOrange else JannahColors.Muted)
            }
        } else {
            Text("→",
                style = JannahType.title,
                color = JannahColors.Orange)
        }
    }
}

@Composable
private fun SelectRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = JannahSpacing.gutter + 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            style = JannahType.body,
            color = if (selected) JannahColors.Ink else JannahColors.Body,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f))
        if (selected) {
            Text("✓", style = JannahType.body, color = JannahColors.Orange)
        }
    }
}

@Composable
private fun InsetField(value: String, label: String, onChange: (String) -> Unit) {
    Box(Modifier.padding(horizontal = JannahSpacing.gutter).padding(bottom = JannahSpacing.s)) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Inset(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = JannahSpacing.gutter, vertical = JannahSpacing.s)
            .clip(RoundedCornerShape(12.dp))
            .background(JannahColors.OrangeSoft)
            .clickable(onClick = onClick)
            .padding(JannahSpacing.m)
    ) {
        Text(text,
            style = JannahType.caption,
            color = JannahColors.OrangeDeep,
            lineHeight = 18.sp)
    }
}

@Composable
private fun DurationPicker(current: Int, onPick: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = JannahSpacing.gutter)
            .padding(bottom = JannahSpacing.s),
        horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
    ) {
        listOf(5, 10, 15, 20).forEach { m ->
            val selected = current == m
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) JannahColors.Orange else JannahColors.Card)
                    .clickable { onPick(m) },
                contentAlignment = Alignment.Center
            ) {
                Text("$m min",
                    style = JannahType.caption,
                    color = if (selected) JannahColors.OnOrange else JannahColors.Ink,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = JannahSpacing.gutter)
            .height(1.dp)
            .background(JannahColors.Divider)
    )
}

private fun sendTestNotification(ctx: Context) {
    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "test"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Test", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }
    val notif = androidx.core.app.NotificationCompat.Builder(ctx, channelId)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle("Jannah")
        .setContentText("Notifications are working.")
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()
    nm.notify(9999, notif)
}
