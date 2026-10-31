package com.jannahos

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jannahos.ui.JannahApp
import com.jannahos.LockState
import com.jannahos.ui.screens.CrashReportScreen
import com.jannahos.ui.OnboardingFlow
import com.jannahos.ui.theme.JannahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        
        LockState.isLockActive = false
        super.onCreate(savedInstanceState)
        // Safely schedule alarms after the UI is ready
        try {
            val lat = Settings.lat(this)
            val lon = Settings.lon(this)
            val times = SalahScheduler.todayTimes(lat, lon)
            val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
            for (key in ordered) {
                val t = times[key] ?: continue
                if (t > System.currentTimeMillis()) {
                    PrayerReminderScheduler.scheduleReminders(this, SalahTracker.label(key), t)
                }
            }
        } catch (_: Throwable) {}
        RamadanAlarmScheduler.scheduleAlarms(this)
        
        // Permissions are requested during onboarding, not here.

        setContent {
            JannahTheme {
                var onboarded by remember { mutableStateOf(Settings.onboardingComplete(this)) }
                val crash = remember { App.latestCrash(application) }
                var showCrash by remember { mutableStateOf(crash != null) }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val lockActive = remember {
                        try { QuranReadingLock.shouldLock(this) } catch (t: Throwable) { false }
                    }
                    var showLock by remember { mutableStateOf(lockActive) }
                    val intentWantsLock = intent?.getBooleanExtra("show_quran_lock", false) == true
                    if (intentWantsLock) showLock = true

                    when {
                        showCrash && crash != null -> {
                            CrashReportScreen(trace = crash, onDismiss = { showCrash = false })
                        }
                        onboarded && showLock -> {
                            com.jannahos.ui.screens.QuranLockScreen(onRelease = { showLock = false })
                        }
                        onboarded -> {
                            JannahApp()
                        }
                        else -> {
                            OnboardingFlow(onDone = {
                                Settings.setOnboardingComplete(this)
                                onboarded = true
                            })
                        }
                    }
                }
            }
        }
    }
}
