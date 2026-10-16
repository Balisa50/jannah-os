package com.jannahos

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

// Continuous surah playback. Plays ayah by ayah in order. Auto-advances
// on completion. No ExoPlayer dependency — plain MediaPlayer is enough
// for sequential audio and stays tiny.
class AyahPlaylist(private val context: Context) {

    interface Listener {
        fun onAyahStarted(surah: Int, ayah: Int)
        fun onSurahFinished(surah: Int)
    }

    private var player: MediaPlayer? = null
    private var currentSurah: Int = 0
    private var currentAyah: Int = 0
    private var lastAyah: Int = 0
    private var reciter: Reciters.Reciter? = null
    private var listener: Listener? = null
    private var playing = false
    private val globalAyahByLocal = mutableMapOf<Int, Int>() // local ayah -> global

    fun setListener(l: Listener?) { listener = l }

    fun isPlaying(): Boolean = playing

    fun playSurah(
        surah: Int,
        startAyah: Int,
        endAyah: Int,
        ayahs: List<QuranApi.Ayah>
    ) {
        if (ayahs.isEmpty()) return
        reciter = Reciters.current(context)
        currentSurah = surah
        lastAyah = endAyah
        globalAyahByLocal.clear()
        ayahs.forEach { globalAyahByLocal[it.number] = it.globalNumber }
        playAyah(surah, startAyah)
    }

    private fun playAyah(surah: Int, ayah: Int) {
        stopInternal()

        val rec = reciter ?: Reciters.current(context).also { reciter = it }
        val global = globalAyahByLocal[ayah]
        if (global == null || global == 0) {
            // Fall back to next ayah once
            if (ayah < lastAyah) playAyah(surah, ayah + 1) else finished(surah)
            return
        }

        currentAyah = ayah
        val url = Reciters.audioUrl(rec, global)

        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(url)
            mp.setOnPreparedListener {
                playing = true
                it.start()
                listener?.onAyahStarted(surah, ayah)
            }
            mp.setOnCompletionListener {
                playing = false
                if (currentAyah < lastAyah) {
                    playAyah(surah, currentAyah + 1)
                } else {
                    finished(surah)
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                playing = false
                // Skip to next on failure
                if (currentAyah < lastAyah) playAyah(surah, currentAyah + 1)
                else finished(surah)
                true
            }
            mp.prepareAsync()
            player = mp
        } catch (e: Exception) {
            Log.w("AyahPlaylist", "play failed: ${e.message}")
            playing = false
        }
    }

    fun stop() {
        stopInternal()
        playing = false
    }

    private fun stopInternal() {
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }

    private fun finished(surah: Int) {
        playing = false
        listener?.onSurahFinished(surah)
    }
}
