package com.jannahos

// Verifies an Al-Fatihah recitation by matching the words the recognizer
// returns against the vocabulary of the surah. This is not a tajwid check
// and not a proof of worship. It is a signal, stored privately, that a
// recitation happened.

object FatihahVerifier {

    // Key words/phrases from Al-Fatihah as they typically appear in
    // speech-recognizer output for English transliteration.
    // Grouped by ayah so we can score sequential coverage.
    private val AYAH_MARKERS: List<List<String>> = listOf(
        // 1. Bismillah
        listOf("bismillah", "bismi", "rahman", "rahim", "raheem", "rahmani", "raheemi"),
        // 2. Alhamdulillah
        listOf("hamd", "hamdu", "lillah", "lillahi", "rabbil", "alamin", "alameen", "aalameen"),
        // 3. Ar-Rahman Ar-Rahim
        listOf("rahman", "rahim", "raheem", "rahmani", "raheemi"),
        // 4. Maliki yawmid din
        listOf("malik", "maalik", "yawm", "yawmi", "deen", "din", "yawmiddin"),
        // 5. Iyyaka na'budu
        listOf("iyyaka", "iyyaka", "nabudu", "naabudu", "na budu"),
        // 6. Iyyaka nasta'in
        listOf("iyyaka", "nastain", "nasta een", "nastaeen", "nastaeen"),
        // 7. Ihdina sirat
        listOf("ihdina", "ihdinas", "sirat", "siraat", "mustaqeem", "mustaqim")
    )

    data class Result(
        val matchedAyahs: Int,
        val totalAyahs: Int,
        val transcript: String,
        val verified: Boolean
    )

    fun verify(spokenText: String): Result {
        val lower = spokenText.lowercase()
        var matched = 0
        AYAH_MARKERS.forEach { markers ->
            if (markers.any { lower.contains(it) }) matched++
        }
        // Require at least 3 of 7 ayah markers — this is generous enough
        // that partial recitation still verifies, strict enough that
        // random speech does not.
        val verified = matched >= 3
        return Result(
            matchedAyahs = matched,
            totalAyahs = AYAH_MARKERS.size,
            transcript = spokenText,
            verified = verified
        )
    }
}
