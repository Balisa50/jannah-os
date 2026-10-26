package com.jannahos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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

@Composable
fun TopBar(
    title: String,
    onBack: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding() // <--- FIXES THE GRAY BANNER BUG
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.m, vertical = JannahSpacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(JannahColors.Card)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("←", fontSize = 18.sp, color = JannahColors.Ink)
            }
            Spacer(Modifier.width(JannahSpacing.s))
        } else {
            Spacer(Modifier.size(40.dp))
            Spacer(Modifier.width(JannahSpacing.s))
        }
        
        Text(
            title,
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink
        )
    }
}
