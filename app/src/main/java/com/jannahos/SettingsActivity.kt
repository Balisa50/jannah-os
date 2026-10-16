package com.jannahos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.jannahos.ui.theme.GlassCard
import com.jannahos.ui.theme.JannahBrushes
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SettingsScreen()
                }
            }
        }
    }

    @Composable
    private fun SettingsScreen() {
        val ctx = LocalContext.current
        var lat by remember { mutableStateOf(Settings.lat(ctx).toString()) }
        var lon by remember { mutableStateOf(Settings.lon(ctx).toString()) }
        var method by remember { mutableStateOf(Settings.method(ctx)) }
        var toast by remember { mutableStateOf<String?>(null) }
        var bedReminder by remember { mutableStateOf(TahajjudPlan.bedtimeReminderOn(ctx)) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.gutter),
        ) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("Settings", style = JannahType.title, color = JannahColors.Ivory)
            Spacer(Modifier.height(JannahSpacing.xs))
            Text("Location, calculation, and app state.",
                style = JannahType.caption, color = JannahColors.Muted)

            Spacer(Modifier.height(JannahSpacing.l))

            // --- LOCATION CARD ---
            GlassCard {
                Column(Modifier.padding(JannahSpacing.m)) {
                    Text("LOCATION", style = JannahType.eyebrow, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    OutlinedTextField(
                        value = lat, onValueChange = { lat = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    OutlinedTextField(
                        value = lon, onValueChange = { lon = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // --- METHOD CARD ---
            GlassCard {
                Column(Modifier.padding(JannahSpacing.m)) {
                    Text("CALCULATION METHOD", style = JannahType.eyebrow, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Settings.METHODS.forEach { (code, label) ->
                        val selected = method == code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) JannahColors.Deep else JannahColors.Surface)
                                .clickable { method = code }
                                .padding(vertical = 12.dp, horizontal = JannahSpacing.s),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                label,
                                style = JannahType.bodySmall,
                                color = if (selected) JannahColors.Ivory else JannahColors.Muted,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) Text("✓", style = JannahType.bodySmall, color = JannahColors.Gold)
                        }
                        Spacer(Modifier.height(JannahSpacing.xs))
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // --- ACTIONS ---
            GlassCard {
                Column(Modifier.padding(JannahSpacing.m)) {
                    Text("ACTIONS", style = JannahType.eyebrow, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    ActionRow("Save location & method") {
                        val latD = lat.toDoubleOrNull()
                        val lonD = lon.toDoubleOrNull()
                        if (latD == null || lonD == null) {
                            toast = "Latitude and longitude must be numbers."
                        } else {
                            Settings.set(ctx, latD, lonD, method)
                            toast = "Saved."
                        }
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    ActionRow(
                        if (bedReminder) "Bedtime reminder: ON" else "Bedtime reminder: OFF"
                    ) {
                        val next = !bedReminder
                        TahajjudPlan.setBedtimeReminder(ctx, next)
                        if (next) BedtimeScheduler.schedule(ctx)
                        bedReminder = next
                        toast = if (next) "Bedtime reminder on." else "Bedtime reminder off."
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    ActionRow("Send test notification") {
                        sendTestNotification(ctx)
                        toast = "Notification sent."
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    ActionRow("Restart onboarding") {
                        Settings.resetOnboarding(ctx)
                        toast = "Reopen the app to restart."
                    }
                }
            }

            if (toast != null) {
                Spacer(Modifier.height(JannahSpacing.m))
                Text(toast!!, style = JannahType.caption, color = JannahColors.Gold,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // --- DANGER ---
            GlassCard {
                Column(Modifier.padding(JannahSpacing.m)) {
                    Text("DATA", style = JannahType.eyebrow, color = JannahColors.Crimson)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "Everything is stored on this device. Nothing is uploaded.",
                        style = JannahType.caption, color = JannahColors.Muted, lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    ActionRow("Clear istighfar today") {
                        IstighfarStore.reset(ctx)
                        toast = "Istighfar count cleared."
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    ActionRow("Clear planner tasks") {
                        PlannerStore(ctx).writableDatabase.delete("tasks", null, null)
                        toast = "Planner cleared."
                    }
                    Spacer(Modifier.height(JannahSpacing.xs))
                    ActionRow("Erase prayer & tahajjud history") {
                        WakeLog(ctx).writableDatabase.delete("events", null, null)
                        toast = "History erased."
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }

    @Composable
    private fun ActionRow(label: String, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(JannahColors.Deep)
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = JannahSpacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = JannahType.bodySmall, color = JannahColors.Ivory,
                modifier = Modifier.weight(1f))
            Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
        }
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
        val notif = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Jannah OS")
            .setContentText("Notifications are working.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(9999, notif)
    }
}
