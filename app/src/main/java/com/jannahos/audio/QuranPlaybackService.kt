package com.jannahos.audio

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

class QuranPlaybackService : Service() {

    private var playlist: AyahPlaylist? = null
    private var surahName: String = ""
    private var ayahLabel: String = ""
    private var currentSurah: Int = 0
    private var currentAyah: Int = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
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
                    stopPlaybackAndSelf()
                }
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { stopPlaybackAndSelf(); return START_NOT_STICKY }
            ACTION_PLAY -> {
                val surah = intent.getIntExtra("surah", 0)
                val startAyah = intent.getIntExtra("start", 1)
                val endAyah = intent.getIntExtra("end", 0)
                if (surah > 0) {
                    startForeground(NOTIF_ID, buildNotification())
                    playFromCache(surah, startAyah, endAyah)
                }
            }
            else -> startForeground(NOTIF_ID, buildNotification())
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
                if (ayahs.isEmpty()) { stopPlaybackAndSelf(); return@Thread }
                val end = if (endAyah <= 0) ayahs.last().number else endAyah
                playlist?.playSurah(surah, startAyah, end, ayahs)
            } catch (e: Exception) {
                stopPlaybackAndSelf()
            }
        }.start()
    }

    private fun stopPlaybackAndSelf() {
        playlist?.stop()
        PlaybackBus.stopped()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        playlist?.stop()
        super.onDestroy()
    }

    // ---- Notification ----

    private fun createChannel() {
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
                    }
                )
            }
        }
    }

    private fun buildNotification(): android.app.Notification {
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

        val reciter = Reciters.current(this)

        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.jannah_icon_foreground)
            .setContentTitle(if (surahName.isBlank()) "Quran" else surahName)
            .setContentText("${reciter.label} · $ayahLabel")
            .setContentIntent(pi)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification())
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(i)
            else context.startService(i)
        }

        fun stop(context: Context) {
            val i = Intent(context, QuranPlaybackService::class.java).setAction(ACTION_STOP)
            context.startService(i)
        }
    }
}
