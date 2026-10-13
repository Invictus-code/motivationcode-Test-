package de.motivationcode.app.data.own

import de.motivationcode.app.data.model.Quote
import kotlinx.serialization.Serializable

/** Ein selbst angelegtes Zitat, wie es in own_quotes.json gespeichert wird. */
@Serializable
data class OwnQuote(
    val id: String,
    val text: String,
    val author: String? = null,
    val createdAt: Long,
) {
    fun toQuote(): Quote = Quote(
        id = id,
        text = text,
        author = author.orEmpty(),
        categories = emptyList(),
        isOwn = true,
    )

    companion object {
        const val MAX_TEXT_LENGTH = 300
        const val MAX_AUTHOR_LENGTH = 80
        const val ID_PREFIX = "own-"
    }
}

/**
 * Bereinigt die Eingabe: Leerzeichen am Rand weg und Anführungszeichen am Anfang/Ende
 * entfernen, weil die App selbst „…“ ergänzt.
 */
fun normalizeQuoteText(input: String): String =
    input.trim()
        .trimStart('„', '"', '“', '”', '»', '«', '\'')
        .trimEnd('“', '"', '”', '«', '»', '\'')
        .trim()
        .take(OwnQuote.MAX_TEXT_LENGTH)

fun normalizeAuthor(input: String): String? =
    input.trim().take(OwnQuote.MAX_AUTHOR_LENGTH).ifBlank { null }
