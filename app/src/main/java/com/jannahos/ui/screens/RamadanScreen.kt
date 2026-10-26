package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.RamadanMode
import com.jannahos.SalahScheduler
import com.jannahos.Settings
import com.jannahos.ui.ScreenHeader
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
        ScreenHeader(title = "Ramadan", eyebrow = "MONTH", subtitle = when {
            phase.active -> "Day ${phase.dayOfRamadan}"
            phase.daysUntil > 0 -> "In ${phase.daysUntil} days"
            else -> "Preparation"
        }, onBack = onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            if (phase.active) {
                Text("TODAY'S TIMES", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(8.dp))
                
                val fajr = times["fajr"]?.let { hhmm.format(Date(it)) } ?: "—"
                val maghrib = times["maghrib"]?.let { hhmm.format(Date(it)) } ?: "—"
                
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Suhoor ends (Fajr)", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.Ink)
                    Text(fajr, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Iftar (Maghrib)", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = JannahColors.Ink)
                    Text(maghrib, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.OrangeDeep)
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            } else {
                Text("PREPARATION", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(8.dp))
                Text("Ramadan begins in ${phase.daysUntil} days.", fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JannahColors.Orange)
                Spacer(Modifier.height(8.dp))
                Text("Use this time to prepare your heart, make up missed fasts, and build your Quran reading habit.", style = JannahType.body, color = JannahColors.Muted, lineHeight = 24.sp)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
            }

            Spacer(Modifier.height(JannahSpacing.xl))

            Text("LAY LAT AL-QADR", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text("The last ten nights. Seek it in the odd nights: 21, 23, 25, 27, 29.", style = JannahType.body, color = JannahColors.Muted)
            Spacer(Modifier.height(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Text(RamadanMode.laylatulQadrDua.first, style = JannahType.arabic, color = JannahColors.Ink, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(RamadanMode.laylatulQadrDua.second, style = JannahType.bodySmall, color = JannahColors.Muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(RamadanMode.laylatulQadrDua.third, style = JannahType.body, color = JannahColors.Body, textAlign = TextAlign.Center, lineHeight = 22.sp)
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
