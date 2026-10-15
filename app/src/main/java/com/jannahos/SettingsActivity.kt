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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NightBg = Color(0xFF0E1116)
private val NightSurface = Color(0xFF161A22)
private val NightText = Color(0xFFE8E5DC)
private val NightMuted = Color(0xFF8B8F98)
private val GoldAccent = Color(0xFFC9A961)

private val Colors = darkColorScheme(
    background = NightBg,
    surface = NightSurface,
    onBackground = NightText,
    onSurface = NightText,
    primary = GoldAccent,
    onPrimary = NightBg
)

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = Colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = NightBg) {
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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Text("Settings", color = NightText, fontSize = 26.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)

            Spacer(Modifier.height(36.dp))

            Text("Location", color = NightMuted, fontSize = 12.sp, letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = lat,
                onValueChange = { lat = it },
                label = { Text("Latitude", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = lon,
                onValueChange = { lon = it },
                label = { Text("Longitude", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(36.dp))

            Text("Calculation method", color = NightMuted, fontSize = 12.sp, letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            Settings.METHODS.forEach { (code, label) ->
                Button(
                    onClick = { method = code },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (method == code) GoldAccent else NightSurface,
                        contentColor = if (method == code) NightBg else NightText
                    )
                ) {
                    Text(label, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left)
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val latD = lat.toDoubleOrNull() ?: return@Button
                    val lonD = lon.toDoubleOrNull() ?: return@Button
                    Settings.set(this@SettingsActivity, latD, lonD, method)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
            ) {
                Text("Save", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            if (saved) {
                Spacer(Modifier.height(12.dp))
                Text("Saved.", color = GoldAccent, fontSize = 13.sp)
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
