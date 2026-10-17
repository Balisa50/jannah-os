package com.jannahos.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.CitySearchApi
import com.jannahos.CityStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun CityPickerScreen(onBack: () -> Unit, onPicked: (String, String) -> Unit) {
    BackHandler(onBack = onBack)

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

    Column(
        Modifier
            .fillMaxSize()
            .background(JannahColors.Cream)
    ) {
        TopBar("City", onBack = onBack)

        LazyColumn(
            Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = JannahSpacing.gutter)
        ) {
            item {
                Spacer(Modifier.height(JannahSpacing.s))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search any city", fontSize = 12.sp) },
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
                    Text("No matches. Try a different spelling.",
                        style = JannahType.caption, color = JannahColors.Muted)
                }
            } else if (query.length < 2) {
                item {
                    Text("Start typing to search for any city worldwide.",
                        style = JannahType.caption, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.m))
                }
                itemsIndexed(
                    CityStore.suggestions.take(10),
                    key = { i, (c, cn) -> "sug|$i|$c|$cn" }
                ) { _, (c, cn) ->
                    CityResultRow(c, cn, null) { onPicked(c, cn) }
                }
            } else {
                itemsIndexed(
                    results,
                    key = { i, r -> "res|$i|${r.name}|${r.country}|${r.admin1 ?: ""}" }
                ) { _, r ->
                    CityResultRow(r.name, r.country, r.admin1) { onPicked(r.name, r.country) }
                }
            }
        }
    }
}

@Composable
private fun CityResultRow(name: String, country: String, admin1: String?, onPick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JannahColors.Card)
            .clickable(onClick = onPick)
            .padding(horizontal = JannahSpacing.m, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(name,
                fontFamily = FontFamily.Serif,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = JannahColors.Ink)
            if (!admin1.isNullOrBlank()) {
                Text(admin1,
                    style = JannahType.caption,
                    color = JannahColors.Muted)
            }
        }
        Text(country,
            style = JannahType.caption,
            color = JannahColors.Muted)
    }
    Spacer(Modifier.height(6.dp))
}
