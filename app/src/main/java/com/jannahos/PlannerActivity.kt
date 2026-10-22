package com.jannahos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.theme.JannahTheme

class PlannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                SwipeBackScreen(onBack = { finish() }) { PlannerScreen() }
            }
        }
    }

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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)
        ) {
            TopBar("Planner", onBack = { finish() })

            Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
                Spacer(Modifier.height(JannahSpacing.m))
                
                Text("Your day, anchored to prayer.", style = JannahType.body, color = JannahColors.Muted)
                Spacer(Modifier.height(JannahSpacing.m))
                Text("$doneCount done today · ${tasks.size} open", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                
                Spacer(Modifier.height(JannahSpacing.l))

                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("What needs doing?", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(JannahSpacing.m))

                Text("ANCHOR TO", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                // Horizontal scrolling anchors
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlannerStore.ANCHORS.forEach { a ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (anchor == a) JannahColors.Orange else JannahColors.Card)
                                .pressClickable { anchor = a }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(a, style = JannahType.caption, color = if (anchor == a) JannahColors.OnOrange else JannahColors.Ink, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(JannahSpacing.m))

                Box(
                    Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(50)).background(if (newTitle.isBlank()) JannahColors.Border else JannahColors.Ink).pressClickable(enabled = newTitle.isNotBlank()) {
                        store.add(newTitle.trim(), anchor)
                        newTitle = ""
                        refresh++
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add Task", style = JannahType.button, color = if (newTitle.isBlank()) JannahColors.Faint else JannahColors.OnOrange)
                }

                Spacer(Modifier.height(JannahSpacing.xl))
                
                Text("OPEN TASKS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                if (tasks.isEmpty()) {
                    Text("Nothing open.", style = JannahType.bodySmall, color = JannahColors.Muted)
                } else {
                    tasks.forEach { t ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(t.title, style = JannahType.body, color = JannahColors.Ink)
                                if (t.anchor != null) {
                                    Text(t.anchor, style = JannahType.micro, color = JannahColors.Muted)
                                }
                            }
                            Box(
                                Modifier.padding(8.dp).pressClickable { store.toggle(t.id); refresh++ }
                            ) {
                                Text("", style = JannahType.title, color = JannahColors.Orange)
                            }
                            Box(
                                Modifier.padding(8.dp).pressClickable { store.delete(t.id); refresh++ }
                            ) {
                                Text("×", style = JannahType.body, color = JannahColors.Muted)
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                    }
                }
                Spacer(Modifier.height(JannahSpacing.xxl))
            }
        }
    }
}
