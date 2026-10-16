package com.jannahos

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object HijriDate {
    fun today(): String {
        return try {
            val h = HijrahDate.from(LocalDate.now())
            val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)
            h.format(fmt)
        } catch (e: Exception) {
            ""
        }
    }
}
