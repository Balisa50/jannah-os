package com.jannahos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.jannahos.IfThenStore
import com.jannahos.ui.TopBar
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun IfThenScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var plans by remember { mutableStateOf(IfThenStore.getPlans(ctx)) }
    var trigger by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("If-Then Plans", onBack = onBack)
        
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(JannahSpacing.gutter)) {
            item {
                Text("Close the intention-action gap. Plan exactly how you will respond when a challenge hits.", style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 22.sp)
                Spacer(Modifier.height(JannahSpacing.l))
                OutlinedTextField(
                    value = trigger, onValueChange = { trigger = it },
                    label = { Text("IF ... (trigger)") },
                    placeholder = { Text("e.g., I feel anxious") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(JannahSpacing.s))
                OutlinedTextField(
                    value = action, onValueChange = { action = it },
                    label = { Text("THEN I WILL ... (action)") },
                    placeholder = { Text("e.g., Pray 2 rak'ahs") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(JannahSpacing.m))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (trigger.isBlank() || action.isBlank()) JannahColors.Border else JannahColors.Orange)
                        .pressClickable(enabled = trigger.isNotBlank() && action.isNotBlank()) {
                            IfThenStore.addPlan(ctx, trigger.trim(), action.trim())
                            plans = IfThenStore.getPlans(ctx)
                            trigger = ""; action = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save Plan", color = JannahColors.OnOrange, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(JannahSpacing.xl))
            }
            
            items(plans.size) { i ->
                val plan = plans[i]
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Text("IF ${plan.first}", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                    Spacer(Modifier.height(4.dp))
                    Text("THEN I WILL ${plan.second}", fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
                    Spacer(Modifier.height(8.dp))
                    Text("Remove", style = JannahType.micro, color = JannahColors.Muted, modifier = Modifier.pressClickable {
                        IfThenStore.removePlan(ctx, plan.first, plan.second)
                        plans = IfThenStore.getPlans(ctx)
                    })
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
            }
        }
    }
}
