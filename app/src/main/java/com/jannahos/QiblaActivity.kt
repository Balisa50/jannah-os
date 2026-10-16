package com.jannahos

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jannahos.ui.theme.JannahColors
import com.jannahos.ui.theme.JannahSpacing
import com.jannahos.ui.theme.JannahTheme
import com.jannahos.ui.theme.JannahType
import kotlin.math.cos
import kotlin.math.sin

// Bearing from (lat,lon) to the Kaaba in Makkah.
private fun qiblaBearing(lat: Double, lon: Double): Float {
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLon = Math.toRadians(39.8262)
    val phi = Math.toRadians(lat)
    val lambda = Math.toRadians(lon)
    val dLon = kaabaLon - lambda
    val y = sin(dLon)
    val x = cos(phi) * Math.tan(kaabaLat) - sin(phi) * cos(dLon)
    val bearing = Math.toDegrees(kotlin.math.atan2(y, x))
    return ((bearing + 360.0) % 360.0).toFloat()
}

class QiblaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JannahTheme {
                QiblaScreen(onBack = { finish() })
            }
        }
    }
}

@Composable
private fun QiblaScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var heading by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sm.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        if (rotation == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                private val r = FloatArray(9)
                private val orientation = FloatArray(3)
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(r, event.values)
                    SensorManager.getOrientation(r, orientation)
                    val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    heading = ((deg + 360f) % 360f)
                }
                override fun onAccuracyChanged(s: Sensor?, accuracy: Int) {}
            }
            sm.registerListener(listener, rotation, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(listener) }
        }
    }

    val lat = Settings.lat(ctx)
    val lon = Settings.lon(ctx)
    val qibla = qiblaBearing(lat, lon)
    val needleRotation by animateFloatAsState(qibla - heading, tween(220), label = "q")

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
                Text("QIBLA",
                    style = JannahType.eyebrow,
                    color = JannahColors.OnOrange.copy(alpha = 0.75f))
                Spacer(Modifier.height(JannahSpacing.xs))
                Box {
                    Text("Face ", style = JannahType.title.copy(fontSize = 24.sp),
                        color = JannahColors.OnOrange)
                }
            }
        }

        Column(
            Modifier.padding(horizontal = JannahSpacing.gutter).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(JannahSpacing.xl))

            if (!sensorAvailable) {
                Text(
                    "This device has no compass sensor. Use any compass and face ${qibla.toInt()}°.",
                    style = JannahType.body, color = JannahColors.Body,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(JannahSpacing.l))
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(300.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = size.minDimension / 2f * 0.92f

                        // Outer ring
                        drawCircle(
                            color = JannahColors.Border,
                            radius = r,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2f)
                        )

                        // Direction ticks
                        for (deg in 0 until 360 step 15) {
                            val rad = Math.toRadians((deg - 90).toDouble())
                            val inner = if (deg % 90 == 0) r - 16f else r - 8f
                            drawLine(
                                color = JannahColors.Faint,
                                start = Offset(
                                    (cx + cos(rad) * inner).toFloat(),
                                    (cy + sin(rad) * inner).toFloat()
                                ),
                                end = Offset(
                                    (cx + cos(rad) * r).toFloat(),
                                    (cy + sin(rad) * r).toFloat()
                                ),
                                strokeWidth = if (deg % 90 == 0) 2f else 1f
                            )
                        }
                    }

                    // Rotating needle pointing at the Kaaba
                    Canvas(modifier = Modifier.size(300.dp)) {
                        rotate(needleRotation) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val top = cy - size.height * 0.40f
                            val left = cx - 14f
                            val right = cx + 14f
                            val bottom = cy + 20f

                            val p = Path().apply {
                                moveTo(cx, top)
                                lineTo(right, bottom)
                                lineTo(cx, bottom - 22f)
                                lineTo(left, bottom)
                                close()
                            }
                            drawPath(p, color = JannahColors.Orange)
                            drawCircle(
                                color = JannahColors.Ink,
                                radius = 6f,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    Text(
                        "${qibla.toInt()}°",
                        fontFamily = FontFamily.Serif,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = JannahColors.Ink
                    )
                }

                Spacer(Modifier.height(JannahSpacing.l))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(JannahSpacing.cardRadius))
                        .background(JannahColors.Card)
                        .padding(JannahSpacing.l)
                ) {
                    Column {
                        Text("How to use",
                            fontFamily = FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = JannahColors.Ink)
                        Spacer(Modifier.height(JannahSpacing.s))
                        Text(
                            "Hold the phone flat. Rotate your body until the orange arrow points to the top of the screen. That direction is the qibla.",
                            style = JannahType.bodySmall,
                            color = JannahColors.Body,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(Modifier.height(JannahSpacing.l))

                Box(
                    Modifier
                        .clip(RoundedCornerShape(JannahSpacing.pillRadius))
                        .background(JannahColors.Ink)
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                ) {
                    Text("Bearing to Makkah: ${qibla.toInt()}°",
                        style = JannahType.button, color = JannahColors.OnOrange)
                }
            }

            Spacer(Modifier.height(JannahSpacing.xl))
        }
    }
}
