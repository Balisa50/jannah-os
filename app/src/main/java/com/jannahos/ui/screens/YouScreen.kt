package com.jannahos.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jannahos.BedtimeActivity
import com.jannahos.PlannerActivity
import com.jannahos.SettingsActivity
import com.jannahos.TahajjudPlan
import com.jannahos.UrgeActivity
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun YouScreen() {
    val ctx = LocalContext.current
    val refresh = remember { mutableStateOf(0) }
    val ignore = refresh.value

    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.m),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("You", style = JannahType.screenTitle, color = JannahColors.DayText)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text("Your formation and settings.", style = JannahType.secondary, color = JannahColors.DayMuted)

        Spacer(Modifier.height(JannahSpacing.m))

        // Formation card with level chooser
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(JannahColors.DaySurface)
        ) {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("Formation", style = JannahType.section, color = JannahColors.DayMuted)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    when (level) {
                        1 -> "Level 1 · One night a week"
                        2 -> "Level 2 · Two nights a week"
                        3 -> "Level 3 · Three nights a week"
                        else -> "Level 4 · Every night"
                    },
                    style = JannahType.primary, color = JannahColors.DayText
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text("$prayedWeek of $target this week",
                    style = JannahType.secondary, color = JannahColors.DayMuted)

                Spacer(Modifier.height(JannahSpacing.m))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)
                ) {
                    listOf(1, 2, 3, 7).forEach { lvl ->
                        Button(
                            onClick = {
                                TahajjudPlan.setLevel(ctx, lvl)
                                refresh.value = refresh.value + 1
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (level == lvl) JannahColors.Accent else JannahColors.DayBg,
                                contentColor = if (level == lvl) JannahColors.AccentInk else JannahColors.DayText
                            )
                        ) { Text("$lvl", style = JannahType.secondary) }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.xs))
                Text("Nights per week", style = JannahType.tertiary, color = JannahColors.DayMuted)
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        // Tools card — list items, not buttons
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(JannahColors.DaySurface)
        ) {
            Column(Modifier.padding(vertical = JannahSpacing.s)) {
                Text("Tools", style = JannahType.section, color = JannahColors.DayMuted,
                    modifier = Modifier.padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.xs))
                ListRow("Tonight's plan") { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
                ListRow("Planner") { ctx.startActivity(Intent(ctx, PlannerActivity::class.java)) }
                ListRow("Urge support") { ctx.startActivity(Intent(ctx, UrgeActivity::class.java)) }
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))

        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(JannahColors.DaySurface)
        ) {
            Column(Modifier.padding(vertical = JannahSpacing.s)) {
                Text("Settings", style = JannahType.section, color = JannahColors.DayMuted,
                    modifier = Modifier.padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.xs))
                ListRow("Location & calculation") { ctx.startActivity(Intent(ctx, SettingsActivity::class.java)) }
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))
    }
}

@Composable
private fun ListRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = JannahSpacing.m, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = JannahType.primary, color = JannahColors.DayText,
            modifier = Modifier.weight(1f))
        Text("→", style = JannahType.primary, color = JannahColors.DayMuted)
    }
}
