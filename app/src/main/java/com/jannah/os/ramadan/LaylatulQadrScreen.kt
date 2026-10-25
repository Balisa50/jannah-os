package com.jannah.os.ramadan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Cream = Color(0xFFFBF6EE)
private val Orange = Color(0xFFE07A3F)
private val Ink = Color(0xFF1F1B17)
private val InkSoft = Color(0xFF8A8078)
private val Hairline = Color(0xFFE7DED1)
private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private data class Night(val number: Int, val isOdd: Boolean)

@Composable
fun LaylatulQadrScreen(
    completedNights: Set<Int> = emptySet(),
    onToggleNight: (Int) -> Unit = {}
) {
    val nights = remember {
        (21..30).map { Night(number = it, isOdd = it % 2 == 1) }
    }
    val local = remember { mutableStateMapOf<Int, Boolean>().apply {
        nights.forEach { put(it.number, completedNights.contains(it.number)) }
    } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 22.dp)
    ) {
        Spacer(Modifier.height(28.dp))
        Text(
            text = "Laylatul Qadr",
            fontFamily = Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp,
            color = Ink
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "The last ten nights. Odd nights are emphasised.",
            fontFamily = Sans,
            fontSize = 14.sp,
            color = InkSoft
        )
        Spacer(Modifier.height(22.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(nights) { night ->
                val done = local[night.number] == true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (done) Color(0xFFF3EADF) else Color.White)
                        .border(
                            width = if (night.isOdd) 1.5.dp else 1.dp,
                            color = if (night.isOdd) Orange else Hairline,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            local[night.number] = !done
                            onToggleNight(night.number)
                        }
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Night ${night.number}",
                            fontFamily = Serif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (night.isOdd) "Odd" else "Even",
                            fontFamily = Sans,
                            fontSize = 12.sp,
                            color = if (night.isOdd) Orange else InkSoft
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (done) Orange else Color.White)
                            .border(1.dp, if (done) Orange else Hairline, CircleShape)
                    )
                }
            }
        }
    }
}
