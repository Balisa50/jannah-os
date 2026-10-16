package com.jannahos

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

// Continuous surah playback with gapless transitions.
// Uses MediaPlayer.setNextMediaPlayer so the next ayah starts the moment
// the current one ends — no silence between verses.
class AyahPlaylist(private val context: Context) {

    interface Listener {
        fun onAyahStarted(surah: Int, ayah: Int)
        fun onSurahFinished(surah: Int)
    }

    private var currentPlayer: MediaPlayer? = null
    private var currentLocalAyah: Int = -1
    private var surah: Int = 0
    private var lastAyah: Int = 0
    private var reciter: Reciters.Reciter? = null
    private var listener: Listener? = null
    private var playing = false
    private val globalAyahByLocal = mutableMapOf<Int, Int>()

    fun setListener(l: Listener?) { listener = l }
    fun isPlaying(): Boolean = playing

    fun playSurah(surah: Int, startAyah: Int, endAyah: Int, ayahs: List<QuranApi.Ayah>) {
        if (ayahs.isEmpty()) return
        stop()
        reciter = Reciters.current(context)
        this.surah = surah
        this.lastAyah = endAyah
        globalAyahByLocal.clear()
        ayahs.forEach { globalAyahByLocal[it.number] = it.globalNumber }
        startAyah(startAyah)
    }

    private fun startAyah(localAyah: Int) {
        releaseCurrent()
        if (localAyah > lastAyah) {
            playing = false
            listener?.onSurahFinished(surah)
            return
        }
        currentLocalAyah = localAyah
        val global = globalAyahByLocal[localAyah] ?: return
        val rec = reciter ?: return
        if (global == 0) { startAyah(localAyah + 1); return }

        try {
            val mp = newPlayer(rec, global)
            mp.setOnPreparedListener { prepared ->
                prepared.start()
                playing = true
                listener?.onAyahStarted(surah, localAyah)
                chainNext(prepared, localAyah)
            }
            mp.setOnErrorListener { _, _, _ ->
                playing = false
                startAyah(localAyah + 1)
                true
            }
            mp.prepareAsync()
            currentPlayer = mp
        } catch (e: Exception) {
            Log.w("AyahPlaylist", "play failed: ${e.message}")
            startAyah(localAyah + 1)
        }
    }

    private fun chainNext(current: MediaPlayer, localAyah: Int) {
        val nextLocal = localAyah + 1
        if (nextLocal > lastAyah) {
            // End of surah — fire finished when current completes.
            current.setOnCompletionListener {
                playing = false
                listener?.onSurahFinished(surah)
            }
            return
        }
        val nextGlobal = globalAyahByLocal[nextLocal] ?: run {
            current.setOnCompletionListener { startAyah(nextLocal) }
            return
        }
        val rec = reciter ?: return

        try {
            val next = newPlayer(rec, nextGlobal)
            next.setOnPreparedListener { preparedNext ->
                try {
                    // Attach to the current player so it plays seamlessly after.
                    current.setNextMediaPlayer(preparedNext)
                    // When current completes, preparedNext starts.
                    // onCompletion fires at the transition moment.
                    current.setOnCompletionListener {
                        currentLocalAyah = nextLocal
                        currentPlayer = preparedNext
                        listener?.onAyahStarted(surah, nextLocal)
                        chainNext(preparedNext, nextLocal)
                    }
                } catch (t: Throwable) {
                    // Fallback: on completion start next explicitly.
                    try { preparedNext.release() } catch (_: Exception) {}
                    current.setOnCompletionListener { startAyah(nextLocal) }
                }
            }
            next.setOnErrorListener { _, _, _ -> true }
            next.prepareAsync()
        } catch (e: Exception) {
            current.setOnCompletionListener { startAyah(nextLocal) }
        }
    }

    private fun newPlayer(rec: Reciters.Reciter, globalAyah: Int): MediaPlayer {
        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        mp.setDataSource(Reciters.audioUrl(rec, globalAyah))
        return mp
    }

    fun stop() {
        releaseCurrent()
        playing = false
        currentLocalAyah = -1
    }

    private fun releaseCurrent() {
        try { currentPlayer?.stop() } catch (_: Exception) {}
        try { currentPlayer?.release() } catch (_: Exception) {}
        currentPlayer = null
    }
}
