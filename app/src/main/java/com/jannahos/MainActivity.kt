package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jannahos.ui.ErrorBoundary
import com.jannahos.ui.screens.TodayScreen
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahColors
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // GLOBAL CRASH LOGGER: Saves the exact error to a file
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = File(getExternalFilesDir(null), "crash_log.txt")
                file.writeText(throwable.stackTraceToString())
            } catch (e: Exception) {}
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = JannahColors.Cream) {
                    // Only test the Dashboard directly
                    ErrorBoundary("Dashboard") {
                        TodayScreen(onNavigate = {})
                    }
                }
            }
        }
    }
}
