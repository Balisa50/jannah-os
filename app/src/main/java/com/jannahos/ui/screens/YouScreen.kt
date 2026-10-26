package com.jannahos.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.BedtimeActivity
import com.jannahos.Settings
import com.jannahos.SettingsActivity
import com.jannahos.ui.pressClickable
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun YouScreen(onNavigate: (String) -> Unit = {}) {
    val ctx = LocalContext.current
    val name = Settings.displayName(ctx)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).background(JannahColors.Cream)) {
        Column(Modifier.padding(horizontal = JannahSpacing.gutter).padding(top = 64.dp, bottom = 32.dp)) {
            Text(if (name.isBlank()) "You" else name, fontFamily = FontFamily.Serif, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            SectionLabel("YOUR PRACTICE")
            ListRow("Notes & Diary") { onNavigate("notes") }
            ListRow("Weekly review") { onNavigate("weekly") }
            ListRow("Private commitments") { onNavigate("struggle") }
            ListRow("Tonight's plan") { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
            ListRow("Letters to future self") { onNavigate("letters") }
            ListRow("Morning & Evening Adhkar") { onNavigate("adhkar") }
            ListRow("Backup & restore") { onNavigate("backup") }

            Spacer(Modifier.height(JannahSpacing.xl))

            SectionLabel("SETTINGS")
            ListRow("App Settings") { ctx.startActivity(Intent(ctx, SettingsActivity::class.java)) }
            ListRow("Ramadan") { onNavigate("ramadan") }
            ListRow("About Jannah") { onNavigate("about") }
            
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(top = 24.dp, bottom = 8.dp))
}

@Composable
private fun ListRow(label: String, onClick: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().pressClickable(onClick = onClick).padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = JannahColors.Ink, modifier = Modifier.weight(1f))
            Text("→", style = JannahType.body, color = JannahColors.Orange)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
    }
}
