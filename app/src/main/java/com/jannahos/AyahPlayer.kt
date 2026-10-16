package com.jannahos

import android.media.MediaPlayer

// One-shot player for ayah audio. Keeps a single MediaPlayer instance.
class AyahPlayer {

    private var player: MediaPlayer? = null
    private var onComplete: (() -> Unit)? = null

    fun play(url: String, onDone: () -> Unit) {
        stop()
        onComplete = onDone
        try {
            val mp = MediaPlayer()
            mp.setDataSource(url)
            mp.setOnPreparedListener { it.start() }
            mp.setOnCompletionListener {
                onComplete?.invoke()
                stop()
            }
            mp.setOnErrorListener { _, _, _ ->
                onComplete?.invoke()
                stop()
                true
            }
            mp.prepareAsync()
            player = mp
        } catch (e: Exception) {
            onComplete?.invoke()
            onComplete = null
        }
    }

    fun stop() {
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }

    fun isPlaying(): Boolean = try { player?.isPlaying == true } catch (_: Exception) { false }
}
