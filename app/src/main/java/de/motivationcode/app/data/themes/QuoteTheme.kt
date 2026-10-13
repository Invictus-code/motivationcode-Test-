package de.motivationcode.app.data.themes

enum class QuoteFont { SERIF, SANS, CURSIVE, MONO }

/**
 * Aussehen einer Zitat-Karte. Farben als ARGB-Long (0xFFRRGGBB), damit sie sowohl in Compose
 * als auch beim Zeichnen des Teilen-Bildes ohne Umrechnung nutzbar sind.
 */
data class QuoteTheme(
    val id: String,
    val name: String,
    val backgroundColors: List<Long>,
    val textColor: Long,
    val font: QuoteFont,
) {
    /** Heller Hintergrund → Statusleiste braucht dunkle Symbole. */
    val isLight: Boolean
        get() = relativeLuminance(textColor) < 0.5
}

object QuoteThemes {
    val all: List<QuoteTheme> = listOf(
        QuoteTheme("nacht", "Nacht", listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364), 0xFFFFFFFF, QuoteFont.SERIF),
        QuoteTheme("morgenrot", "Morgenrot", listOf(0xFF6A1B4D, 0xFFB4473A), 0xFFFFFFFF, QuoteFont.SERIF),
        QuoteTheme("ozean", "Ozean", listOf(0xFF0B3D91, 0xFF1C7C8C), 0xFFFFFFFF, QuoteFont.SANS),
        QuoteTheme("wald", "Wald", listOf(0xFF1B3B2F, 0xFF2F5D46), 0xFFF3EBD3, QuoteFont.SERIF),
        QuoteTheme("papier", "Papier", listOf(0xFFF5EEDC, 0xFFEDE3C8), 0xFF3B2F2F, QuoteFont.SERIF),
        QuoteTheme("lavendel", "Lavendel", listOf(0xFF4B2A6B, 0xFF7B4E9C), 0xFFFFFFFF, QuoteFont.CURSIVE),
        QuoteTheme("polarlicht", "Polarlicht", listOf(0xFF1A2A6C, 0xFF2E7058), 0xFFFFFFFF, QuoteFont.SANS),
        QuoteTheme("sand", "Sand", listOf(0xFFE8D5B7, 0xFFD9BF95), 0xFF3E2C1C, QuoteFont.SERIF),
        QuoteTheme("kirschbluete", "Kirschblüte", listOf(0xFFFCE4EC, 0xFFF8BBD0), 0xFF4A1C2C, QuoteFont.CURSIVE),
        QuoteTheme("schiefer", "Schiefer", listOf(0xFF232526, 0xFF414345), 0xFFFFFFFF, QuoteFont.MONO),
        QuoteTheme("hell", "Minimal hell", listOf(0xFFFAFAFA, 0xFFFAFAFA), 0xFF1F1F1F, QuoteFont.SANS),
        QuoteTheme("dunkel", "Minimal dunkel", listOf(0xFF121212, 0xFF121212), 0xFFF5F5F5, QuoteFont.SANS),
    )

    val default: QuoteTheme = all.first()

    fun byId(id: String): QuoteTheme = all.firstOrNull { it.id == id } ?: default
}

/** Relative Leuchtdichte nach WCAG 2.x, Eingabe als ARGB. */
fun relativeLuminance(argb: Long): Double {
    fun channel(shift: Int): Double {
        val c = ((argb shr shift) and 0xFF) / 255.0
        return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
    }
    return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
}

/** Kontrastverhältnis nach WCAG (1.0 bis 21.0). */
fun contrastRatio(a: Long, b: Long): Double {
    val la = relativeLuminance(a)
    val lb = relativeLuminance(b)
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
}
