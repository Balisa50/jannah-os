package com.jannahos.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
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
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
import com.jannahos.ui.theme.JannahBrushes
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import com.jannahos.ui.theme.ScreenScaffold

@Composable
fun OnboardingFlow(onDone: () -> Unit) {
    val ctx = LocalContext.current
    var step by remember { mutableStateOf(0) }

    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("13.4549") }
    var lon by remember { mutableStateOf("-16.5790") }
    var method by remember { mutableStateOf("MWL") }
    var level by remember { mutableStateOf(1) }

    ScreenScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = JannahSpacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            // Step dots
            Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)) {
                repeat(5) { i ->
                    Box(
                        Modifier
                            .size(if (i == step) 24.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) JannahColors.Gold else JannahColors.Faint
                            )
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (step) {
                    0 -> Welcome()
                    1 -> AskName(name, onChange = { name = it })
                    2 -> Location(lat, lon, method,
                        onLat = { lat = it }, onLon = { lon = it }, onMethod = { method = it })
                    3 -> ChooseLevel(level, onPick = { level = it })
                    else -> DoneScreen()
                }
            }

            // Bottom bar with action
            val label = when (step) {
                0 -> "Begin"
                else -> if (step == 4) "Open the app" else "Continue"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.buttonRadius))
                    .background(JannahBrushes.goldButton)
                    .clickable {
                        if (step == 4) {
                            val latD = lat.toDoubleOrNull() ?: 13.4549
                            val lonD = lon.toDoubleOrNull() ?: -16.5790
                            Settings.set(ctx, latD, lonD, method)
                            Settings.setDisplayName(ctx, name)
                            TahajjudPlan.setLevel(ctx, level)
                            onDone()
                        } else {
                            step += 1
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(label, style = JannahType.button, color = JannahColors.GoldInk)
            }

            Spacer(Modifier.height(JannahSpacing.l))
        }
    }
}

@Composable
private fun Welcome() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome.", style = JannahType.title, color = JannahColors.Ivory)
        Spacer(Modifier.height(JannahSpacing.l))
        Text(
            "This is not another prayer-times app. It is a system for building one habit at a time — starting with tahajjud, ending with a closer connection to Allah.",
            style = JannahType.body, color = JannahColors.Body,
            textAlign = TextAlign.Center, lineHeight = 26.sp
        )
        Spacer(Modifier.height(JannahSpacing.l))
        Text(
            "Nothing you write here leaves your phone. No account. No cloud. No ads.",
            style = JannahType.caption, color = JannahColors.Muted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Location(
    lat: String, lon: String, method: String,
    onLat: (String) -> Unit, onLon: (String) -> Unit, onMethod: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Where are you?", style = JannahType.title, color = JannahColors.Ivory)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("Used only for prayer times. Stored on your phone.",
            style = JannahType.caption, color = JannahColors.Muted, textAlign = TextAlign.Center)

        Spacer(Modifier.height(JannahSpacing.l))

        OutlinedTextField(
            value = lat, onValueChange = onLat,
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(JannahSpacing.s))
        OutlinedTextField(
            value = lon, onValueChange = onLon,
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(JannahSpacing.l))

        Text("Calculation method", style = JannahType.eyebrow, color = JannahColors.Muted,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Left)
        Spacer(Modifier.height(JannahSpacing.s))

        Settings.METHODS.forEach { (code, label) ->
            val selected = method == code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) JannahColors.Surface else JannahColors.Deep)
                    .clickable { onMethod(code) }
                    .padding(JannahSpacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    style = JannahType.bodySmall,
                    color = if (selected) JannahColors.Ivory else JannahColors.Muted,
                    modifier = Modifier.weight(1f)
                )
                if (selected) Text("✓", style = JannahType.bodySmall, color = JannahColors.Gold)
            }
            Spacer(Modifier.height(JannahSpacing.xs))
        }

        Spacer(Modifier.height(JannahSpacing.l))
    }
}

@Composable
private fun ChooseLevel(level: Int, onPick: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Start small.", style = JannahType.title, color = JannahColors.Ivory)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("Choose how many nights a week you want to wake for tahajjud. You can change this anytime.",
            style = JannahType.caption, color = JannahColors.Muted, textAlign = TextAlign.Center)

        Spacer(Modifier.height(JannahSpacing.l))

        listOf(
            1 to "One night a week",
            2 to "Two nights a week",
            3 to "Three nights a week",
            7 to "Every night"
        ).forEach { (lvl, label) ->
            val selected = level == lvl
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) JannahColors.Surface else JannahColors.Deep)
                    .clickable { onPick(lvl) }
                    .padding(JannahSpacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    style = JannahType.body,
                    color = if (selected) JannahColors.Ivory else JannahColors.Body,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (selected) Text("✓", style = JannahType.body, color = JannahColors.Gold)
            }
            Spacer(Modifier.height(JannahSpacing.s))
        }
    }
}

@Composable
private fun AskName(current: String, onChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("What should we call you?",
            style = JannahType.title, color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("Just a first name is fine. You can leave it blank.",
            style = JannahType.caption, color = JannahColors.Muted,
            textAlign = TextAlign.Center)

        Spacer(Modifier.height(JannahSpacing.l))

        OutlinedTextField(
            value = current,
            onValueChange = onChange,
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DoneScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("You're set.", style = JannahType.title, color = JannahColors.Ivory)
        Spacer(Modifier.height(JannahSpacing.l))
        Text(
            "Come back tonight. Set the alarm. Sleep with intention.",
            style = JannahType.body, color = JannahColors.Body, textAlign = TextAlign.Center
        )
    }
}
