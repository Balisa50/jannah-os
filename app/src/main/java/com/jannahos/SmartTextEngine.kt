package com.jannahos

object SmartTextEngine {
    fun process(input: String): String {
        var text = input
        
        // 1. Auto-convert Salawat (catching all common spelling mistakes)
        val salawatTriggers = listOf(
            "salallahu alaihi wa salam",
            "sallahu alaihi wa salaam",
            "sallallahu alaihi wa sallam",
            "sallahu alaihi wa salam",
            "saw",
            "pbuh"
        )
        for (trigger in salawatTriggers) {
            if (text.endsWith("$trigger ") || text == trigger) {
                text = text.replace(trigger, "ﷺ ")
            }
        }

        // 2. Auto-convert Subhanahu wa Ta'ala
        val swtTriggers = listOf("subhanahu wa taala", "swt")
        for (trigger in swtTriggers) {
            if (text.endsWith("$trigger ") || text == trigger) {
                text = text.replace(trigger, "سبحانه وتعالى ")
            }
        }
        
        return text
    }
}
