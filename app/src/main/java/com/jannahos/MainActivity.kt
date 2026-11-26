package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.jannahos.ui.JannahApp
import com.jannahos.ui.OnboardingFlow
import com.jannahos.ui.screens.CrashReportScreen
import com.jannahos.ui.theme.JannahTheme

class MainActivity : ComponentActivity() {
    private var requestedStartDestination by mutableStateOf("siraj")

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("open_quran_lock", false)) {
            requestedStartDestination = "quran_lock"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LockState.restore(this)
        if (intent.getBooleanExtra("open_quran_lock", false)) {
            requestedStartDestination = "quran_lock"
        }
        
        // Rebuild enabled schedules when the app is opened again after a force-stop.
        ScheduleRestorer.restore(this)

        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .imePadding()
                    ) {
                        var onboarded by remember { mutableStateOf(Settings.onboardingComplete(this@MainActivity)) }
                        var pendingCrash by remember { mutableStateOf(CrashLogger.latest(this@MainActivity)) }

                        if (pendingCrash != null) {
                            CrashReportScreen(trace = pendingCrash!!, onDismiss = {
                                CrashLogger.clearAll(this@MainActivity)
                                pendingCrash = null
                            })
                        } else if (onboarded) {
                            key(requestedStartDestination) {
                                JannahApp(startDestination = requestedStartDestination)
                            }
                        } else {
                            OnboardingFlow(onDone = {
                                Settings.setOnboardingComplete(this@MainActivity)
                                onboarded = true
                            })
                        }
                    }
                }
            }
        }
    }
}
