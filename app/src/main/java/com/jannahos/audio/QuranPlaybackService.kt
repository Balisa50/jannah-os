package com.jannahos.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jannahos.AyahPlaylist
import com.jannahos.MainActivity
import com.jannahos.QuranApi
import com.jannahos.QuranCache
import com.jannahos.R
import com.jannahos.Reciters
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class QuranPlaybackService : Service() {

    private var playlist: AyahPlaylist? = null
    private var surahName: String = ""
    private var ayahLabel: String = ""
    private var currentSurah: Int = 0
    private var currentAyah: Int = 0
    private var started = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Absolutely first: register as foreground. Nothing else before this.
        // If this fails, Android kills the process, so we log and stop cleanly.
        try {
            ensureChannel()
            startForeground(NOTIF_ID, buildNotification())
            started = true
        } catch (t: Throwable) {
            writeCrash("startForeground failed", t)
            stopSelf()
            return
        }

        // Now safe to set up the player.
        try {
            playlist = AyahPlaylist(this).apply {
                setListener(object : AyahPlaylist.Listener {
                    override fun onAyahStarted(surah: Int, ayah: Int) {
                        currentSurah = surah
                        currentAyah = ayah
                        surahName = QuranCache.SURAHS.firstOrNull { it.first == surah }?.second
                            ?: "Surah $surah"
                        ayahLabel = "Ayah $ayah"
                        PlaybackBus.started(surah, ayah)
                        updateNotification()
                    }
                    override fun onSurahFinished(surah: Int) {
                        PlaybackBus.stopped()
                        shutdown()
                    }
                })
            }
        } catch (t: Throwable) {
            writeCrash("playlist setup failed", t)
            shutdown()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Guarantee we are still foreground (Android checks occasionally).
        if (!started) {
            try {
                ensureChannel()
                startForeground(NOTIF_ID, buildNotification())
                started = true
            } catch (t: Throwable) {
                writeCrash("onStartCommand startForeground failed", t)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        when (intent?.action) {
            ACTION_STOP -> { shutdown(); return START_NOT_STICKY }
            ACTION_PLAY -> {
                val surah = intent.getIntExtra("surah", 0)
                val startAyah = intent.getIntExtra("start", 1)
                val endAyah = intent.getIntExtra("end", 0)
                if (surah > 0) playFromCache(surah, startAyah, endAyah)
            }
        }
        return START_NOT_STICKY
    }

    private fun playFromCache(surah: Int, startAyah: Int, endAyah: Int) {
        Thread {
            try {
                val cache = QuranCache(this)
                val ayahs = if (cache.isCached(surah)) cache.load(surah)
                else {
                    val f = QuranApi.fetchSurah(surah)
                    cache.save(surah, f)
                    f
                }
                if (ayahs.isEmpty()) { shutdown(); return@Thread }
                val end = if (endAyah <= 0) ayahs.last().number else endAyah
                playlist?.playSurah(surah, startAyah, end, ayahs)
            } catch (t: Throwable) {
                writeCrash("playFromCache failed", t)
                shutdown()
            }
        }.start()
    }

    private fun shutdown() {
        try { playlist?.stop() } catch (_: Throwable) {}
        PlaybackBus.stopped()
        try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (_: Throwable) {}
        stopSelf()
    }

    override fun onDestroy() {
        try { playlist?.stop() } catch (_: Throwable) {}
        super.onDestroy()
    }

    // ---- Notification ----

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL,
                        "Quran recitation",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Reciter playing in the background"
                        setShowBadge(false)
                        setSound(null, null)
                        enableVibration(false)
                    }
                )
            }
        }
    }

    private fun buildNotification(): Notification {
        // Use a system icon, guaranteed valid on every device.
        val icon = android.R.drawable.ic_media_play

        val open = Intent(this, MainActivity::class.java)
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getActivity(this, 0, open, flags)

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, QuranPlaybackService::class.java).setAction(ACTION_STOP),
            flags
        )

        val reciter = try { Reciters.current(this) } catch (_: Throwable) { null }

        val title = if (surahName.isBlank()) "Quran" else surahName
        val text = if (reciter != null) "${reciter.label} · $ayahLabel" else ayahLabel

        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pi)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .build()
    }

    private fun updateNotification() {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotification())
        } catch (_: Throwable) {}
    }

    private fun writeCrash(tag: String, t: Throwable) {
        try {
            val sw = StringWriter()
            t.printStackTrace(PrintWriter(sw))
            val dir = File(filesDir, "crashes")
            if (!dir.exists()) dir.mkdirs()
            File(dir, "svc_${System.currentTimeMillis()}.txt").writeText("$tag\n\n$sw")
        } catch (_: Throwable) {}
    }

    companion object {
        private const val CHANNEL = "quran_playback"
        private const val NOTIF_ID = 4001
        const val ACTION_PLAY = "com.jannahos.audio.PLAY"
        const val ACTION_STOP = "com.jannahos.audio.STOP"

        fun play(context: Context, surah: Int, startAyah: Int, endAyah: Int) {
            val i = Intent(context, QuranPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra("surah", surah)
                putExtra("start", startAyah)
                putExtra("end", endAyah)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(i)
                else context.startService(i)
            } catch (t: Throwable) {
                // Fallback: plain startService. On Android 8+ the service
                // must still call startForeground() within 5s of onCreate , 
                // which it does.
                try { context.startService(i) } catch (_: Throwable) {}
            }
        }

        fun stop(context: Context) {
            try {
                val i = Intent(context, QuranPlaybackService::class.java).setAction(ACTION_STOP)
                context.startService(i)
            } catch (_: Throwable) {}
        }
    }
}
