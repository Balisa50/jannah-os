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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.DuaLibrary
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahType

@Composable
fun DuaLibraryScreen(onBack: () -> Unit) {
    var selectedMoment by remember { mutableStateOf<String?>(null) }
    var selectedDua by remember { mutableStateOf<DuaLibrary.Dua?>(null) }

    when {
        selectedDua != null -> DuaDetail(
            dua = selectedDua!!,
            onBack = { selectedDua = null }
        )
        selectedMoment != null -> MomentList(
            moment = selectedMoment!!,
            onBack = { selectedMoment = null },
            onPick = { selectedDua = it }
        )
        else -> MomentsIndex(onBack = onBack, onPick = { selectedMoment = it })
    }
}

@Composable
private fun MomentsIndex(onBack: () -> Unit, onPick: (String) -> Unit) {
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
                Text("DUAS",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Row {
                    Text("His words, ", style = JannahType.title.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                    Text("your voice.", style = JannahType.titleItalic.copy(fontSize = 22.sp),
                        color = JannahColors.OnOrange)
                }
                Spacer(Modifier.height(4.dp))
                Text("Every dua sourced from the sunnah.",
                    style = JannahType.caption,
                    color = JannahColors.OnOrange.copy(alpha = 0.85f))
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            DuaLibrary.moments.forEachIndexed { i, moment ->
                val count = DuaLibrary.duas.count { it.moment == moment }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(moment) }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(moment,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        modifier = Modifier.weight(1f))
                    Text("$count dua${if (count > 1) "s" else ""}",
                        style = JannahType.caption, color = JannahColors.Muted)
                    Spacer(Modifier.width(8.dp))
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                if (i < DuaLibrary.moments.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
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
private fun MomentList(moment: String, onBack: () -> Unit, onPick: (DuaLibrary.Dua) -> Unit) {
    val duas = DuaLibrary.duas.filter { it.moment == moment }

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
                Text("DUAS · ${moment.uppercase()}",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Text(moment,
                    fontFamily = FontFamily.Serif,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = JannahColors.OnOrange)
            }
        }

        Column(Modifier.padding(horizontal = JannahSpacing.gutter)) {
            Spacer(Modifier.height(JannahSpacing.l))

            duas.forEachIndexed { i, d ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(d) }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(d.title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink,
                        modifier = Modifier.weight(1f))
                    Text("→", style = JannahType.bodySmall, color = JannahColors.Muted)
                }
                if (i < duas.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(JannahColors.Divider))
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
private fun DuaDetail(dua: DuaLibrary.Dua, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(JannahColors.Cream)
            .padding(horizontal = JannahSpacing.gutter)
    ) {
        Spacer(Modifier.height(JannahSpacing.l))

        
        Spacer(Modifier.height(JannahSpacing.l))

        Text(dua.title,
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = JannahColors.Ink,
            lineHeight = 30.sp)

        Spacer(Modifier.height(JannahSpacing.l))

        // Arabic
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.Card)
                .padding(JannahSpacing.l)
        ) {
            Text(dua.arabic,
                style = JannahType.arabicLarge,
                color = JannahColors.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(JannahSpacing.l))

        // Transliteration
        Text("How to say it",
            style = JannahType.eyebrow,
            color = JannahColors.OrangeDeep)
        Spacer(Modifier.height(JannahSpacing.s))
        Text(dua.transliteration,
            style = JannahType.body,
            color = JannahColors.Body,
            lineHeight = 24.sp)

        Spacer(Modifier.height(JannahSpacing.l))

        // Translation
        Text("Meaning",
            style = JannahType.eyebrow,
            color = JannahColors.OrangeDeep)
        Spacer(Modifier.height(JannahSpacing.s))
        Text(dua.translation,
            style = JannahType.body,
            color = JannahColors.Body,
            lineHeight = 24.sp)

        Spacer(Modifier.height(JannahSpacing.l))

        // Source
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                .background(JannahColors.OrangeSoft)
                .padding(JannahSpacing.l)
        ) {
            Column {
                Text("SOURCE",
                    style = JannahType.eyebrow,
                    color = JannahColors.OrangeDeep)
                Spacer(Modifier.height(JannahSpacing.s))
                Text(dua.source,
                    style = JannahType.bodySmall,
                    color = JannahColors.Ink)
                if (dua.caveat.isNotBlank()) {
                    Spacer(Modifier.height(JannahSpacing.s))
                    Text(dua.caveat,
                        style = JannahType.caption,
                        color = JannahColors.Body,
                        lineHeight = 18.sp)
                }
            }
        }

        Spacer(Modifier.height(JannahSpacing.xxl))
    }
}
