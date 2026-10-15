package com.jannahos

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val REASONS = listOf(
    "slept_late" to "I slept too late",
    "alarm_missed" to "The alarm did not wake me",
    "phone_far" to "The phone was too far",
    "went_back" to "I woke but went back",
    "too_tired" to "I was too exhausted"
)

private fun readableReason(code: String): String =
    REASONS.firstOrNull { it.first == code }?.second ?: code

class MainActivity : ComponentActivity() {

    private val lat: Double get() = Settings.lat(this)
    private val lon: Double get() = Settings.lon(this)

    // Bumped on every resume so child screens' writes show up when we come back.
    private val resumeTick = mutableStateOf(0)

    override fun onResume() {
        super.onResume()
        resumeTick.value = resumeTick.value + 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.RECORD_AUDIO
                ), 100
            )
        }
        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val pending = remember { WakeLog(this).pendingFollowUp() }
                    if (pending != null) {
                        FollowUpScreen(pending)
                    } else {
                        Dashboard()
                    }
                }
            }
        }
    }

    // ---------- Follow-up screen (missed last night) ----------

    @Composable
    private fun FollowUpScreen(event: WakeLog.Event) {
        val log = remember { WakeLog(this) }
        val answered = remember { mutableStateOf(false) }
        val showDashboard = remember { mutableStateOf(false) }

        if (showDashboard.value) {
            Dashboard()
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.screenGutter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!answered.value) {
                Text("Last night", style = JannahType.section, color = JannahColors.DayMuted)
                Spacer(Modifier.height(JannahSpacing.m))
                Text("What got in the way?", style = JannahType.screenTitle,
                    color = JannahColors.DayText, textAlign = TextAlign.Center)
                Spacer(Modifier.height(JannahSpacing.s))
                Text("This shapes tonight's plan. It is not a punishment.",
                    style = JannahType.secondary, color = JannahColors.DayMuted,
                    textAlign = TextAlign.Center)

                Spacer(Modifier.height(JannahSpacing.xl))

                REASONS.forEach { (code, label) ->
                    Button(
                        onClick = {
                            log.setReason(event.id, code)
                            answered.value = true
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JannahColors.DaySurface,
                            contentColor = JannahColors.DayText
                        )
                    ) { Text(label, style = JannahType.secondary) }
                }

                Spacer(Modifier.height(JannahSpacing.l))
                TextButton(onClick = {
                    log.setReason(event.id, "skipped")
                    answered.value = true
                }) {
                    Text("Skip", style = JannahType.tertiary, color = JannahColors.DayMuted)
                }
            } else {
                Text("Recorded.", style = JannahType.screenTitle, color = JannahColors.DayText)
                Spacer(Modifier.height(JannahSpacing.m))
                Text("Tonight's plan adjusts.",
                    style = JannahType.secondary, color = JannahColors.DayMuted)
                Spacer(Modifier.height(JannahSpacing.xl))
                Button(
                    onClick = { showDashboard.value = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JannahColors.Accent,
                        contentColor = JannahColors.AccentInk
                    )
                ) { Text("Continue", style = JannahType.button) }
            }
        }
    }

    // ---------- Dashboard ----------

    @Composable
    private fun Dashboard() {
        val refresh = remember { mutableStateOf(0) }
        // Reading resumeTick ensures we recompose every time MainActivity resumes.
        val tick = resumeTick.value
        val ignore = refresh.value + tick

        val log = WakeLog(this)
        val history = log.lastNights(7)
        val common = log.commonReason()
        val prayed = log.prayedCount()

        val next = TahajjudScheduler.computeTonight(lat, lon)
        val scheduled = TahajjudScheduler.nextScheduled(this)
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val fullFmt = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault())
        val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())

        val insight = FrictionMap.topInsight(this)
        val misses = FrictionMap.missesLast7Days(this)
        val prayed7 = FrictionMap.prayedLast7Days(this)

        val level = TahajjudPlan.level(this)
        val (prayedWeek, target) = TahajjudPlan.weekProgress(this)
        val onTonight = TahajjudPlan.isOnTonight(this)

        val prayerTimes = SalahScheduler.todayTimes(lat, lon)
        val prayersDone = SalahTracker.today(this).count { it.value }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            // Title
            Text("Tonight", style = JannahType.screenTitle, color = JannahColors.DayText)

            Spacer(Modifier.height(JannahSpacing.l))

            // Hero: tonight's tahajjud time
            Text("Last third begins", style = JannahType.section, color = JannahColors.DayMuted)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(fullFmt.format(next),
                color = JannahColors.DayText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Light)

            Spacer(Modifier.height(JannahSpacing.l))

            // Primary action
            Button(
                onClick = {
                    TahajjudScheduler.schedule(this@MainActivity, next)
                    refresh.value = refresh.value + 1
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) { Text("Set tahajjud alarm", style = JannahType.button) }

            Spacer(Modifier.height(JannahSpacing.s))

            Text(
                if (scheduled > 0L) "Alarm set · " + fullFmt.format(Date(scheduled)) else "No alarm set",
                style = JannahType.tertiary,
                color = JannahColors.DayMuted,
                textAlign = TextAlign.Center
            )

            // ---- Formation ----
            SectionDivider()

            SectionLabel("Formation")
            Spacer(Modifier.height(JannahSpacing.s))
            Text(
                when (level) {
                    1 -> "Level 1 · One night a week"
                    2 -> "Level 2 · Two nights a week"
                    3 -> "Level 3 · Three nights a week"
                    else -> "Level 4 · Every night"
                },
                style = JannahType.primary,
                color = JannahColors.DayText
            )
            Spacer(Modifier.height(JannahSpacing.xs))
            Text(
                (if (onTonight) "Scheduled tonight. " else "Rest tonight. ") +
                    "$prayedWeek of $target this week",
                style = JannahType.secondary, color = JannahColors.DayMuted
            )

            val readyToLevelUp = when (level) {
                1 -> prayedWeek >= 1
                2 -> prayedWeek >= 2
                3 -> prayedWeek >= 3
                else -> false
            }
            if (readyToLevelUp) {
                Spacer(Modifier.height(JannahSpacing.m))
                Button(
                    onClick = {
                        val nextL = when (level) { 1 -> 2; 2 -> 3; 3 -> 7; else -> 7 }
                        TahajjudPlan.setLevel(this@MainActivity, nextL)
                        refresh.value = refresh.value + 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JannahColors.DaySurface,
                        contentColor = JannahColors.Accent
                    )
                ) { Text("Level up next week", style = JannahType.secondary) }
            }

            // ---- Pattern ----
            if (insight != null || misses > 0 || prayed7 > 0) {
                Spacer(Modifier.height(JannahSpacing.xl))
                SectionDivider()
                Spacer(Modifier.height(JannahSpacing.l))

                SectionLabel("Pattern")
                Spacer(Modifier.height(JannahSpacing.s))

                if (prayed7 > 0) {
                    Text("$prayed7 of last 7 nights prayed",
                        style = JannahType.primary, color = JannahColors.DayText)
                    Spacer(Modifier.height(JannahSpacing.xs))
                }
                if (insight != null) {
                    Text("Common block: ${insight.label}",
                        style = JannahType.secondary, color = JannahColors.DayMuted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(insight.suggestion,
                        style = JannahType.secondary, color = JannahColors.Accent,
                        lineHeight = 20.sp)
                } else if (common != null) {
                    Text("Last miss: ${readableReason(common)}",
                        style = JannahType.secondary, color = JannahColors.DayMuted)
                }
            }

            // ---- Today's prayers ----
            SectionDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's prayers", style = JannahType.section, color = JannahColors.DayMuted)
                TextButton(onClick = {
                    startActivity(Intent(this@MainActivity, SalahActivity::class.java))
                }) {
                    Text("$prayersDone of 5 →", style = JannahType.tertiary, color = JannahColors.Accent)
                }
            }

            Spacer(Modifier.height(JannahSpacing.s))

            prayerTimes.forEach { (key, t) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(SalahTracker.label(key), style = JannahType.secondary, color = JannahColors.DayText)
                    Text(timeFmt.format(t), style = JannahType.secondary, color = JannahColors.DayMuted)
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            var reminderMsg by remember { mutableStateOf<String?>(null) }
            Button(
                onClick = {
                    if (!SalahScheduler.canUseExactAlarms(this@MainActivity)) {
                        // Send the user to system settings to allow exact alarms.
                        val intent = Intent(
                            android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            android.net.Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                        reminderMsg = "Allow 'Alarms & reminders' for accurate times."
                    } else {
                        SalahScheduler.schedule(this@MainActivity, lat, lon)
                        reminderMsg = "Prayer reminders set for today."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.DaySurface,
                    contentColor = JannahColors.DayText
                )
            ) { Text("Set prayer reminders", style = JannahType.secondary) }

            if (reminderMsg != null) {
                Spacer(Modifier.height(JannahSpacing.s))
                Text(reminderMsg!!, style = JannahType.tertiary,
                    color = JannahColors.Accent, textAlign = TextAlign.Center)
            }

            // ---- Recent nights ----
            if (history.isNotEmpty()) {
                Spacer(Modifier.height(JannahSpacing.xl))
                SectionDivider()
                Spacer(Modifier.height(JannahSpacing.l))

                Text("Last 7 nights", style = JannahType.section, color = JannahColors.DayMuted)
                Spacer(Modifier.height(JannahSpacing.s))

                history.forEach { e ->
                    val day = dayFmt.format(Date(e.ts))
                    val status = when (e.outcome) {
                        "prayed" -> "Prayed"
                        "missed" -> "Missed"
                        else -> e.outcome
                    }
                    val statusColor = if (e.outcome == "prayed") JannahColors.Accent else JannahColors.DayMuted

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(day, style = JannahType.secondary, color = JannahColors.DayMuted)
                        Text(status, style = JannahType.secondary, color = statusColor)
                    }
                }
            }

            // ---- Tools ----
            SectionDivider()

            SectionLabel("Tools")
            Spacer(Modifier.height(JannahSpacing.s))

            ToolRow(
                leftLabel = "Quran",
                onLeft = { startActivity(Intent(this@MainActivity, QuranActivity::class.java)) },
                rightLabel = "Planner",
                onRight = { startActivity(Intent(this@MainActivity, PlannerActivity::class.java)) }
            )
            Spacer(Modifier.height(JannahSpacing.s))
            ToolRow(
                leftLabel = "Istighfar",
                onLeft = { startActivity(Intent(this@MainActivity, IstighfarActivity::class.java)) },
                rightLabel = "Tonight's plan",
                onRight = { startActivity(Intent(this@MainActivity, BedtimeActivity::class.java)) }
            )
            Spacer(Modifier.height(JannahSpacing.s))
            Button(
                onClick = { startActivity(Intent(this@MainActivity, UrgeActivity::class.java)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.DaySurface,
                    contentColor = JannahColors.DayText
                )
            ) { Text("Not now — urge support", style = JannahType.secondary) }

            // ---- Footer ----
            Spacer(Modifier.height(JannahSpacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }) { Text("Settings", style = JannahType.tertiary, color = JannahColors.DayMuted) }
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }

    // ---------- Helpers ----------

    @Composable
    private fun SectionDivider() {
        Spacer(Modifier.height(JannahSpacing.l))
        HorizontalDivider(color = JannahColors.Divider, thickness = 0.5.dp)
        Spacer(Modifier.height(JannahSpacing.l))
    }

    @Composable
    private fun SectionLabel(text: String) {
        Text(
            text,
            style = JannahType.section,
            color = JannahColors.DayMuted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Left
        )
    }

    @Composable
    private fun ToolRow(
        leftLabel: String,
        onLeft: () -> Unit,
        rightLabel: String,
        onRight: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
        ) {
            Button(
                onClick = onLeft,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.DaySurface,
                    contentColor = JannahColors.DayText
                )
            ) { Text(leftLabel, style = JannahType.secondary) }
            Button(
                onClick = onRight,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.DaySurface,
                    contentColor = JannahColors.DayText
                )
            ) { Text(rightLabel, style = JannahType.secondary) }
        }
    }
}
