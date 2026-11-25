package com.jannahos.siraj.core

sealed interface SirajAction {
    data class OpenSurah(val number: Int, val name: String) : SirajAction
    data object OpenQuran : SirajAction
    data object OpenPrayerTimes : SirajAction
    data object OpenQibla : SirajAction
    data object OpenQuranSearch : SirajAction
    data object OpenAdhkar : SirajAction
    data object OpenNotes : SirajAction
    data object OpenSettings : SirajAction
}

data class SirajCommandResult(
    val reply: String,
    val action: SirajAction? = null
)
