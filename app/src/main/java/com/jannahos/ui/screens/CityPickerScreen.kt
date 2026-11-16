package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.CitySearchApi
import com.jannahos.CityStore
import com.jannahos.PrayerReminderScheduler
import com.jannahos.Settings
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun CityPickerScreen(onBack: () -> Unit, onPicked: (String, String) -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<CitySearchApi.Result>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.length < 2) {
            results = emptyList()
            loading = false
            return@LaunchedEffect
        }
        loading = true
        delay(400)
        results = withContext(Dispatchers.IO) { CitySearchApi.search(query) }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Choose City", onBack = onBack)

        LazyColumn(Modifier.fillMaxSize().weight(1f).padding(horizontal = JannahSpacing.gutter)) {
            item {
                Spacer(Modifier.height(JannahSpacing.s))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search any city worldwide", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(JannahSpacing.m))
            }

            if (loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(JannahSpacing.l), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = JannahColors.Orange)
                    }
                }
            } else if (query.length >= 2 && results.isEmpty()) {
                item {
                    Text("No matches found. Try a different spelling.", style = JannahType.caption, color = JannahColors.Muted)
                }
            } else if (query.length < 2) {
                item {
                    Text("QUICK PICK", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(bottom = 8.dp))
                }
                itemsIndexed(CityStore.suggestions, key = { _, c -> "${c.name}|${c.country}" }) { _, city ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.Card)
                            .pressClickable {
                                // Save coordinates immediately
                                Settings.set(ctx, city.lat, city.lon, "MWL")
                                CityStore.set(ctx, city.name, city.country)
                                PrayerReminderScheduler.scheduleAllForToday(ctx)
                                onPicked(city.name, city.country)
                            }
                            .padding(horizontal = JannahSpacing.m, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(city.name, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = JannahColors.Ink)
                        }
                        Text(city.country, style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            } else {
                item {
                    Text("SEARCH RESULTS", style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(bottom = 8.dp))
                }
                itemsIndexed(results, key = { i, r -> "res|$i|${r.name}|${r.country}" }) { _, r ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(JannahColors.Card)
                            .pressClickable {
                                // Use lat/lon from API
                                Settings.set(ctx, r.lat, r.lon, "MWL")
                                CityStore.set(ctx, r.name, r.country)
                                PrayerReminderScheduler.scheduleAllForToday(ctx)
                                onPicked(r.name, r.country)
                            }
                            .padding(horizontal = JannahSpacing.m, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(r.name, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = JannahColors.Ink)
                            r.admin1?.let { if (it.isNotBlank()) Text(it, style = JannahType.caption, color = JannahColors.Muted) }
                        }
                        Text(r.country, style = JannahType.caption, color = JannahColors.Muted)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}
