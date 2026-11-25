package com.jannahos

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FaceDownService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var proximity: Sensor? = null

    private var isFaceDown = false
    private var isCovered = false
    private var stillnessJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(888, buildNotification())

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        proximity?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val z = event.values[2]
            // Z is roughly -9.8 when face down, +9.8 when face up
            val nowFaceDown = z < -7.0f
            
            if (nowFaceDown != isFaceDown) {
                isFaceDown = nowFaceDown
                checkTrigger()
            }
        }

        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            // Usually 0.0 means covered, > 0 means uncovered
            val nowCovered = event.values[0] < event.sensor.maximumRange
            if (nowCovered != isCovered) {
                isCovered = nowCovered
                checkTrigger()
            }
        }
    }

    private fun checkTrigger() {
        if (isFaceDown && isCovered) {
            // Must be still for 5 seconds before triggering to avoid accidental triggers
            stillnessJob?.cancel()
            stillnessJob = scope.launch {
                delay(5000)
                if (isFaceDown && isCovered && Settings.isFaceDownEnabled(this@FaceDownService)) {
                    triggerBedtimeLock()
                }
            }
        } else {
            stillnessJob?.cancel()
        }
    }

    private fun triggerBedtimeLock() {
        // Stop listening to prevent double triggers
        sensorManager.unregisterListener(this)
        
        // A foreground service still cannot assume it may launch an Activity.
        // Activate the independent bedtime timer and let the user open it from
        // the notification.
        BedtimeReceiver.activate(this, 15 * 60 * 1000L)
        
        // Re-register after a delay
        scope.launch {
            delay(10_000)
            accelerometer?.let { sensorManager.registerListener(this@FaceDownService, it, SensorManager.SENSOR_DELAY_NORMAL) }
            proximity?.let { sensorManager.registerListener(this@FaceDownService, it, SensorManager.SENSOR_DELAY_NORMAL) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        stillnessJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, "face_down_channel")
            .setContentTitle("Face-Down Sleep Active")
            .setContentText("Place your phone face down to activate Bedtime Lock.")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("face_down_channel", "Face-Down Sleep", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
