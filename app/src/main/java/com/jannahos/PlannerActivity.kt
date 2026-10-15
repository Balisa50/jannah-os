package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TextButton
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

class PlannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = Colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = NightBg) {
                    PlannerScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun PlannerScreen() {
        var refresh by remember { mutableStateOf(0) }
        val ignore = refresh
        val store = remember { PlannerStore(this@PlannerActivity) }
        var newTitle by remember { mutableStateOf("") }
        var anchor by remember { mutableStateOf(PlannerStore.ANCHORS[6]) }

        val tasks = store.openTasks()
        val doneCount = store.doneToday()

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Text("Planner", color = NightText, fontSize = 26.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)

            Spacer(Modifier.height(10.dp))
            Text("$doneCount done today · ${tasks.size} open", color = NightMuted, fontSize = 12.sp)

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("What needs doing?", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text("Anchor to", color = NightMuted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))

            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PlannerStore.ANCHORS.forEach { a ->
                    Button(
                        onClick = { anchor = a },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (anchor == a) GoldAccent else NightSurface,
                            contentColor = if (anchor == a) NightBg else NightText
                        )
                    ) {
                        Text(a, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        store.add(newTitle.trim(), anchor)
                        newTitle = ""
                        refresh = refresh + 1
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
            ) {
                Text("Add", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(32.dp))

            if (tasks.isEmpty()) {
                Text("Nothing open.", color = NightMuted, fontSize = 13.sp)
            } else {
                tasks.forEach { t ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                            Text(t.title, color = NightText, fontSize = 14.sp)
                            if (t.anchor != null) {
                                Text(t.anchor, color = NightMuted, fontSize = 11.sp)
                            }
                        }
                        Row {
                            Button(
                                onClick = {
                                    store.toggle(t.id)
                                    refresh = refresh + 1
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NightSurface, contentColor = NightText)
                            ) { Text("Done", fontSize = 12.sp) }
                            Spacer(Modifier.height(0.dp))
                            TextButton(onClick = {
                                store.delete(t.id)
                                refresh = refresh + 1
                            }) { Text("×", color = NightMuted, fontSize = 18.sp) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Anchor your day around prayer, not the other way.",
                color = NightMuted, fontSize = 12.sp, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}
