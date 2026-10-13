package de.motivationcode.app.data

import de.motivationcode.app.data.own.OwnQuote
import de.motivationcode.app.data.own.normalizeAuthor
import de.motivationcode.app.data.own.normalizeQuoteText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OwnQuoteTextTest {

    @Test
    fun `trims whitespace`() {
        assertEquals("Mach es einfach.", normalizeQuoteText("  Mach es einfach.\n"))
    }

    @Test
    fun `removes quotation marks the user typed`() {
        assertEquals("Mach es einfach.", normalizeQuoteText("„Mach es einfach.“"))
        assertEquals("Mach es einfach.", normalizeQuoteText("\"Mach es einfach.\""))
        assertEquals("Mach es einfach.", normalizeQuoteText("»Mach es einfach.«"))
    }

    @Test
    fun `keeps quotation marks inside the text`() {
        assertEquals("Sag „Ja“ zum Leben", normalizeQuoteText("Sag „Ja“ zum Leben"))
    }

    @Test
    fun `blank text becomes empty`() {
        assertEquals("", normalizeQuoteText("   "))
        assertEquals("", normalizeQuoteText("„“"))
    }

    @Test
    fun `text is cut at the maximum length`() {
        assertEquals(OwnQuote.MAX_TEXT_LENGTH, normalizeQuoteText("a".repeat(1000)).length)
    }

    @Test
    fun `blank author becomes null`() {
        assertNull(normalizeAuthor("  "))
        assertEquals("Oma Hilde", normalizeAuthor(" Oma Hilde "))
    }
}
