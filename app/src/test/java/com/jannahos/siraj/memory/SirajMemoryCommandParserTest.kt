package com.jannahos.siraj.memory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SirajMemoryCommandParserTest {
    @Test fun parsesExplicitRememberPrefix() {
        assertEquals("I prefer concise answers", SirajMemoryCommandParser.rememberContent(
            "remember: I prefer concise answers"
        ))
    }

    @Test fun parsesRememberThatPhrase() {
        assertEquals("My project is called SIRAJ", SirajMemoryCommandParser.rememberContent(
            "Remember that my project is called SIRAJ"
        ))
    }

    @Test fun doesNotCaptureUnclearRememberIntent() {
        assertNull(SirajMemoryCommandParser.rememberContent("remember me to call my friend"))
    }

    @Test fun parsesExplicitForgetQuery() {
        assertEquals("concise answers", SirajMemoryCommandParser.forgetQuery(
            "forget: concise answers"
        ))
    }

    @Test fun recognizesShowAndClearCommands() {
        assertTrue(SirajMemoryCommandParser.isShowMemoryRequest("what do you remember"))
        assertTrue(SirajMemoryCommandParser.isClearMemoryRequest("clear memory"))
        assertFalse(SirajMemoryCommandParser.isClearMemoryRequest("memory is important"))
    }
}
