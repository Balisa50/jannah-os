package com.jannahos

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.SwipeBackScreen
import com.jannahos.ui.TopBar
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class QiblaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                SwipeBackScreen(onBack = { finish() }) { QiblaScreen() }
            }
        }
    }
}

@Composable
fun QiblaScreen() {
    val ctx = LocalContext.current
    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)

    val kaabaLat = 21.4225
    val kaabaLon = 39.8262

    val dLon = (kaabaLon - lon) * PI / 180.0
    val lat1 = lat * PI / 180.0
    val lat2 = kaabaLat * PI / 180.0
    val y = sin(dLon)
    val x = cos(lat1) * kotlin.math.tan(lat2) - sin(lat1) * cos(dLon)
    var bearing = atan2(y, x) * 180.0 / PI
    if (bearing < 0) bearing += 360.0

    Column(Modifier.fillMaxSize().background(JannahColors.Cream)) {
        TopBar("Qibla", onBack = { })
        
        Column(Modifier.weight(1f).padding(JannahSpacing.gutter), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("FACE", style = JannahType.eyebrow, color = JannahColors.OrangeDeep)
            Spacer(Modifier.height(8.dp))
            Text("${bearing.toInt()}°", fontFamily = FontFamily.Serif, fontSize = 72.sp, fontWeight = FontWeight.Bold, color = JannahColors.Ink)
            Spacer(Modifier.height(16.dp))
            Text("From North", style = JannahType.body, color = JannahColors.Muted)
            Spacer(Modifier.height(32.dp))
            Box(Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)).background(JannahColors.OrangeSoft).padding(16.dp)) {
                Text(
                    "Use any physical compass or your phone's map app to face this exact degree. This ensures you are facing the Kaaba in Makkah.",
                    style = JannahType.bodySmall, color = JannahColors.OrangeDeep, textAlign = TextAlign.Center, lineHeight = 22.sp
                )
            }
        }
    }
}
