package de.motivationcode.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import de.motivationcode.app.data.themes.QuoteFont
import de.motivationcode.app.data.themes.QuoteTheme

fun QuoteFont.toFontFamily(): FontFamily = when (this) {
    QuoteFont.SERIF -> FontFamily.Serif
    QuoteFont.SANS -> FontFamily.SansSerif
    QuoteFont.CURSIVE -> FontFamily.Cursive
    QuoteFont.MONO -> FontFamily.Monospace
}

fun QuoteTheme.backgroundBrush(): Brush {
    val colors = backgroundColors.map { Color(it) }.distinct()
    return if (colors.size == 1) SolidColor(colors.first()) else Brush.verticalGradient(colors)
}
