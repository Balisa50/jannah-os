package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.jannahos.ui.JannahApp
import com.jannahos.ui.OnboardingFlow
import com.jannahos.ui.theme.JannahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LockState.isLockActive = false
        super.onCreate(savedInstanceState)
        
        // Schedule alarms every time the app opens
        PrayerReminderScheduler.scheduleAllForToday(this)

        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var onboarded by remember { mutableStateOf(Settings.onboardingComplete(this)) }
                    
                    if (onboarded) {
                        JannahApp()
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
