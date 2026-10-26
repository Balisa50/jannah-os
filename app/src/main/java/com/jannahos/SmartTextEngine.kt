package com.jannahos

object SmartTextEngine {
    fun process(input: String): String {
        var text = input
        // Only trigger when followed by a space or at the end
        if (text.endsWith("sallahu alaihi wa salaam ") || text == "sallahu alaihi wa salaam") {
            text = text.replace("sallahu alaihi wa salaam", "ﷺ ")
        }
        if (text.endsWith("subhanahu wa taala ") || text == "subhanahu wa taala") {
            text = text.replace("subhanahu wa taala", "سبحانه وتعالى ")
        }
        return text
    }
}
