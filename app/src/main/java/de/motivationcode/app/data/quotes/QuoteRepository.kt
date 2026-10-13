package de.motivationcode.app.data.quotes

import android.content.Context
import de.motivationcode.app.data.model.Quote
import de.motivationcode.app.data.own.OwnQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/** Alle Zitate der App: mitgelieferte und eigene. Ändert sich, sobald eigene Zitate dazukommen. */
interface QuoteRepository {
    val quotes: Flow<List<Quote>>
}

class DefaultQuoteRepository(
    private val loadBundled: suspend () -> List<Quote>,
    ownQuotes: Flow<List<OwnQuote>>,
) : QuoteRepository {
    override val quotes: Flow<List<Quote>> =
        combine(flow { emit(loadBundled()) }, ownQuotes) { bundled, own ->
            own.map { it.toQuote() } + bundled
        }
}

/** Liest die mitgelieferten Zitate einmal aus assets/quotes.json und hält sie im Speicher. */
class BundledQuotes(private val context: Context) {
    private val mutex = Mutex()
    private var cache: List<Quote>? = null

    suspend fun load(): List<Quote> = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) {
            val text = context.assets.open(QUOTES_FILE).bufferedReader().use { it.readText() }
            parseQuotes(text)
        }.also { cache = it }
    }

    companion object {
        const val QUOTES_FILE = "quotes.json"
        private val json = Json { ignoreUnknownKeys = true }

        fun parseQuotes(text: String): List<Quote> = json.decodeFromString(text)
    }
}
