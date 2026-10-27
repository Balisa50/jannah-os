package com.jannahos

object SmartTextEngine {
    fun process(input: String): String {
        var text = input
        
        // Maktabook-style Shortcuts
        val shortcuts = mapOf(
            "pbuh" to "صلى الله عليه وسلم ",
            "saw" to "صلى الله عليه وسلم ",
            "swt" to "سبحانه وتعالى ",
            "ra" to "رضي الله عنه ",
            "insh" to "إن شاء الله ",
            "inshaallah" to "إن شاء الله ",
            "alham" to "الحمد لله ",
            "alhamdulillah" to "الحمد لله "
        )

        for ((trigger, replacement) in shortcuts) {
            if (text.endsWith("$trigger ", ignoreCase = true) || text.equals(trigger, ignoreCase = true)) {
                val regex = Regex("(?i)$trigger\\s*$")
                text = text.replace(regex, replacement)
            }
        }
        
        return text
    }
}
