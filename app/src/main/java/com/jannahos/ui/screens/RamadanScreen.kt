package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.RamadanMode
import com.jannahos.RamadanTrackerStore
import com.jannahos.SalahScheduler
import com.jannahos.Settings
import com.jannahos.ui.ScreenHeader
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RamadanScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val phase = RamadanMode.current()
    val times = SalahScheduler.todayTimes(ctx, Settings.lat(ctx), Settings.lon(ctx))
    val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    var tick by remember { mutableIntStateOf(0) }
    val ignore = tick

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        ScreenHeader(
            title = "Ramadan",
            eyebrow = "THE MONTH",
            subtitle = when {
                phase.active -> "Day ${phase.dayOfRamadan}"
                phase.daysUntil > 0 -> "In ${phase.daysUntil} days"
                else -> "Preparation"
            }, 
            onBack = onBack
        )

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Text("TODAY'S TIMES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            val fajr = times["fajr"]?.let { hhmm.format(Date(it)) } ?: "—"
            val maghrib = times["maghrib"]?.let { hhmm.format(Date(it)) } ?: "—"
            
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Suhoor ends", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.Ink)
                Text(fajr, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Iftar begins", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.Ink)
                Text(maghrib, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("LAY LAT AL-QADR", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text("The best of nights. Seek it in the odd nights of the last ten.", style = JannahType.caption, color = JannahColors.Muted)
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(21, 23, 25, 27, 29).forEach { night ->
                    val isPrayed = RamadanTrackerStore.isNightPrayed(ctx, night)
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPrayed) JannahColors.OrangeSoft else JannahColors.Card)
                            .pressClickable { 
                                RamadanTrackerStore.setNightPrayed(ctx, night, !isPrayed)
                                tick++ 
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape)
                                .background(if (isPrayed) JannahColors.Orange else Color.Transparent)
                                .border(1.dp, if (isPrayed) JannahColors.Orange else JannahColors.Border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPrayed) Text("✓", color = JannahColors.OnOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("Night $night", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                        Spacer(Modifier.weight(1f))
                        if (isPrayed) Text("Qiyam established", style = JannahType.micro, color = JannahColors.OrangeDeep)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("MAKE-UP FASTS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text("Fasts you missed (travel, illness, etc.)", style = JannahType.caption, color = JannahColors.Muted)
            Spacer(Modifier.height(12.dp))

            val missed = RamadanTrackerStore.getMissedFasts(ctx)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.Card).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("$missed days", fontFamily = FontFamily.Serif, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(JannahColors.OrangeSoft).pressClickable { 
                        RamadanTrackerStore.removeMissedFast(ctx)
                        tick++
                    }, contentAlignment = Alignment.Center) { Text("−", fontSize = 24.sp, color = JannahColors.OrangeDeep) }
                    
                    Box(Modifier.size(40.dp).clip(CircleShape).background(JannahColors.Orange).pressClickable { 
                        RamadanTrackerStore.addMissedFast(ctx)
                        tick++
                    }, contentAlignment = Alignment.Center) { Text("+", fontSize = 24.sp, color = JannahColors.OnOrange) }
                }
            }

            Spacer(Modifier.height(32.dp))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("THE DUA FOR LAYLAT AL-QADR", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(16.dp))
                    Text(RamadanMode.laylatulQadrDua.first, style = JannahType.arabic, color = JannahColors.Ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(RamadanMode.laylatulQadrDua.second, style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(RamadanMode.laylatulQadrDua.third, style = JannahType.body, color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 22.sp)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
