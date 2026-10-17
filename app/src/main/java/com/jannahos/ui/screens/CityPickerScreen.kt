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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.CityStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun CityPickerScreen(onBack: () -> Unit, onPicked: (String, String) -> Unit) {
    BackHandler(onBack = onBack)

    var query by remember { mutableStateOf("") }
    var customCity by remember { mutableStateOf("") }
    var customCountry by remember { mutableStateOf("") }

    val filtered = remember(query) {
        if (query.isBlank()) CityStore.suggestions
        else CityStore.suggestions.filter {
            it.first.lowercase().contains(query.lowercase()) ||
            it.second.lowercase().contains(query.lowercase())
        }
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
                    label = { Text("Search city", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(JannahSpacing.l))
            }

            if (filtered.isEmpty()) {
                item {
                    Text("No match. Enter it manually.",
                        style = JannahType.caption, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.m))
                    OutlinedTextField(
                        value = customCity,
                        onValueChange = { customCity = it },
                        label = { Text("City", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    OutlinedTextField(
                        value = customCountry,
                        onValueChange = { customCountry = it },
                        label = { Text("Country", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(JannahSpacing.m))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                            .background(
                                if (customCity.isBlank() || customCountry.isBlank())
                                    JannahColors.Border else JannahColors.Orange
                            )
                            .clickable {
                                if (customCity.isNotBlank() && customCountry.isNotBlank()) {
                                    onPicked(customCity.trim(), customCountry.trim())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Use this city",
                            style = JannahType.button,
                            color = if (customCity.isBlank() || customCountry.isBlank())
                                JannahColors.Faint else JannahColors.OnOrange)
                    }
                }
            } else {
                items(filtered, key = { "${it.first}-${it.second}" }) { (c, cn) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(JannahColors.Card)
                            .clickable { onPicked(c, cn) }
                            .padding(horizontal = JannahSpacing.m, vertical = 16.dp)
                    ) {
                        Text(c,
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = JannahColors.Ink,
                            modifier = Modifier.weight(1f))
                        Text(cn,
                            style = JannahType.caption,
                            color = JannahColors.Muted)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}
