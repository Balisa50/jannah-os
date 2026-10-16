package com.jannahos.ui.screens

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
import com.jannahos.FinanceStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun FinanceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { FinanceStore(ctx) }
    val monthKey = remember { store.currentMonthKey() }
    var refresh by remember { mutableStateOf(0) }
    val ignore = refresh

    val month = store.month(monthKey)
    val expenses = store.expenses(monthKey)
    val spent = store.totalSpent(monthKey)

    var salaryInput by remember { mutableStateOf(month?.salary?.toString() ?: "") }
    var currency by remember { mutableStateOf(month?.currency ?: "GMD") }
    var showSetup by remember { mutableStateOf(month == null) }

    var newAmount by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("food") }
    var newNote by remember { mutableStateOf("") }

    val salary = month?.salary ?: 0.0
    val floor = salary * 0.5      // 0.5x, essentials floor
    val ceiling = salary * 1.5    // 1.5x, total spending ceiling
    val spentRatio = if (salary > 0) spent / salary else 0.0

    val statusColor = when {
        salary <= 0 -> JannahColors.Muted
        spent <= floor -> JannahColors.Orange
        spent <= ceiling -> JannahColors.OrangeDeep
        else -> JannahColors.Danger
    }

    val statusText = when {
        salary <= 0 -> "Set your salary to begin."
        spent <= floor -> "Well within balance. Balanced spending."
        spent <= ceiling -> "Approaching your ceiling. Slow down."
        else -> "Above your monthly ceiling. Review the waste."
    }

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
                Text("FINANCE",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("The middle ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("path.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Between waste and stinginess, Qur'an 25:67",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Setup card
            if (showSetup) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("MONTHLY SALARY",
                            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                        ) {
                            OutlinedTextField(
                                value = salaryInput,
                                onValueChange = { salaryInput = it },
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
                        Spacer(Modifier.height(JannahSpacing.m))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                                .background(JannahColors.Ink)
                                .clickable {
                                    val s = salaryInput.toDoubleOrNull() ?: return@clickable
                                    if (s > 0) {
                                        store.setSalary(monthKey, s, currency.ifBlank { "GMD" })
                                        showSetup = false
                                        refresh++
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Set salary",
                                style = JannahType.button, color = JannahColors.OnOrange)
                        }
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            } else if (month != null) {
                // Balance card, the heart of the module
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("THIS MONTH",
                            style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("%.0f".format(spent),
                                fontFamily = FontFamily.Serif,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink,
                                letterSpacing = (-1).sp,
                                lineHeight = 48.sp)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text(month.currency,
                                style = JannahType.title.copy(fontSize = 16.sp),
                                color = JannahColors.Muted,
                                modifier = Modifier.padding(bottom = 8.dp))
                        }
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(statusText,
                            style = JannahType.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold)

                        Spacer(Modifier.height(JannahSpacing.m))

                        // Progress bar with floor and ceiling marks
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(JannahColors.Border)
                        ) {
                            val pct = (spentRatio / 1.5).coerceIn(0.0, 1.0).toFloat()
                            Box(
                                Modifier
                                    .fillMaxWidth(pct)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(statusColor)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0", style = JannahType.micro, color = JannahColors.Faint)
                            Text("0.5×  %.0f".format(floor),
                                style = JannahType.micro, color = JannahColors.Faint)
                            Text("1.5×  %.0f".format(ceiling),
                                style = JannahType.micro, color = JannahColors.Faint)
                        }

                        Spacer(Modifier.height(JannahSpacing.l))

                        // Rule cards
                        RuleRow("0.5× floor", "%.0f %s".format(floor, month.currency),
                            "Essentials. Below this you are under-investing in your own life.")
                        Spacer(Modifier.height(JannahSpacing.s))
                        RuleRow("1.5× ceiling", "%.0f %s".format(ceiling, month.currency),
                            "Total outflow. Above this is wastage (Qur'an 17:27).")
                        Spacer(Modifier.height(JannahSpacing.s))
                        RuleRow("Remaining", "%.0f %s".format((ceiling - spent).coerceAtLeast(0.0), month.currency),
                            "Still within your balance this month.")
                    }
                }

                Spacer(Modifier.height(JannahSpacing.m))

                // Reset salary
                Box(
                    Modifier
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .clickable { showSetup = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Change salary or currency",
                        style = JannahType.caption, color = JannahColors.Muted)
                }

                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Log an expense
            if (!showSetup && month != null) {
                Text("LOG A SPEND", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))

                OutlinedTextField(
                    value = newAmount,
                    onValueChange = { newAmount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount", fontSize = 11.sp) }
                )
                Spacer(Modifier.height(JannahSpacing.s))

                // Category grid, 3 per row
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FinanceStore.CATEGORIES.chunked(2).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            row.forEach { (code, label) ->
                                val selected = newCategory == code
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) JannahColors.OrangeSoft else JannahColors.Card)
                                        .clickable { newCategory = code }
                                        .padding(horizontal = 10.dp, vertical = 12.dp)
                                ) {
                                    Text(label,
                                        style = JannahType.caption,
                                        color = if (selected) JannahColors.OrangeDeep else JannahColors.Body,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(JannahSpacing.s))
                OutlinedTextField(
                    value = newNote,
                    onValueChange = { newNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note (optional)", fontSize = 11.sp) }
                )

                Spacer(Modifier.height(JannahSpacing.s))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(
                            if (newAmount.toDoubleOrNull() == null ||
                                newAmount.toDoubleOrNull()!! <= 0.0)
                                JannahColors.Border else JannahColors.Ink
                        )
                        .clickable {
                            val a = newAmount.toDoubleOrNull() ?: return@clickable
                            if (a <= 0) return@clickable
                            store.addExpense(monthKey, newCategory, a, newNote.ifBlank { null })
                            newAmount = ""
                            newNote = ""
                            refresh++
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Log expense",
                        style = JannahType.button,
                        color = if (newAmount.toDoubleOrNull() == null ||
                            newAmount.toDoubleOrNull()!! <= 0.0)
                            JannahColors.Faint else JannahColors.OnOrange)
                }

                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Recent expenses
            if (expenses.isNotEmpty() && !showSetup) {
                Text("THIS MONTH'S SPENDING",
                    style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                expenses.forEachIndexed { i, e ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(FinanceStore.label(e.category),
                                fontFamily = FontFamily.Serif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = JannahColors.Ink)
                            if (!e.note.isNullOrBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(e.note,
                                    style = JannahType.caption,
                                    color = JannahColors.Muted,
                                    fontStyle = FontStyle.Italic)
                            }
                        }
                        Text("%.2f".format(e.amount),
                            fontFamily = FontFamily.Serif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { store.deleteExpense(e.id); refresh++ }
                                .padding(6.dp)
                        ) { Text("×", style = JannahType.body, color = JannahColors.Muted) }
                    }
                    if (i < expenses.size - 1) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
                    }
                }
                Spacer(Modifier.height(JannahSpacing.l))
            }

            // Anchor card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.OrangeSoft)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("THE PRINCIPLE",
                        style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "\"And those who, when they spend, are neither extravagant nor niggardly, but hold a medium course between those extremes.\"",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.sp,
                        color = JannahColors.Ink,
                        lineHeight = 24.sp
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text("Qur'an 25:67",
                        style = JannahType.micro, color = JannahColors.Faint)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "The 0.5× / 1.5× band is a practical shape of this principle. Below 0.5× you may be under-spending on your own needs. Above 1.5× is the territory of excess.",
                        style = JannahType.caption,
                        color = JannahColors.Body,
                        lineHeight = 16.sp
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

@Composable
private fun RuleRow(label: String, value: String, note: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label,
                style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(2.dp))
            Text(note,
                style = JannahType.caption,
                color = JannahColors.Muted,
                lineHeight = 16.sp)
        }
        Text(value,
            fontFamily = FontFamily.Serif,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink)
    }
}
