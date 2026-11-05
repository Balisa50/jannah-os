package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.jannahos.ui.ErrorBoundary
import com.jannahos.ui.JannahApp
import com.jannahos.ui.OnboardingFlow
import com.jannahos.ui.theme.JannahTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LockState.isLockActive = false
        super.onCreate(savedInstanceState)

        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // If location is 0.0, they skipped onboarding. Force them back in.
                    var onboarded by remember { 
                        mutableStateOf(
                            Settings.onboardingComplete(this) && Settings.lat(this) != 0.0
                        ) 
                    }
                    
                    if (onboarded) {
                        ErrorBoundary("JannahApp") {
                            JannahApp()
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
