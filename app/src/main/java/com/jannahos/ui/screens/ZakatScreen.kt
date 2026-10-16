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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.Zakat
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ZakatScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current

    var prices by remember { mutableStateOf<Zakat.Prices?>(null) }
    var pricesError by remember { mutableStateOf(false) }
    var useSilver by remember { mutableStateOf(true) }

    // Manual override fields (empty unless user edits)
    var manualGold by remember { mutableStateOf("") }
    var manualSilver by remember { mutableStateOf("") }

    var cash by remember { mutableStateOf("") }
    var goldGrams by remember { mutableStateOf("") }
    var silverGrams by remember { mutableStateOf("") }
    var investments by remember { mutableStateOf("") }
    var business by remember { mutableStateOf("") }
    var receivables by remember { mutableStateOf("") }
    var debts by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val p = withContext(Dispatchers.IO) { Zakat.fetchPrices() }
        prices = p
        pricesError = p == null
    }

    // Effective prices: use manual if set, else fetched
    val effGold = manualGold.toDoubleOrNull() ?: prices?.goldPerGram ?: 0.0
    val effSilver = manualSilver.toDoubleOrNull() ?: prices?.silverPerGram ?: 0.0
    val effective = Zakat.Prices(effGold, effSilver, prices?.fetched == true)

    val assets = Zakat.Assets(
        cash = cash.toDoubleOrNull() ?: 0.0,
        goldGrams = goldGrams.toDoubleOrNull() ?: 0.0,
        silverGrams = silverGrams.toDoubleOrNull() ?: 0.0,
        investments = investments.toDoubleOrNull() ?: 0.0,
        businessInventory = business.toDoubleOrNull() ?: 0.0,
        receivables = receivables.toDoubleOrNull() ?: 0.0,
        debtsOwed = debts.toDoubleOrNull() ?: 0.0
    )

    val result = if (effGold > 0 || effSilver > 0)
        Zakat.calculate(assets, effective, useSilver)
    else null

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
                Text("ZAKAT",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("The third ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("pillar.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("2.5% of wealth held above nisab for one lunar year.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            // Live prices block
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("TODAY'S METAL PRICES",
                        style = JannahType.eyebrow,
                        color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        when {
                            prices != null -> "Gold $${"%.2f".format(prices!!.goldPerGram)}/g · Silver $${"%.2f".format(prices!!.silverPerGram)}/g"
                            pricesError -> "Could not load live prices. Enter manually."
                            else -> "Loading..."
                        },
                        style = JannahType.bodySmall,
                        color = JannahColors.Muted
                    )
                    Spacer(Modifier.height(JannahSpacing.m))
                    Text("Override if needed", style = JannahType.caption, color = JannahColors.Muted)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
                    ) {
                        OutlinedTextField(
                            value = manualGold,
                            onValueChange = { manualGold = it },
                            label = { Text("Gold / g", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = manualSilver,
                            onValueChange = { manualSilver = it },
                            label = { Text("Silver / g", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            // Nisab method
            Text("NISAB METHOD", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JannahSpacing.s)
            ) {
                NisabChip("Silver (595g)", useSilver, Modifier.weight(1f)) { useSilver = true }
                NisabChip("Gold (85g)", !useSilver, Modifier.weight(1f)) { useSilver = false }
            }
            Spacer(Modifier.height(JannahSpacing.xs))
            Text(
                "Silver is lower, so it applies to more people, many scholars prefer it for that reason.",
                style = JannahType.caption, color = JannahColors.Muted, lineHeight = 16.sp
            )

            Spacer(Modifier.height(JannahSpacing.l))

            // Assets
            Text("YOUR WEALTH", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))

            AssetField("Cash & bank balances", cash) { cash = it }
            AssetField("Gold (grams)", goldGrams) { goldGrams = it }
            AssetField("Silver (grams)", silverGrams) { silverGrams = it }
            AssetField("Investments (stocks, funds)", investments) { investments = it }
            AssetField("Business inventory value", business) { business = it }
            AssetField("Money owed to you", receivables) { receivables = it }

            Spacer(Modifier.height(JannahSpacing.l))

            Text("DEBTS YOU OWE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(JannahSpacing.s))
            AssetField("Debts due now (not future instalments)", debts) { debts = it }

            Spacer(Modifier.height(JannahSpacing.l))

            // Result
            if (result != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.OrangeSoft)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("YOUR ZAKAT",
                            style = JannahType.eyebrow,
                            color = JannahColors.OrangeDeep)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            if (result.eligible) "$" + "%.2f".format(result.zakatDue) else "Not eligible",
                            fontFamily = FontFamily.Serif,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink,
                            letterSpacing = (-1).sp
                        )
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            if (result.eligible)
                                "2.5% of $${"%.2f".format(result.net)} net wealth, above nisab of $${"%.2f".format(result.nisabValue)} (${result.method})."
                            else
                                "Your net wealth ($" + "%.2f".format(result.net) + ") is below nisab ($" + "%.2f".format(result.nisabValue) + "). No zakat is due."
                            ,
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                    .background(JannahColors.Card)
                    .padding(JannahSpacing.l)
            ) {
                Column {
                    Text("EVIDENCE",
                        style = JannahType.eyebrow,
                        color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "Allah says: \"Take from their wealth a charity by which you purify them and cause them increase.\" (Qur'an 9:103)",
                        style = JannahType.bodySmall,
                        color = JannahColors.Body,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "The Prophet ﷺ set nisab for silver at 200 dirhams and for gold at 20 dinars. Different scholarly opinions exist on the exact gram equivalents.",
                        style = JannahType.caption,
                        color = JannahColors.Muted,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(
                        "This is a calculator, not a fatwa. For complex cases, consult a scholar.",
                        style = JannahType.micro,
                        color = JannahColors.Faint
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
private fun NisabChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(48.dp)
            .clip(RoundedCornerShape(JannahSpacing.pillRadius))
            .background(if (selected) JannahColors.Orange else JannahColors.Card)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = JannahType.caption,
            color = if (selected) JannahColors.OnOrange else JannahColors.Ink)
    }
}

@Composable
private fun AssetField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}
