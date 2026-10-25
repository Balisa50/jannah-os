package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.LaylatulQadrStore
import com.jannahos.RamadanMode
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun RamadanScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val ctx = LocalContext.current
    val phase = RamadanMode.current()
    var tick by remember { mutableIntStateOf(0) }
    val ignore = tick

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(JannahColors.Orange)
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.xl, bottom = JannahSpacing.l)
        ) {
            Column {
                Text("RAMADAN",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(when {
                    phase.active -> "Day ${phase.dayOfRamadan} of Ramadan."
                    phase.daysUntil > 0 -> "In ${phase.daysUntil} days."
                    else -> "Preparing for Ramadan."
                },
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.OnOrange,
                    lineHeight = 32.sp
                )
                Spacer(Modifier.height(4.dp))
                Text("The month of the Quran. The month of patience.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("SUHOOR", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card).padding(JannahSpacing.l)
            ) {
                Text(RamadanMode.dayGuidance.suhoorNote, style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 20.sp)
            }

            Spacer(Modifier.height(JannahSpacing.m))

            Text("IFTAR DUA", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card).padding(JannahSpacing.l)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(RamadanMode.dayGuidance.iftarDuaArabic, style = JannahType.arabic, color = JannahColors.Ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(RamadanMode.dayGuidance.iftarDuaTranslation, style = JannahType.bodySmall, color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(RamadanMode.dayGuidance.iftarSource, style = JannahType.micro, color = JannahColors.Faint)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // ─── LAYLATUL QADR TRACKER ───
            Text("THE LAST TEN NIGHTS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.xs))
            Text("Tap a night when you stand in prayer. Progress saves automatically.",
                style = JannahType.caption, color = JannahColors.Muted, lineHeight = 18.sp)
            Spacer(Modifier.height(JannahSpacing.s))

            RamadanMode.lastTenFocus.forEachIndexed { i, nf ->
                val prayed = LaylatulQadrStore.isPrayed(ctx, nf.night)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressClickable {
                            LaylatulQadrStore.setPrayed(ctx, nf.night, !prayed)
                            tick++
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(28.dp).clip(CircleShape)
                            .background(if (prayed) JannahColors.Orange else JannahColors.Card)
                            .pressClickable {
                                LaylatulQadrStore.setPrayed(ctx, nf.night, !prayed)
                                tick++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (prayed) Text("✓", color = JannahColors.OnOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.padding(horizontal = 6.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Night ${nf.night}",
                            style = JannahType.bodySmall,
                            color = JannahColors.Ink,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text(nf.note, style = JannahType.caption, color = JannahColors.Body, lineHeight = 18.sp)
                    }
                }
                if (i < RamadanMode.lastTenFocus.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))
            val prayedCount = LaylatulQadrStore.prayedCount(ctx)
            Text("$prayedCount of 10 nights marked.", style = JannahType.caption, color = JannahColors.Muted)

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft).padding(JannahSpacing.l)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("THE DUA FOR LAYLAT AL-QADR", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text(RamadanMode.laylatulQadrDua.first, style = JannahType.arabic, color = JannahColors.Ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(RamadanMode.laylatulQadrDua.second, style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(RamadanMode.laylatulQadrDua.third, style = JannahType.body, color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(RamadanMode.laylatulQadrSource, style = JannahType.micro, color = JannahColors.Faint, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card).padding(JannahSpacing.l)
            ) {
                Column {
                    Text("EVIDENCE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("Aisha said: \"When the last ten nights began, the Prophet ﷺ would stay awake at night, wake his family, and exert himself in worship.\"",
                        style = JannahType.bodySmall, color = JannahColors.Body, lineHeight = 20.sp)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("Sahih al-Bukhari 2024, Sahih Muslim 1174", style = JannahType.micro, color = JannahColors.Faint)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier.clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text("← Back", style = JannahType.button, color = JannahColors.OnOrange)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
