package com.jannahos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun ScreenHeader(
    title: String,
    eyebrow: String,
    subtitle: String? = null,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(JannahColors.Cream)
            .statusBarsPadding()
            .padding(horizontal = JannahSpacing.gutter)
            .padding(top = 12.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(JannahColors.SurfaceAlt)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("←", fontSize = 20.sp, color = JannahColors.Ink, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(eyebrow.uppercase(), style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(3.dp))
                Text(title, fontFamily = FontFamily.Serif, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink, lineHeight = 31.sp)
            }
        }
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 20.sp)
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
}


@Composable
fun ScreenTitle(
    title: String,
    eyebrow: String,
    subtitle: String? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(JannahColors.Cream)
            .statusBarsPadding()
            .padding(horizontal = JannahSpacing.gutter)
            .padding(top = 22.dp, bottom = 20.dp)
    ) {
        Text(eyebrow.uppercase(), style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
        Spacer(Modifier.height(6.dp))
        Text(
            title,
            fontFamily = FontFamily.Serif,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            lineHeight = 40.sp
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(subtitle, style = JannahType.bodySmall, color = JannahColors.Muted, lineHeight = 20.sp)
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
}
