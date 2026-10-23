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
import com.jannahos.ui.screens.CrashReportScreen
import com.jannahos.ui.OnboardingFlow
import com.jannahos.ui.theme.JannahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.app.AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
        super.onCreate(savedInstanceState)

        setContent {
            JannahTheme {
                var showSplash by remember { mutableStateOf(true) }
                if (showSplash) {
                    com.jannahos.ui.JannahSplashScreen { showSplash = false }
                    return@JannahTheme
                }
                
                var onboarded by remember { mutableStateOf(Settings.onboardingComplete(this)) }
                
                // Request permissions only AFTER splash vanishes
                androidx.compose.runtime.LaunchedEffect(showSplash) {
                    if (!showSplash && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissions(
                            arrayOf(
                                android.Manifest.permission.POST_NOTIFICATIONS,
                                android.Manifest.permission.RECORD_AUDIO
                            ), 100
                        )
                    }
                }
                val crash = remember { App.latestCrash(application) }
                var showCrash by remember { mutableStateOf(crash != null) }
                var setupDone by remember { mutableStateOf(Settings.setupComplete(this)) }

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
                        onboarded && !setupDone -> {
                            com.jannahos.ui.screens.OfflineSetupScreen(
                                onReady = {
                                    Settings.setSetupComplete(this)
                                    setupDone = true
                                }
                            )
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
