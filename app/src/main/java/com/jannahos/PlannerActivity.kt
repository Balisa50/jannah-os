package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jannahos.ui.SwipeBackScreen
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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

class PlannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SwipeBackScreen(onBack = { finish() }) { PlannerScreen() }
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            Text("Planner", style = JannahType.screenTitle, color = JannahColors.DayText)

            Spacer(Modifier.height(JannahSpacing.s))
            Text("$doneCount done today · ${tasks.size} open",
                style = JannahType.secondary, color = JannahColors.DayMuted)

            Spacer(Modifier.height(JannahSpacing.l))

            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("What needs doing?", style = JannahType.tertiary) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(JannahSpacing.m))

            Text("Anchor to", style = JannahType.section, color = JannahColors.DayMuted)
            Spacer(Modifier.height(JannahSpacing.s))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(JannahSpacing.xs)
            ) {
                PlannerStore.ANCHORS.forEach { a ->
                    Button(
                        onClick = { anchor = a },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (anchor == a) JannahColors.Orange else JannahColors.DaySurface,
                            contentColor = if (anchor == a) JannahColors.OnOrange else JannahColors.DayText
                        )
                    ) {
                        Text(a, style = JannahType.tertiary)
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            Button(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        store.add(newTitle.trim(), anchor)
                        newTitle = ""
                        refresh = refresh + 1
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Orange,
                    contentColor = JannahColors.OnOrange
                )
            ) {
                Text("Add", style = JannahType.button)
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            if (tasks.isEmpty()) {
                Text("Nothing open.", style = JannahType.secondary, color = JannahColors.DayMuted)
            } else {
                tasks.forEach { t ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = JannahSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.72f)) {
                            Text(t.title, style = JannahType.primary, color = JannahColors.DayText)
                            if (t.anchor != null) {
                                Text(t.anchor, style = JannahType.tertiary, color = JannahColors.DayMuted)
                            }
                        }
                        Row {
                            Button(
                                onClick = {
                                    store.toggle(t.id)
                                    refresh = refresh + 1
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JannahColors.DaySurface,
                                    contentColor = JannahColors.DayText
                                )
                            ) { Text("Done", style = JannahType.tertiary) }
                            TextButton(onClick = {
                                store.delete(t.id)
                                refresh = refresh + 1
                            }) { Text("×", style = JannahType.primary, color = JannahColors.DayMuted) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Text(
                "Anchor your day around prayer, not the other way.",
                style = JannahType.tertiary,
                color = JannahColors.DayMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}
