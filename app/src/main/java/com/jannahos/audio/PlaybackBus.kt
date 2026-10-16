package com.jannahos.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Single source of truth for what is currently playing.
// The service writes; any screen reads. No IPC, no listeners.
object PlaybackBus {
    data class State(
        val surah: Int = 0,
        val ayah: Int = 0,
        val playing: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun started(surah: Int, ayah: Int) {
        _state.value = State(surah, ayah, true)
    }

    fun stopped() {
        _state.value = State()
    }
}
