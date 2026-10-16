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
import com.jannahos.ui.OnboardingFlow
import com.jannahos.ui.theme.JannahTheme

class MainActivity : ComponentActivity() {
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
                var onboarded by remember { mutableStateOf(Settings.onboardingComplete(this)) }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
