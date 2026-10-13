package de.motivationcode.app.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import de.motivationcode.app.data.model.Quote
import de.motivationcode.app.data.themes.QuoteTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object QuoteSharer {

    fun shareText(quote: Quote): String = "„${quote.text}“ — ${quote.author}"

    /** Rendert das Zitat als PNG in den Cache und öffnet das Android-Teilen-Menü. */
    suspend fun share(context: Context, quote: Quote, theme: QuoteTheme) {
        val uri = withContext(Dispatchers.IO) {
            val bitmap = QuoteImageRenderer.render(quote, theme)
            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            val file = File(dir, "zitat.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareText(quote))
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Zitat teilen"))
    }
}
