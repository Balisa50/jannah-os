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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.SadaqahLog
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SadaqahScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val ctx = LocalContext.current
    val log = remember { SadaqahLog(ctx) }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh
    val recent = log.recent(30)
    val totals = log.totalByCurrency()
    val count30 = log.countLast30()

    var amount by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("GMD") }
    var kind by remember { mutableStateOf("sadaqah") }
    var note by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    val dayFmt = SimpleDateFormat("d MMM · HH:mm", Locale.getDefault())

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
                Text("SADAQAH",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("$count30 ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("gifts in 30 days.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("The giving you do quietly.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            if (totals.isNotEmpty()) {
                Text("TOTAL GIVEN", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                totals.forEach { (cur, amt) ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cur, style = JannahType.bodySmall, color = JannahColors.Muted)
                        Text("%.2f".format(amt),
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            Text("LOG A GIFT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.weight(1.4f),
                    label = { Text("Amount", fontSize = 11.sp) }
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Currency", fontSize = 11.sp) }
                )
            }

            Spacer(Modifier.height(JannahSpacing.s))

            SadaqahLog.KINDS.forEach { (code, label) ->
                val selected = kind == code
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) JannahColors.OrangeSoft else JannahColors.Card)
                        .clickable { kind = code }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label,
                        style = JannahType.bodySmall,
                        color = if (selected) JannahColors.OrangeDeep else JannahColors.Body,
                        modifier = Modifier.weight(1f))
                    if (selected) Text("✓",
                        style = JannahType.bodySmall, color = JannahColors.Orange)
                }
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(JannahSpacing.s))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (optional)", fontSize = 11.sp) }
            )

            Spacer(Modifier.height(JannahSpacing.m))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                    .background(
                        if (amount.toDoubleOrNull() == null || amount.toDoubleOrNull()!! <= 0.0)
                            JannahColors.Border else JannahColors.Ink
                    )
                    .clickable {
                        val a = amount.toDoubleOrNull() ?: return@clickable
                        if (a <= 0.0) return@clickable
                        log.add(a, currency.ifBlank { "GMD" }, kind, note.ifBlank { null })
                        amount = ""
                        note = ""
                        saved = true
                        refresh++
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Log gift",
                    style = JannahType.button,
                    color = if (amount.toDoubleOrNull() == null || amount.toDoubleOrNull()!! <= 0.0)
                        JannahColors.Faint else JannahColors.OnOrange)
            }

            if (saved) {
                Spacer(Modifier.height(JannahSpacing.s))
                Text("Recorded.",
                    style = JannahType.caption, color = JannahColors.Orange,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(JannahSpacing.l))

            if (recent.isNotEmpty()) {
                Text("RECENT", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                recent.forEachIndexed { i, g ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(SadaqahLog.label(g.kind),
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                            Spacer(Modifier.height(2.dp))
                            Text(dayFmt.format(Date(g.ts)),
                                style = JannahType.micro, color = JannahColors.Faint)
                            if (!g.note.isNullOrBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(g.note,
                                    style = JannahType.caption,
                                    color = JannahColors.Muted,
                                    fontStyle = FontStyle.Italic)
                            }
                        }
                        Text("%.2f %s".format(g.amount, g.currency),
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Orange)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { log.delete(g.id); refresh++ }
                                .padding(6.dp)
                        ) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                    }
                    if (i < recent.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("EVIDENCE",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "The Prophet ﷺ said: \"Charity does not decrease wealth.\" (Sahih Muslim 2588)",
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .clip(RoundedCornerShape(JannahSpacing.pillRadius))
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
