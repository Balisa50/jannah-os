package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.RamadanMode
import com.jannahos.SalahScheduler
import com.jannahos.Settings
import com.jannahos.ui.TopBar
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
    val times = SalahScheduler.todayTimes(Settings.lat(ctx), Settings.lon(ctx))
    val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Ramadan", onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Orange).padding(JannahSpacing.l)) {
                Column {
                    Text("RAMADAN", style = JannahType.eyebrow, color = JannahColors.OnOrange.copy(alpha = 0.75f))
                    Spacer(Modifier.height(4.dp))
                    Text(when {
                        phase.active -> "Day ${phase.dayOfRamadan}"
                        phase.daysUntil > 0 -> "In ${phase.daysUntil} days"
                        else -> "Preparation"
                    }, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.OnOrange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Text("TODAY'S TIMES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.Card)) {
                Column {
                    val fajr = times["fajr"]?.let { hhmm.format(Date(it)) } ?: "—"
                    val maghrib = times["maghrib"]?.let { hhmm.format(Date(it)) } ?: "—"
                    
                    Row(Modifier.fillMaxWidth().padding(JannahSpacing.l), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Suhoor ends (Fajr)", fontFamily = FontFamily.Serif, fontSize = 16.sp, color = JannahColors.Ink)
                        Text(fajr, fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
                    Row(Modifier.fillMaxWidth().padding(JannahSpacing.l), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Iftar (Maghrib)", fontFamily = FontFamily.Serif, fontSize = 16.sp, color = JannahColors.Ink)
                        Text(maghrib, fontFamily = FontFamily.Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("LAY LAT AL-QADR", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Text("The last ten nights. Seek it in the odd nights: 21, 23, 25, 27, 29.", style = JannahType.caption, color = JannahColors.Muted)
            Spacer(Modifier.height(JannahSpacing.s))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(JannahSpacing.cardRadius)).background(JannahColors.OrangeSoft).padding(JannahSpacing.l)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(RamadanMode.laylatulQadrDua.first, style = JannahType.arabic, color = JannahColors.Ink, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(RamadanMode.laylatulQadrDua.second, style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(RamadanMode.laylatulQadrDua.third, style = JannahType.body, color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 22.sp)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
