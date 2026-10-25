package com.jannahos.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.jannahos.R.drawable.dhikr_hero),
                contentDescription = "You",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)))))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = JannahSpacing.gutter).padding(bottom = JannahSpacing.m)) {
                Text(if (name.isBlank()) "You." else "$name.", fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
            }
        }

        Spacer(Modifier.height(JannahSpacing.m))
        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            
            SectionLabel("YOUR PRACTICE")
            ListRow("Weekly review") { onNavigate("weekly") }
            Divider()
            ListRow("Diary") { onNavigate("diary") }
            Divider()
            ListRow("Private commitments") { onNavigate("struggle") }
            Divider()
            ListRow("Tonight's plan") { ctx.startActivity(Intent(ctx, BedtimeActivity::class.java)) }
            Divider()
            ListRow("Letters to future self") { onNavigate("letters") }
            Divider()
            ListRow("Backup & restore") { onNavigate("backup") }
            
            Spacer(Modifier.height(JannahSpacing.l))
            
            SectionLabel("SETTINGS")
            ListRow("App Settings") { ctx.startActivity(Intent(ctx, SettingsActivity::class.java)) }
            Divider()
            ListRow("About Jannah") { onNavigate("about") }
            Divider()
            ListRow("Ramadan") { onNavigate("ramadan") }
            Spacer(Modifier.height(JannahSpacing.xxl))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(text, style = JannahType.eyebrow, color = JannahColors.OrangeDeep, modifier = Modifier.padding(top = JannahSpacing.m, bottom = JannahSpacing.s))
    }
}

@Composable
private fun ListRow(label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().pressClickable(onClick = onClick).padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = JannahColors.Ink, modifier = Modifier.weight(1f))
        Text("→", style = JannahType.body, color = JannahColors.Orange)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Border))
}
