package de.motivationcode.app.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withTranslation
import de.motivationcode.app.data.model.Quote
import de.motivationcode.app.data.themes.QuoteFont
import de.motivationcode.app.data.themes.QuoteTheme

/** Zeichnet ein Zitat als Bild (Hochformat 4:5, gut für Messenger und Social Media). */
object QuoteImageRenderer {

    fun render(quote: Quote, theme: QuoteTheme, width: Int = 1080, height: Int = 1350): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawBackground(canvas, theme, width, height)

        val textColor = theme.textColor.toInt()
        val typeface = theme.font.toTypeface()
        val margin = width * 0.1f
        val textWidth = (width - 2 * margin).toInt()

        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            this.typeface = typeface
        }
        val quoteText = "„${quote.text}“"
        var textSize = 76f
        var quoteLayout: StaticLayout
        do {
            quotePaint.textSize = textSize
            quoteLayout = centeredLayout(quoteText, quotePaint, textWidth, lineSpacing = 1.25f)
            textSize -= 4f
        } while (quoteLayout.height > height * 0.6f && textSize > 28f)

        val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ColorUtils.setAlphaComponent(textColor, 230)
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            this.textSize = 40f
        }
        val authorLayout = centeredLayout("— ${quote.author}", authorPaint, textWidth, lineSpacing = 1.1f)

        val gap = 48f
        val top = (height - (quoteLayout.height + gap + authorLayout.height)) / 2f
        canvas.withTranslation(margin, top) {
            quoteLayout.draw(this)
            translate(0f, quoteLayout.height + gap)
            authorLayout.draw(this)
        }

        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ColorUtils.setAlphaComponent(textColor, 160)
            this.textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Tagesmotivation", width / 2f, height - 56f, footerPaint)
        return bitmap
    }

    private fun drawBackground(canvas: Canvas, theme: QuoteTheme, width: Int, height: Int) {
        val colors = theme.backgroundColors.map { it.toInt() }.toIntArray()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (colors.size > 1) {
            paint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colors, null, Shader.TileMode.CLAMP)
        } else {
            paint.color = colors.first()
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun centeredLayout(text: String, paint: TextPaint, width: Int, lineSpacing: Float): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, lineSpacing)
            .build()

    private fun QuoteFont.toTypeface(): Typeface = when (this) {
        QuoteFont.SERIF -> Typeface.SERIF
        QuoteFont.SANS -> Typeface.SANS_SERIF
        QuoteFont.CURSIVE -> Typeface.create("cursive", Typeface.NORMAL)
        QuoteFont.MONO -> Typeface.MONOSPACE
    }
}
