package com.jannahos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType

object IstighfarStore {
    private const val PREFS = "jannah_istighfar"
    private const val KEY_DAY = "day"
    private const val KEY_COUNT = "count"

    fun todayCount(context: Context): Int {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedDay = p.getString(KEY_DAY, "")
        val today = WakeLog.todayStamp()
        if (storedDay != today) return 0
        return p.getInt(KEY_COUNT, 0)
    }

    fun increment(context: Context): Int {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = WakeLog.todayStamp()
        val storedDay = p.getString(KEY_DAY, "")
        val current = if (storedDay == today) p.getInt(KEY_COUNT, 0) else 0
        val next = current + 1
        p.edit().putString(KEY_DAY, today).putInt(KEY_COUNT, next).apply()
        return next
    }

    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

class IstighfarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    IstighfarScreen()
                }
            }
        }
    }

    @Composable
    private fun IstighfarScreen() {
        var count by remember { mutableStateOf(IstighfarStore.todayCount(this)) }
        val dua = Duas.istighfar

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JannahSpacing.screenGutter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xxl))

            Text("Istighfar", style = JannahType.screenTitle, color = JannahColors.DayText)

            Spacer(Modifier.height(JannahSpacing.l))

            Text("Today: $count", style = JannahType.section, color = JannahColors.DayMuted)

            Spacer(Modifier.height(JannahSpacing.xl))

            Text(dua.arabic, style = JannahType.arabic, color = JannahColors.DayText, textAlign = TextAlign.Center)
            Spacer(Modifier.height(JannahSpacing.m))
            Text(dua.transliteration, style = JannahType.secondary, color = JannahColors.DayMuted,
                textAlign = TextAlign.Center, lineHeight = 22.sp)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(dua.translation, style = JannahType.primary, color = JannahColors.DayText,
                textAlign = TextAlign.Center, lineHeight = 24.sp)
            Spacer(Modifier.height(JannahSpacing.s))
            Text(dua.source, style = JannahType.tertiary, color = JannahColors.DayMuted)

            Spacer(Modifier.height(JannahSpacing.xl))

            Button(
                onClick = { count = IstighfarStore.increment(this@IstighfarActivity) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = JannahColors.Accent,
                    contentColor = JannahColors.AccentInk
                )
            ) {
                Text("Recite one", style = JannahType.button)
            }

            Spacer(Modifier.height(JannahSpacing.l))

            Text(
                "The Prophet ﷺ sought forgiveness more than seventy times a day. This counter is a private reminder. It is not a score.",
                style = JannahType.tertiary,
                color = JannahColors.DayMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(JannahSpacing.m))

            TextButton(onClick = {
                IstighfarStore.reset(this@IstighfarActivity)
                count = 0
            }) {
                Text("Reset today", style = JannahType.tertiary, color = JannahColors.DayMuted)
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}
