package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.jannahos.ui.JannahApp
import com.jannahos.ui.OnboardingFlow
import com.jannahos.ui.theme.JannahTheme

class MainActivity : ComponentActivity() {
    private var requestedStartDestination by mutableStateOf("today")

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("open_quran_lock", false)) {
            requestedStartDestination = "quran_lock"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LockState.restore(this)
        if (intent.getBooleanExtra("open_quran_lock", false)) {
            requestedStartDestination = "quran_lock"
        }
        
        // Rebuild enabled schedules when the app is opened again after a force-stop.
        ScheduleRestorer.restore(this)

        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var onboarded by remember { mutableStateOf(Settings.onboardingComplete(this)) }
                    
                    if (onboarded) {
                        key(requestedStartDestination) {
                            JannahApp(startDestination = requestedStartDestination)
                        }
                    } else {
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
