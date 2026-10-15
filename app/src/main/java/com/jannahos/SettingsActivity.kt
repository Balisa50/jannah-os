package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SettingsScreen()
                }
            }
        }
    }

    @Composable
    private fun SettingsScreen() {
        var lat by remember { mutableStateOf(Settings.lat(this).toString()) }
        var lon by remember { mutableStateOf(Settings.lon(this).toString()) }
        var method by remember { mutableStateOf(Settings.method(this)) }
        var saved by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            Text("Settings", style = JannahType.screenTitle, color = JannahColors.DayText)

            Spacer(Modifier.height(JannahSpacing.xl))

            Text(
                "Location",
                style = JannahType.section,
                color = JannahColors.DayMuted,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(JannahSpacing.s))
            OutlinedTextField(
                value = lat,
                onValueChange = { lat = it },
                label = { Text("Latitude", style = JannahType.tertiary) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(JannahSpacing.s))
            OutlinedTextField(
                value = lon,
                onValueChange = { lon = it },
                label = { Text("Longitude", style = JannahType.tertiary) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(JannahSpacing.xl))

            Text(
                "Calculation method",
                style = JannahType.section,
                color = JannahColors.DayMuted,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(JannahSpacing.s))

            Settings.METHODS.forEach { (code, label) ->
                Button(
                    onClick = { method = code },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (method == code) JannahColors.Accent else JannahColors.DaySurface,
                        contentColor = if (method == code) JannahColors.AccentInk else JannahColors.DayText
                    )
                ) {
                    Text(
                        label,
                        style = JannahType.secondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Button(
                onClick = {
                    val latD = lat.toDoubleOrNull() ?: return@Button
                    val lonD = lon.toDoubleOrNull() ?: return@Button
                    Settings.set(this@SettingsActivity, latD, lonD, method)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) {
                Text("Save", style = JannahType.button)
            }

            if (saved) {
                Spacer(Modifier.height(JannahSpacing.s))
                Text("Saved.", style = JannahType.secondary, color = JannahColors.Accent)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
