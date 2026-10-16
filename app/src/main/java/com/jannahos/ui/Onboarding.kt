package com.jannahos.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.Settings
import com.jannahos.TahajjudPlan
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

            // Step indicator
            Row(horizontalArrangement = Arrangement.spacedBy(JannahSpacing.xs)) {
                repeat(6) { i ->
                    Box(
                        Modifier
                            .size(if (i == step) 24.dp else 6.dp, 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (i <= step) JannahColors.Orange
                                else JannahColors.Border
                            )
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (step) {
                    0 -> Welcome()
                    1 -> TheFourPillars()
                    2 -> AskName(name) { name = it }
                    3 -> Location(lat, lon, method, { lat = it }, { lon = it }, { method = it })
                    4 -> ChooseLevel(level) { level = it }
                    else -> DoneScreen(name.ifBlank { "friend" })
                }
            }

            val label = when (step) {
                0, 1, 2, 3 -> "Continue"
                else -> "Begin"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        if (step == 5) {
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
                Text(label, style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.l))
        }
    }
}

@Composable
private fun Welcome() {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120)
        visible = true
    }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(900), label = "wa")
    val offset by animateFloatAsState(if (visible) 0f else 24f, tween(900), label = "wo")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .graphicsLayer { this.alpha = alpha; translationY = offset },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Jannah",
            fontFamily = FontFamily.Serif,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp,
            color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("A system for a life lived deliberately.",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 17.sp,
            color = JannahColors.Orange,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.xxl))
        Text(
            "Most Islamic apps give you more to do. This one gives you less, done deeper. It starts with one thing, and grows only when you are ready.",
            style = JannahType.body,
            color = JannahColors.Body,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun TheFourPillars() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("How this works",
            fontFamily = FontFamily.Serif,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("Four things. Nothing else on the surface.",
            style = JannahType.caption, color = JannahColors.Muted)
        Spacer(Modifier.height(JannahSpacing.xl))

        Pillar(
            "1", "Formation",
            "Tahajjud, built as a habit, not a streak. Start with one night a week. The plan adapts when you miss, without guilt."
        )
        Pillar(
            "2", "Witness",
            "The diary reshapes your dashboard for 24 hours based on what you wrote. Milestones are celebrated. Struggles get mercy, not shame."
        )
        Pillar(
            "3", "The Middle Path",
            "Prayer, time, and money share one principle: balance. The app helps you find it, not chase maximums."
        )
        Pillar(
            "4", "Learning",
            "Right Now learns what moves you. It stops sending what you ignore and elevates what works."
        )

        Spacer(Modifier.height(JannahSpacing.l))
        Text(
            "There are more tools, Quran, adhkar, hifdh, duas, fasting. They are there when you need them. They will not compete for your attention.",
            style = JannahType.bodySmall,
            color = JannahColors.Muted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun Pillar(num: String, title: String, body: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = JannahSpacing.m),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(JannahColors.Orange),
            contentAlignment = Alignment.Center
        ) {
            Text(num,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.OnOrange)
        }
        Spacer(Modifier.padding(horizontal = 6.dp))
        Column(Modifier.weight(1f)) {
            Text(title,
                fontFamily = FontFamily.Serif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = JannahColors.Ink)
            Spacer(Modifier.height(4.dp))
            Text(body,
                style = JannahType.bodySmall,
                color = JannahColors.Body,
                lineHeight = 20.sp)
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
            fontFamily = FontFamily.Serif,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("A first name is fine. You can leave it blank.",
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
private fun Location(
    lat: String, lon: String, method: String,
    onLat: (String) -> Unit, onLon: (String) -> Unit, onMethod: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Where are you?",
            fontFamily = FontFamily.Serif,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink)
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

        Text("Calculation method",
            style = JannahType.eyebrow, color = JannahColors.OrangeDeep,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Left)
        Spacer(Modifier.height(JannahSpacing.s))

        Settings.METHODS.forEach { (code, label) ->
            val selected = method == code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) JannahColors.OrangeSoft else JannahColors.Card)
                    .clickable { onMethod(code) }
                    .padding(JannahSpacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    style = JannahType.bodySmall,
                    color = if (selected) JannahColors.OrangeDeep else JannahColors.Body,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (selected) Text("✓", style = JannahType.bodySmall, color = JannahColors.Orange)
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
        Text("Start small.",
            fontFamily = FontFamily.Serif,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink)
        Spacer(Modifier.height(JannahSpacing.s))
        Text("How many nights a week do you want to wake for tahajjud? You can change this anytime.",
            style = JannahType.caption, color = JannahColors.Muted, textAlign = TextAlign.Center)

        Spacer(Modifier.height(JannahSpacing.l))

        listOf(
            1 to "One night a week · start with Thursday",
            2 to "Two nights a week · Monday & Thursday",
            3 to "Three nights a week · Mon, Thu, Sat",
            7 to "Every night"
        ).forEach { (lvl, label) ->
            val selected = level == lvl
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) JannahColors.OrangeSoft else JannahColors.Card)
                    .clickable { onPick(lvl) }
                    .padding(JannahSpacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    style = JannahType.body,
                    color = if (selected) JannahColors.OrangeDeep else JannahColors.Ink,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (selected) Text("✓", style = JannahType.body, color = JannahColors.Orange)
            }
            Spacer(Modifier.height(JannahSpacing.s))
        }
    }
}

@Composable
private fun DoneScreen(name: String) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("You're set, $name.",
            fontFamily = FontFamily.Serif,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp)
        Spacer(Modifier.height(JannahSpacing.m))
        Text("Come back tonight. Set the alarm. Sleep with intention.",
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = 17.sp,
            color = JannahColors.Orange,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp)
        Spacer(Modifier.height(JannahSpacing.l))
        Text("The rest is there when you want it.",
            style = JannahType.bodySmall,
            color = JannahColors.Muted,
            textAlign = TextAlign.Center)
    }
}
