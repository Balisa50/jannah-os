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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.BedtimeActivity
import com.jannahos.PlannerActivity
import com.jannahos.SettingsActivity
import com.jannahos.TahajjudPlan
import com.jannahos.UrgeActivity
import com.jannahos.ui.theme.GlassCard
import com.jannahos.ui.theme.JannahBrushes
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun YouScreen() {
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    val level = TahajjudPlan.level(ctx)
    val (prayedWeek, target) = TahajjudPlan.weekProgress(ctx)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = JannahSpacing.gutter),
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        Text("You", style = JannahType.title, color = JannahColors.Ivory)
        Spacer(Modifier.height(JannahSpacing.xs))
        Text("Formation, tools, settings",
            style = JannahType.caption, color = JannahColors.Muted)

        Spacer(Modifier.height(JannahSpacing.l))

        // Formation card — emphasised
        GlassCard {
            Column(Modifier.padding(JannahSpacing.m)) {
                Text("FORMATION", style = JannahType.eyebrow, color = JannahColors.Muted)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(
                    when (level) {
                        1 -> "One night a week"
                        2 -> "Two nights a week"
                        3 -> "Three nights a week"
                        else -> "Every night"
                    },
                    style = JannahType.body, color = JannahColors.Ivory,
                    fontSize = 20.sp, fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(JannahSpacing.xs))
                Text("$prayedWeek of $target this week",
                    style = JannahType.caption, color = JannahColors.Muted)

                Spacer(Modifier.height(JannahSpacing.m))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)
                ) {
                    listOf(1, 2, 3, 7).forEach { lvl ->
                        val selected = level == lvl
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) JannahColors.Gold
                                    else JannahColors.Deep
                                )
                                .clickable {
                                    TahajjudPlan.setLevel(ctx, lvl)
                                    refresh = refresh + 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                lvl.toString(),
                                style = JannahType.body,
                                color = if (selected) JannahColors.GoldInk else JannahColors.Body,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.xs))
                Text("Nights per week",
                    style = JannahType.caption, color = JannahColors.Faint)
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Tools
        Text("TOOLS", style = JannahType.eyebrow, color = JannahColors.Muted,
            modifier = Modifier.padding(horizontal = JannahSpacing.xs))
        Spacer(Modifier.height(JannahSpacing.s))

        GlassCard {
            Column(Modifier.padding(vertical = JannahSpacing.xs)) {
                ListRow("Tonight's plan") { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
                ListRow("Planner")         { ctx.startActivity(Intent(ctx, PlannerActivity::class.java)) }
                ListRow("Urge support")    { ctx.startActivity(Intent(ctx, UrgeActivity::class.java)) }
            }
        }

        Spacer(Modifier.height(JannahSpacing.l))

        Text("SETTINGS", style = JannahType.eyebrow, color = JannahColors.Muted,
            modifier = Modifier.padding(horizontal = JannahSpacing.xs))
        Spacer(Modifier.height(JannahSpacing.s))

        GlassCard {
            Column(Modifier.padding(vertical = JannahSpacing.xs)) {
                ListRow("Location & calculation") {
                    ctx.startActivity(Intent(ctx, SettingsActivity::class.java))
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.xl))
    }
}

@Composable
private fun ListRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = JannahSpacing.m, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = JannahType.body, color = JannahColors.Ivory,
            modifier = Modifier.weight(1f))
        Text("→", style = JannahType.body, color = JannahColors.Muted)
    }
}
