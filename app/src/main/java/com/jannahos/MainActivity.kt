package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
                    var onboarded by remember { mutableStateOf(Settings.onboardingComplete(this)) }
                    var crashLog by remember { mutableStateOf<String?>(null) }
                    
                    if (crashLog != null) {
                        Text(
                            "CRASH DETECTED:\n\n$crashLog", 
                            color = Color.Red, 
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        try {
                            if (onboarded) {
                                JannahApp()
                            } else {
                                OnboardingFlow(onDone = {
                                    Settings.setOnboardingComplete(this)
                                    onboarded = true
                                })
                            }
                        } catch (e: Throwable) {
                            crashLog = e.stackTraceToString()
                        }
                    }
                }
            }
        }
    }
}
