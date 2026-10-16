package com.jannahos

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

// Continuous surah playback. Preloads the next ayah so transitions are gapless.
class AyahPlaylist(private val context: Context) {

    interface Listener {
        fun onAyahStarted(surah: Int, ayah: Int)
        fun onSurahFinished(surah: Int)
    }

    private var player: MediaPlayer? = null
    private var preloaded: MediaPlayer? = null
    private var preloadedForAyah: Int = -1

    private var currentSurah: Int = 0
    private var currentAyah: Int = 0
    private var lastAyah: Int = 0
    private var reciter: Reciters.Reciter? = null
    private var listener: Listener? = null
    private var playing = false
    private val globalAyahByLocal = mutableMapOf<Int, Int>()

    fun setListener(l: Listener?) { listener = l }
    fun isPlaying(): Boolean = playing

    fun playSurah(surah: Int, startAyah: Int, endAyah: Int, ayahs: List<QuranApi.Ayah>) {
        if (ayahs.isEmpty()) return
        reciter = Reciters.current(context)
        currentSurah = surah
        lastAyah = endAyah
        globalAyahByLocal.clear()
        ayahs.forEach { globalAyahByLocal[it.number] = it.globalNumber }
        playAyah(surah, startAyah)
    }

    private fun playAyah(surah: Int, ayah: Int) {
        releaseCurrent()
        val rec = reciter ?: Reciters.current(context).also { reciter = it }

        // If we have the right ayah preloaded, use it. Otherwise load fresh.
        if (preloaded != null && preloadedForAyah == ayah) {
            player = preloaded
            preloaded = null
            preloadedForAyah = -1
            currentAyah = ayah
            player?.setOnCompletionListener {
                playing = false
                if (currentAyah < lastAyah) playAyah(surah, currentAyah + 1)
                else finished(surah)
            }
            try {
                player?.start()
                playing = true
                listener?.onAyahStarted(surah, ayah)
                preloadNext(surah, ayah, rec)
                return
            } catch (e: Exception) {
                Log.w("AyahPlaylist", "preloaded start failed, fresh loading")
                releaseCurrent()
            }
        }

        val global = globalAyahByLocal[ayah]
        if (global == null || global == 0) {
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
                preloadNext(surah, ayah, rec)
            }
            mp.setOnCompletionListener {
                playing = false
                if (currentAyah < lastAyah) playAyah(surah, currentAyah + 1)
                else finished(surah)
            }
            mp.setOnErrorListener { _, _, _ ->
                playing = false
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

    private fun preloadNext(surah: Int, currentLocalAyah: Int, rec: Reciters.Reciter) {
        val nextAyah = currentLocalAyah + 1
        if (nextAyah > lastAyah) return
        val global = globalAyahByLocal[nextAyah] ?: return
        if (global == 0) return

        try {
            preloaded?.release()
        } catch (_: Exception) {}
        preloaded = null

        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setDataSource(Reciters.audioUrl(rec, global))
            mp.prepareAsync()
            preloaded = mp
            preloadedForAyah = nextAyah
        } catch (e: Exception) {
            preloaded = null
            preloadedForAyah = -1
        }
    }

    fun stop() {
        releaseCurrent()
        try { preloaded?.release() } catch (_: Exception) {}
        preloaded = null
        preloadedForAyah = -1
        playing = false
    }

    private fun releaseCurrent() {
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }

    private fun finished(surah: Int) {
        playing = false
        listener?.onSurahFinished(surah)
    }
}
