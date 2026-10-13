package de.motivationcode.app.data

import de.motivationcode.app.data.themes.QuoteThemes
import de.motivationcode.app.data.themes.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteThemesTest {

    @Test
    fun `theme ids are unique`() {
        val ids = QuoteThemes.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `text is readable on every background color`() {
        // WCAG AA für normalen Text: mindestens 4,5:1 – gilt auch für die kleinere Quellenzeile.
        QuoteThemes.all.forEach { theme ->
            theme.backgroundColors.forEach { background ->
                val ratio = contrastRatio(theme.textColor, background)
                assertTrue("${theme.name}: Kontrast nur %.2f".format(ratio), ratio >= 4.5)
            }
        }
    }

    @Test
    fun `unknown id falls back to default`() {
        assertEquals(QuoteThemes.default, QuoteThemes.byId("gibtsnicht"))
    }

    @Test
    fun `contrast ratio matches known values`() {
        assertEquals(21.0, contrastRatio(0xFF000000, 0xFFFFFFFF), 0.01)
        assertEquals(1.0, contrastRatio(0xFF777777, 0xFF777777), 0.01)
    }
}
