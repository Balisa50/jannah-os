package com.jannahos.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.jannahos.CityStore
import com.jannahos.PrayerApi
import com.jannahos.QiblaActivity
import com.jannahos.SalahScheduler
import com.jannahos.SalahTracker
import com.jannahos.Settings
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrayScreen() {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    var reminderMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val useCity = remember { CityStore.useCity(ctx) }
    val cityName = remember { CityStore.name(ctx) }
    val countryName = remember { CityStore.country(ctx) }

    // Real API times (Aladhan), computed once per day. Falls back to the
    // offline Adhan calculation if the network is unavailable.
    val apiTimes = remember(todayKey, useCity) {
        if (!useCity || cityName.isBlank()) null
        else try { PrayerApi.fetchByCity(ctx, cityName, countryName, 3) } catch (e: Exception) { null }
    }

    val times: Map<String, Date?> = remember(todayKey, useCity, apiTimes) {
        if (apiTimes != null) {
            mapOf(
                "fajr" to Date(PrayerApi.timeToMillis(apiTimes.fajr)),
                "dhuhr" to Date(PrayerApi.timeToMillis(apiTimes.dhuhr)),
                "asr" to Date(PrayerApi.timeToMillis(apiTimes.asr)),
                "maghrib" to Date(PrayerApi.timeToMillis(apiTimes.maghrib)),
                "isha" to Date(PrayerApi.timeToMillis(apiTimes.isha))
            )
        } else {
            SalahScheduler.todayTimes(lat, lon)
        }
    }

    val hhmm = SimpleDateFormat("HH:mm", Locale.getDefault())
    val ordered = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")

    val now = System.currentTimeMillis()
    val nextKey = ordered.firstOrNull { (times[it]?.time ?: 0L) > now }
    val nextTime = nextKey?.let { times[it] }?.time

    val diffMs = (nextTime ?: 0L) - now
    val countdown = if (nextTime != null && diffMs > 0) {
        String.format(Locale.US, "%02d:%02d:%02d",
            diffMs / 3600000, (diffMs / 60000) % 60, (diffMs / 1000) % 60)
    } else "--:--:--"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = JannahSpacing.gutter)
                .padding(top = JannahSpacing.l, bottom = JannahSpacing.m)
        ) {
            Column {
                Text("Prayer",
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.Ink)
                Spacer(Modifier.height(2.dp))
                Text(
                    if (useCity && cityName.isNotBlank()) "Today in $cityName"
                    else "Today for your location",
                    style = JannahType.caption, color = JannahColors.Muted)
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {

            if (nextKey != null && nextTime != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Orange)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("NEXT",
                            style = JannahType.eyebrow,
                            color = JannahColors.OnOrange.copy(alpha = 0.75f))
                        Spacer(Modifier.height(4.dp))
                        Text(SalahTracker.label(nextKey),
                            fontFamily = FontFamily.Serif,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.OnOrange)
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(countdown,
                                fontFamily = FontFamily.Serif,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.OnOrange)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("· " + hhmm.format(nextTime),
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 16.sp,
                                color = JannahColors.OnOrange.copy(alpha = 0.85f),
                                modifier = Modifier.padding(bottom = 3.dp))
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.m))
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
            ) {
                Column {
                    ordered.forEachIndexed { i, key ->
                        val t = times[key]
                        val isNext = key == nextKey
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = JannahSpacing.l, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(SalahTracker.label(key),
                                fontFamily = FontFamily.Serif,
                                fontSize = 18.sp,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNext) JannahColors.Orange else JannahColors.Ink,
                                modifier = Modifier.weight(1f))
                            Text(t?.let { hhmm.format(it) } ?: "—",
                                style = JannahType.body,
                                color = if (isNext) JannahColors.Orange else JannahColors.Muted,
                                fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Normal)
                        }
                        if (i < ordered.size - 1) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                        }
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .clickable {
                        ctx.startActivity(Intent(ctx, QiblaActivity::class.java))
                    }
                    .padding(JannahSpacing.l)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Qibla direction",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(2.dp))
                        Text("Tap to open the compass",
                            style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Text("→", style = JannahType.title, color = JannahColors.Orange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.m))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(JannahColors.Ink)
                    .clickable {
                        if (!SalahScheduler.canUseExactAlarms(ctx)) {
                            reminderMsg = "Enable Alarms & reminders in system settings."
                        } else {
                            SalahScheduler.schedule(ctx, lat, lon)
                            reminderMsg = "Reminders set for today."
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Set prayer reminders",
                    style = JannahType.button, color = JannahColors.OnOrange)
            }

            if (reminderMsg != null) {
                Spacer(Modifier.height(JannahSpacing.s))
                Text(reminderMsg!!,
                    style = JannahType.caption, color = JannahColors.Orange,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}
