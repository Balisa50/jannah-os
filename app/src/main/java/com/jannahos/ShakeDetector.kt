package com.jannahos

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

// Detects a shake gesture using the accelerometer. No permission needed.
class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {

    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accel: Sensor? = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeMs = 0L
    private val threshold = 12.5f   // m/s^2
    private val cooldown = 400L     // ms between accepted shakes

    fun start() {
        accel?.let { sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stop() {
        sm.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val gForce = magnitude / SensorManager.GRAVITY_EARTH

        if (gForce > threshold / SensorManager.GRAVITY_EARTH + 1.0f) {
            val now = System.currentTimeMillis()
            if (now - lastShakeMs > cooldown) {
                lastShakeMs = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
