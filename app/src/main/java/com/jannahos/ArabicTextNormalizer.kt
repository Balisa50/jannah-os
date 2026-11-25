package com.jannahos

/**
 * Search-only Arabic normalization. It removes tashkeel and Quranic annotation
 * marks while preserving letters, and folds common alef/hamza variants.
 * Original source text is never modified or replaced.
 */
object ArabicTextNormalizer {
    private val marks = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val nonLetters = Regex("[^\\u0621-\\u064A\\s]")

    fun normalize(value: String): String = value
        .replace(marks, "")
        .replace('\u0640'.toString(), "")
        .replace(Regex("[\\u0622\\u0623\\u0625\\u0671]"), "ا")
        .replace("ؤ", "و")
        .replace("ئ", "ي")
        .replace("ى", "ي")
        .replace("ة", "ه")
        .replace(nonLetters, " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
