package com.jannahos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NightBg = Color(0xFF0E1116)
private val NightSurface = Color(0xFF161A22)
private val NightText = Color(0xFFE8E5DC)
private val NightMuted = Color(0xFF8B8F98)
private val GoldAccent = Color(0xFFC9A961)

private val Colors = darkColorScheme(
    background = NightBg,
    surface = NightSurface,
    onBackground = NightText,
    onSurface = NightText,
    primary = GoldAccent,
    onPrimary = NightBg
)

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
            MaterialTheme(colorScheme = Colors) {
                Surface(modifier = Modifier.fillMaxSize(), color = NightBg) {
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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            Text("Istighfar", color = NightText, fontSize = 26.sp,
                fontWeight = FontWeight.Light, letterSpacing = 2.sp)

            Spacer(Modifier.height(28.dp))

            Text("Today: $count", color = NightMuted, fontSize = 13.sp, letterSpacing = 1.sp)

            Spacer(Modifier.height(40.dp))

            Text(dua.arabic, color = NightText, fontSize = 22.sp,
                textAlign = TextAlign.Center, lineHeight = 38.sp)
            Spacer(Modifier.height(18.dp))
            Text(dua.transliteration, color = NightMuted, fontSize = 14.sp,
                textAlign = TextAlign.Center, lineHeight = 22.sp)
            Spacer(Modifier.height(14.dp))
            Text(dua.translation, color = NightText, fontSize = 15.sp,
                textAlign = TextAlign.Center, lineHeight = 24.sp)
            Spacer(Modifier.height(12.dp))
            Text(dua.source, color = NightMuted, fontSize = 11.sp)

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { count = IstighfarStore.increment(this@IstighfarActivity) },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NightBg)
            ) {
                Text("Recite one", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "The Prophet ﷺ sought forgiveness more than seventy times a day. This counter is a private reminder. It is not a score.",
                color = NightMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp
            )

            Spacer(Modifier.height(24.dp))

            TextButton(onClick = {
                IstighfarStore.reset(this@IstighfarActivity)
                count = 0
            }) {
                Text("Reset today", color = NightMuted, fontSize = 11.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
